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
            [o2sn.views.top-menu :as tmenu])
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

(defn page-trans [child]
  [ui/transition {:visible (not @(rf/subscribe [:page-hiding?]))
                  :animation "scale"
                  :duration 500
                  :transition-on-mount true
                  :mount-on-show true}
   [:div child]])

(defn login-page []
  [page-trans [login/login-form]])

(defn signup-page []
  [page-trans [signup/signup-form]])


(def pages
  {:home #'home/home-page
   :about #'about-page
   :login #'login-page
   :signup #'signup-page
   ;;:new-story #'new-story
   })

(defn page []
  [:div
   (let [page @(rf/subscribe [:page])]
     [(get pages page)])])

(defn require-login [page]
  (if @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-active-page page])
    (rf/dispatch [:set-active-page :login])))

(defn require-logout [page]
  (when-not @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-active-page page])))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (require-login :home))

(secretary/defroute "/welcome" []
  (require-logout :welcome))

#_(secretary/defroute "/story/new" []
  (require-login :new-story))

(secretary/defroute "/login" []
  (require-logout :login))

(secretary/defroute "/signup" []
  (require-logout :signup))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(secretary/defroute "/logout" []
  (when @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:logout])))

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
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
