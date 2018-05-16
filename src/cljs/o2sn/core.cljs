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
  (:require-macros [o2sn.ui-macros :refer [mchild]])
  (:import goog.History))

(defn about-page []
  [:div "the about page"])

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

(defn error-label [valid? msg ctrl form]
  (let [[start-x end-x] (if valid? [35 0] [0 35])
        [start-scale end-scale] (if valid? [1 0] [0 1])
        preset (if valid? (clj->js {:stiffness 65
                                    :damping 17})
                   (.-wobbly ui/presets))
        activated? @(rf/subscribe [:form-control-activated? form ctrl])]
    (when (or activated? (not valid?))
      (rf/dispatch [:set-form-control-activated? form ctrl])
      [ui/motion {:default-style {:x start-x :s start-scale}
                  :style {:s (ui/spring end-scale preset)
                          :x (ui/spring end-x preset)}}
       (mchild
        {:opacity {:val :s :fn identity}
         :transform {:val :s :fn #(str "scale(" % "," % ")")}
         :height {:val :x :fn #(str % "px")}}
        {}
        [:div
         [:> ui/label {:basic true
                       :color (if valid? "teal" "red")
                       :pointing "below"}
          (if-not valid?
            msg
            "(^_^)'")]])])))

(defn login-username []
  (let [valid? @(rf/subscribe [:login-username-valid?])
        form-valid? (not @(rf/subscribe [:login-errors?]))]
    [:> ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:login-username-error])
      :username
      :login-form]
     [:> ui/form-input {:fluid true
                        :id "login-username"
                        :icon (r/as-element
                               [:> ui/icon {:name "user"
                                            :color (if (and valid? form-valid?)
                                                     "teal"
                                                     "red")}])
                        :icon-position "left"
                        :loading @(rf/subscribe [:login-username-validating?])
                        :autoFocus true
                        :error (not (and valid? form-valid?))
                        :value @(rf/subscribe [:login-username])
                        :placeholder "Username"
                        :on-focus #(.setSelectionRange (-> % .-target)
                                                       (-> % .-target .-value .-length)
                                                       (-> % .-target .-value .-length))
                        :on-change #(do (rf/dispatch [:set-login-username
                                                      (-> % .-target .-value)]-)
                                        (rf/dispatch [:validate-login-username
                                                      (-> % .-target .-value)]))}]]))

(defn login-password []
  (let [valid? @(rf/subscribe [:login-password-valid?])
        form-valid? (not @(rf/subscribe [:login-errors?]))]
    [:> ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:login-password-error])
      :password
      :login-form]
     [:> ui/form-input {:fluid true
                        :id "login-password"
                        :icon (r/as-element
                               [:> ui/icon {:name "lock"
                                            :color (if (and valid? form-valid?)
                                                     "teal"
                                                     "red")}])
                        :icon-position "left"
                        :loading @(rf/subscribe [:login-password-validating?])
                        :autoFocus true
                        :error (not (and valid? form-valid?))
                        :value @(rf/subscribe [:login-password])
                        :placeholder "Password"
                        :type "password"
                        :on-focus #(.setSelectionRange (-> % .-target)
                                                       (-> % .-target .-value .-length)
                                                       (-> % .-target .-value .-length))
                        :on-change #(do (rf/dispatch [:set-login-password
                                                      (-> % .-target .-value)])
                                        (rf/dispatch [:validate-login-password
                                                      (-> % .-target .-value)]))}]]))

(defn login-form []
  (let [form-errors? @(rf/subscribe [:login-errors?])]
    [:div.login-form
     [:> ui/grid {:text-align "center"
                  :style {:height "100%"}
                  :vertical-align "middle"}
      [:> ui/grid-column {:style {:max-width 450}}
       [:> ui/header {:as "h2" :text-align "center" :style {:color "white"}}
        "Log to your account"]
       [:> ui/form {:size "large"
                    :error form-errors?}
        [:> ui/segment {:stacked true}
         [login-username]
         [login-password]
         [:> ui/button
          {:color (if (and @(rf/subscribe [:login-button-enabled?])
                           (not form-errors?))
                    "teal"
                    "red")
           :fluid true
           :disabled (not @(rf/subscribe [:login-button-enabled?]))
           :size "large"
           :loading @(rf/subscribe [:login-processing?])
           :on-click #(rf/dispatch [:login])}
          "Login"]]
        [:> ui/message {:error true
                        :header "Bad Credentials"
                        :content "Make sure you entered the correct username and password"}]]
       [:> ui/message "New to us? "
        [:a {:href "/#/signup"} "Sign Up"]]]]]))

(defn signup-email []
  (let [valid? @(rf/subscribe [:signup-email-valid?])]
    [:> ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-email-error])
      :email
      :signup-form]
     [:> ui/form-input {:fluid true
                        :id "signup-email"
                        :icon (r/as-element [:> ui/icon {:name "mail"
                                                         :color (if valid?
                                                                  "teal"
                                                                  "red")}])
                        :icon-position "left"
                        :loading @(rf/subscribe [:signup-email-validating?])
                        :autoFocus true
                        :error (not valid?)
                        :value @(rf/subscribe [:signup-email])
                        :placeholder "E-mail address"
                        :on-focus #(.setSelectionRange (-> % .-target)
                                                       (-> % .-target .-value .-length)
                                                       (-> % .-target .-value .-length))
                        :on-blur #(if valid?
                                    (rf/dispatch [:validate-signup-email-ajax
                                                  (-> % .-target .-value)]))
                        :on-change #(do (rf/dispatch [:set-signup-email
                                                      (-> % .-target .-value)])
                                        (rf/dispatch [:validate-signup-email
                                                      (-> % .-target .-value)]))}]]))

(defn signup-username []
  (let [valid? @(rf/subscribe [:signup-username-valid?])]
    [:> ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-username-error])
      :username
      :signup-form]
     [:> ui/form-input {:fluid true
                        :id "signup-username"
                        :icon (r/as-element [:> ui/icon {:name "user"
                                                         :color (if valid?
                                                                  "teal"
                                                                  "red")}])
                        :icon-position "left"
                        :loading @(rf/subscribe [:signup-username-validating?])
                        :autoFocus true
                        :error (not valid?)
                        :value @(rf/subscribe [:signup-username])
                        :placeholder "Username"
                        :on-focus #(.setSelectionRange (-> % .-target)
                                                       (-> % .-target .-value .-length)
                                                       (-> % .-target .-value .-length))
                        :on-blur #(if valid?
                                    (rf/dispatch [:validate-signup-username-ajax
                                                  (-> % .-target .-value)]))
                        :on-change #(do (rf/dispatch [:set-signup-username
                                                      (-> % .-target .-value)])
                                        (rf/dispatch [:validate-signup-username
                                                      (-> % .-target .-value)]))}]]))

(defn signup-password []
  (let [valid? @(rf/subscribe [:signup-password-valid?])]

    [:> ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-password-error])
      :password
      :signup-form]
     [:> ui/form-input {:fluid true
                        :id "signup-password"
                        :icon (r/as-element [:> ui/icon {:name "lock"
                                                         :color (if valid?
                                                                  "teal"
                                                                  "red")}])
                        :icon-position "left"
                        :loading @(rf/subscribe [:signup-password-validating?])
                        :autoFocus true
                        :error (not valid?)
                        :value @(rf/subscribe [:signup-password])
                        :placeholder "Password"
                        :type "password"
                        :on-focus #(.setSelectionRange (-> % .-target)
                                                       (-> % .-target .-value .-length)
                                                       (-> % .-target .-value .-length))
                        :on-change #(do (rf/dispatch [:set-signup-password
                                                      (-> % .-target .-value)])
                                        (rf/dispatch [:validate-signup-password
                                                      (-> % .-target .-value)]))}]]))

(defn signup-repassword []
  (let [valid? @(rf/subscribe [:signup-repassword-valid?])]

    [:> ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-repassword-error])
      :repassword
      :signup-form]
     [:> ui/form-input {:fluid true
                        :id "signup-repassword"
                        :icon (r/as-element [:> ui/icon {:name "lock"
                                                         :color (if valid?
                                                                  "teal"
                                                                  "red")}])
                        :icon-position "left"
                        :loading @(rf/subscribe [:signup-repassword-validating?])
                        :autoFocus true
                        :error (not valid?)
                        :value @(rf/subscribe [:signup-repassword])
                        :placeholder "Confirm Password"
                        :type "password"
                        :on-focus #(.setSelectionRange (-> % .-target)
                                                       (-> % .-target .-value .-length)
                                                       (-> % .-target .-value .-length))
                        :on-change #(do (rf/dispatch [:set-signup-repassword
                                                      (-> % .-target .-value)])
                                        (rf/dispatch [:validate-signup-repassword
                                                      (-> % .-target .-value)]))}]]))

(defn signup-form []
  [:div.signup-form
   [:> ui/grid {:text-align "center"
                :style {:height "100%"}
                :vertical-align "middle"}
    [:> ui/grid-column {:style (if @(rf/subscribe [:signed-up?])
                                 {:max-width "50%"}
                                 {:max-width 450})}
     (if-not @(rf/subscribe [:signed-up?])
       [:div
        [:> ui/header {:as "h2"  :text-align "center" :style {:color "white"}}
         "Create your new account "]
        [:> ui/form {:size "large"
                     :error @(rf/subscribe [:signup-errors?])
                     :success true}
         [:> ui/segment {:stacked true}
          [signup-email]
          [signup-username]
          [signup-password]
          [signup-repassword]
          [:> ui/button
           {:color (if @(rf/subscribe [:signup-button-enabled])
                     "teal"
                     "red")
            :fluid true
            :disabled (not @(rf/subscribe [:signup-button-enabled]))
            :size "large"
            :loading @(rf/subscribe [:signup-processing?])
            :on-click #(rf/dispatch [:signup])}
           "Sign Up"]]
         [:> ui/transition {:visible @(rf/subscribe [:signup-errors?])
                            :animation "scale"
                            :duration 500}
          [:> ui/message {:error true
                          :header @(rf/subscribe [:signup-error-header])
                          :content @(rf/subscribe [:signup-error-msg])}]]]
        [:> ui/message "Already a member ? "
         [:a {:href "/#/login"} "Log In"]]]
       [ui/motion {:default-style {:x 0}
                   :style {:x (ui/spring 1
                                         (clj->js {:stiffness 80
                                                   :damping 12}))}}
        (mchild
         {:opacity {:val :x :fn identity}
          :transform {:val :x :fn #(str "scale(" % "," % ")")}}
         {}
         [:div
          [:> ui/message {:success true
                          :size "massive"
                          :icon "smile"
                          :header "Account is successfully created"
                          :content (r/as-element
                                    [:div
                                     [:p "A confirmation email has been sent to you"]
                                     [:a {:href "#/login"} "Login to your new account"]])}]])])]]])

(defn login-page []
  [:> ui/transition {:visible true
                     :animation "fade"
                     :duration 1300
                     :transition-on-mount true
                     :mount-on-show true}
   [:div
    [login-form]]])


(defn signup-page []
  [:> ui/transition {:visible true
                     :animation "fade"
                     :duration 1300
                     :transition-on-mount true
                     :mount-on-show true}
   [:div
    [signup-form]]])

(defn welcome-page []
  [:div "Welcome page"])

(def pages
  {:home #'home-page
   :about #'about-page
   :welcome #'welcome-page
   :login #'login-page
   :signup #'signup-page})

(defn page []
  [:div
   (let [page @(rf/subscribe [:page])]
     [(get pages page)])])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (when @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-active-page :home])))

(secretary/defroute "/welcome" []
  (when-not @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-active-page :welcome])))

(secretary/defroute "/login" []
  (when-not @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-active-page :login])))

(secretary/defroute "/signup" []
  (when-not @(rf/subscribe [:user-logged-in])
    (rf/dispatch [:set-active-page :signup])))

(secretary/defroute "/about" []
  (println "about")
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
