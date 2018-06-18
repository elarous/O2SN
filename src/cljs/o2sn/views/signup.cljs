(ns o2sn.views.signup
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [o2sn.ui :as ui]
            [o2sn.views.forms :refer [error-label]]))


(defn signup-email []
  (let [valid? @(rf/subscribe [:signup-email-valid?])]
    [ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-email-error])
      :email
      :signup-form]
     [ui/form-input
      {:fluid true
       :id "signup-email"
       :icon (r/as-element
              [ui/icon {:name "mail"
                        :color (if valid? "teal" "red")}])
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
    [ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-username-error])
      :username
      :signup-form]
     [ui/form-input
      {:fluid true
       :id "signup-username"
       :icon (r/as-element [ui/icon
                            {:name "user"
                             :color (if valid? "teal" "red")}])
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
    [ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-password-error])
      :password
      :signup-form]
     [ui/form-input
      {:fluid true
       :id "signup-password"
       :icon (r/as-element [ui/icon
                            {:name "lock"
                             :color (if valid? "teal" "red")}])
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
    [ui/form-field
     [error-label
      valid?
      @(rf/subscribe [:signup-repassword-error])
      :repassword
      :signup-form]
     [ui/form-input
      {:fluid true
       :id "signup-repassword"
       :icon (r/as-element [ui/icon
                            {:name "lock"
                             :color (if valid? "teal" "red")}])
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
   [ui/grid {:text-align "center"
             :style {:height "100%"}
             :vertical-align "middle"}
    [ui/grid-column {:style (if @(rf/subscribe [:signed-up?])
                              {:max-width "50%"}
                              {:max-width 450})}
     (if-not @(rf/subscribe [:signed-up?])
       [:div
        [ui/header {:as "h2"  :text-align "center" :color "teal"}
         "Create your new account "]
        [ui/form {:size "large"
                  :error @(rf/subscribe [:signup-errors?])
                  :success true}
         [ui/segment {:stacked true}
          [signup-email]
          [signup-username]
          [signup-password]
          [signup-repassword]
          [ui/button
           {:color (if @(rf/subscribe [:signup-button-enabled]) "teal" "red")
            :fluid true
            :disabled (not @(rf/subscribe [:signup-button-enabled]))
            :size "large"
            :loading @(rf/subscribe [:signup-processing?])
            :on-click #(rf/dispatch [:signup])}
           "Sign Up"]]
         [ui/transition {:visible @(rf/subscribe [:signup-errors?])
                         :animation "scale"
                         :duration 500}
          [ui/message {:error true
                       :header @(rf/subscribe [:signup-error-header])
                       :content @(rf/subscribe [:signup-error-msg])}]]]
        [ui/message "Already a member ? "
         [:a {:href "/#/login"} "Log In"]]]
       [ui/message
        {:success true
         :size "massive"
         :icon "smile"
         :header "Account is successfully created"
         :content (r/as-element
                   [:div
                    [:p "A confirmation email has been sent to you"]
                    [:a {:href "#/login"} "Login to your new account"]])}])]]])



