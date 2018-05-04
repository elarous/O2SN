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
            [o2sn.ui :as ui])
  (:import goog.History))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:br]
     [:> ui/button {:primary true} "OK"]]]])


(defn main-menu []
  [:> ui/menu {:size "large"
               :pointing true
               :stackable true
               :inverted false}
   [:> ui/menu-item {:name "logo"}]
   [:> ui/menu-item {:name "first-item"
                     :active true
                     :on-click #(js/alert "first item clicked !")}
    [:> ui/icon {:name "home"}]
    "News"
    [:> ui/label 3]]
   [:> ui/menu-item {:name "first-item"
                     :active false
                     :on-click #(js/alert "first item clicked !")}
    [:> ui/icon {:name "envelope"}]
    "Messages"
    [:> ui/label 1]]
   [:> ui/menu-item {:name "first-item"
                     :active false
                     :on-click #(js/alert "first item clicked !")}
    [:> ui/icon {:name "users"}]
    "Friends"
    [:> ui/label 5]]
   [:> ui/menu-item {:name "second-item"
                     :active false
                     :on-click #(js/alert "second item clicked !")}
    [:> ui/icon {:name "newspaper"}]
    "My Wall"]
   [:> ui/menu-menu {:position "right"}
    [:> ui/menu-item {:name "first-item"
                      :active false}
     [:> ui/input {:icon "search"
                   :placeholder "Search For News ..."}]]
    [:> ui/menu-item {:name "first-item"
                      :active false}
     [:> ui/button {:primary true}
      "Login"]]]])

(defn home-page []
  [:h1 "The Home Page"])

(defn login-form []
  [:div.login-form
   [:> ui/grid {:text-align "center"
                :style {:height "100%"}
                :vertical-align "middle"}
    [:> ui/grid-column {:style {:max-width 450}}
     [:> ui/header {:as "h2" :color "teal" :text-align "center"}
      "Log to your account"]
     [:> ui/form {:size "large"
                  :error @(rf/subscribe [:login-form-errors?])}
      [:> ui/segment {:stacked true}
       [:> ui/form-input {:fluid true
                          :icon "user"
                          :icon-position "left"
                          :placeholder "Username"
                          :error @(rf/subscribe [:login-form-errors?])
                          :value @(rf/subscribe [:login-form-username])
                          :on-change #(rf/dispatch [:set-login-form-username
                                                    (-> % .-target .-value)])}]
       [:> ui/form-input {:fluid true
                          :icon "lock"
                          :icon-position "left"
                          :placeholder "Password"
                          :type "password"
                          :error @(rf/subscribe [:login-form-errors?])
                          :value @(rf/subscribe [:login-form-password])
                          :on-change #(rf/dispatch [:set-login-form-password
                                                    (-> % .-target .-value)])}]
       [:> ui/button
        {:color "teal"
         :fluid true
         :size "large"
         :loading @(rf/subscribe [:login-form-processing?])
         :on-click #(rf/dispatch [:login
                                  @(rf/subscribe [:login-form-username])
                                  @(rf/subscribe [:login-form-password])])}
        "Login"]]
      [:> ui/message {:error true
                      :header "Bad Credentials"
                      :content "Make sure you entered the correct username and password"}]]
     [:> ui/message "New to us? "
      [:a {:href "/#/signup"} "Sign Up"]]]]])

(defn signup-form []
  [:div.signup-form
   [:> ui/grid {:text-align "center"
                :style {:height "100%"}
                :vertical-align "middle"}
    [:> ui/grid-column {:style {:max-width 450}}
     [:> ui/header {:as "h2" :color "teal" :text-align "center"}
      "Create your new account "]
     [:> ui/form {:size "large"
                  :error @(rf/subscribe [:login-form-errors?])}
      [:> ui/segment {:stacked true}
       [:> ui/form-input {:fluid true
                          :icon "at"
                          :icon-position "left"
                          :placeholder "E-mail address"}]
       [:> ui/form-input {:fluid true
                          :icon "user"
                          :icon-position "left"
                          :placeholder "Username"
                          :error @(rf/subscribe [:login-form-errors?])
                          :value @(rf/subscribe [:login-form-username])
                          :on-change #(rf/dispatch [:set-login-form-username
                                                    (-> % .-target .-value)])}]
       [:> ui/form-input {:fluid true
                          :icon "lock"
                          :icon-position "left"
                          :placeholder "Password"
                          :type "password"
                          :error @(rf/subscribe [:login-form-errors?])
                          :value @(rf/subscribe [:login-form-password])
                          :on-change #(rf/dispatch [:set-login-form-password
                                                    (-> % .-target .-value)])}]
       [:> ui/form-input {:fluid true
                          :icon "lock"
                          :icon-position "left"
                          :placeholder "Confirm Password"
                          :type "password"}]
       [:> ui/button
        {:color "teal"
         :fluid true
         :size "large"
         :loading @(rf/subscribe [:login-form-processing?])
         :on-click #(rf/dispatch [:login
                                  @(rf/subscribe [:login-form-username])
                                  @(rf/subscribe [:login-form-password])])}
        "Login"]]
      [:> ui/message {:error true
                      :header "Bad Credentials"
                      :content "Make sure you entered the correct username and password"}]]
     [:> ui/message "Already a member ? "
      [:a {:href "/#/login"} "Log In"]]]]])



(defn welcome-page []
  (if (= :login-form @(rf/subscribe [:welcome-form]))
    [login-form]
    [signup-form]))

(def pages
  {:home #'home-page
   :about #'about-page
   :welcome #'welcome-page})

(defn page []
  (if @(rf/subscribe [:user-logged-in])
    [:div
     (when-not (= :welcome @(rf/subscribe [:page]))
       [main-menu])
     [(pages @(rf/subscribe [:page]))]]
    [:div
     [welcome-page]]))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/welcome" []
  (when-not @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-active-page :welcome])))

(secretary/defroute "/login" []
  (when-not @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-welcome-form :login-form])))

(secretary/defroute "/signup" []
  (when-not @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-welcome-form :signup-form])))

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
