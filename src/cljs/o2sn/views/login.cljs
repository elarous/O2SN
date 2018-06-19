(ns o2sn.views.login
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [o2sn.ui :as ui]
            [o2sn.views.forms :refer [error-label]]))



(defn login-username []
  (let [valid? @(rf/subscribe [:login-username-valid?])
        form-valid? (not @(rf/subscribe [:login-errors?]))]
    [ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:login-username-error])
      :username
      :login-form]
     [ui/form-input
      {:fluid true
       :id "login-username"
       :icon (r/as-element
              [ui/icon
               {:name "user"
                :color (if (and valid? form-valid?) "teal" "red")}])
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
                                     (-> % .-target .-value)] -)
                       (rf/dispatch [:validate-login-username
                                     (-> % .-target .-value)]))}]]))

(defn login-password []
  (let [valid? @(rf/subscribe [:login-password-valid?])
        form-valid? (not @(rf/subscribe [:login-errors?]))]
    [ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:login-password-error])
      :password
      :login-form]
     [ui/form-input
      {:fluid true
       :id "login-password"
       :icon (r/as-element
              [ui/icon {:name "lock"
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
  (if @(rf/subscribe [:checking-auth?])
    [:div.login-msg
     [ui/message {:icon true
                  :info true}
      [ui/icon {:name "circle notched" :loading true}]
      [ui/message-content
       [ui/message-header "Please Wait"]
       "Checking authentication"]]]
    (if-not @(rf/subscribe [:user-logged-in])
      (let [form-errors? @(rf/subscribe [:login-errors?])]
        [:div.login-form
         [ui/grid {:text-align "center"
                   :style {:height "100%"}
                   :vertical-align "middle"}
          [ui/grid-column {:style {:max-width 450}}
           [ui/header {:as "h2" :text-align "center" :color "teal"}
            "Log to your account"]
           [ui/form {:size "large"
                     :error form-errors?}
            [ui/segment {:stacked true}
             [login-username]
             [login-password]
             [ui/button
              {:color (if (and @(rf/subscribe [:login-button-enabled?])
                               (not form-errors?))
                        "teal" "red")
               :fluid true
               :disabled (not @(rf/subscribe [:login-button-enabled?]))
               :size "large"
               :loading @(rf/subscribe [:login-processing?])
               :on-click #(rf/dispatch [:login])}
              "Login"]]
            [ui/transition {:visible @(rf/subscribe [:login-errors?])
                            :animation "scale"
                            :duration 500}
             [ui/message {:error true
                          :header "Bad Credentials"
                          :content "Make sure you entered the correct username and password"}]]]
           [ui/message "New to us? "
            [:a {:href "/#/signup"} "Sign Up"]]]]])
      [:div.login-msg
       [ui/message {:icon true
                    :success true}
        [ui/icon {:name "check"}]
        [ui/message-content
         [ui/message-header "Success"]
         "You are now logged in."]]])))


