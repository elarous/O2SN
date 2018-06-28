(ns o2sn.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [o2sn.ajax :refer [load-interceptors!]]
            [o2sn.events]
            [o2sn.subs]
            [o2sn.ui :as ui]
            [o2sn.views.home :as home]
            [o2sn.views.login :as login]
            [o2sn.views.signup :as signup]
            [o2sn.views.top-menu :as tmenu]
            [o2sn.views.sidebar :as sidebar])
  (:import goog.History))

(defn about-page []
  [ui/popup {:hoverable false
             :position "bottom left"
             :style {:height "auto"}
             :trigger (r/as-element
                       [:span
                        [ui/icon {:namfacebook.come "feed"
                                  :size "large"
                                  :link true}]])}
   [:button "click here"]])



(defn page-trans [page]
  [ui/transition {:visible (not @(rf/subscribe [:page-hiding?]))
                  :animation "scale"
                  :duration 500
                  :transition-on-mount true
                  :mount-on-show true}
   [:div page]])

(defn panel-trans [panel]
  [ui/transition {:visible (not @(rf/subscribe [:panel-hiding?]))
                  :animation "scale"
                  :duration 500
                  :transition-on-mount true
                  :mount-on-show true}
   [:div {:style {:height "100%"}}
    [panel]]])

(defn add-menus [page]
  [:div.h100
   [tmenu/main-menu]
   [sidebar/side-bar
    [panel-trans page]]])

(defn page []
  [:div
   (let [page @(rf/subscribe [:active-page])]
     (if @(rf/subscribe [:with-menu?])
       (-> page add-menus page-trans)
       (page-trans [page])))])

(defn wrap-auth [page-k]
  (if @(rf/subscribe [:require-login? page-k])
    (secretary/dispatch! "/login")
    (rf/dispatch [:set-page page-k])))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (wrap-auth :home))

(secretary/defroute "/story/new" []
  (rf/dispatch [:set-active-panel :new-story]))

(secretary/defroute "/login" []
  (wrap-auth :login))

(secretary/defroute "/signup" []
  (wrap-auth :signup))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(secretary/defroute "/logout" []
  (when @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:logout])))

(secretary/defroute "/messages" []
  (rf/dispatch [:set-active-panel :messages]))

(secretary/defroute "/home" []
  (rf/dispatch [:set-active-panel :home]))

(secretary/defroute "/channels" []
  (rf/dispatch [:set-active-panel :channels]))

(secretary/defroute "/channel/add" []
  (do (rf/dispatch [:set-active-panel :channels])
      (rf/dispatch [:channels/switch-named-tab :add])))


;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:check-authenticated])
  (rf/dispatch-sync [:get-categories])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
