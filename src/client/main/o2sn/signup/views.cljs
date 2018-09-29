(ns o2sn.signup.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [bidi.bidi :refer [path-for]]
            [o2sn.common.routes :refer [routes]]
            [o2sn.common.views :refer [error-label]]))


(defn signup-email []
  (let [valid? @(rf/subscribe [:signup/email-valid?])]
    [:> ui/Form.Field
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:signup/email-error])
                   :path-to-ctrl [:signup-form :email]}]
     [:> ui/Form.Input
      {:fluid true
       :id "signup-email"
       :icon (r/as-element
              [:> ui/Icon {:name "mail"
                        :color (if valid? "teal" "red")}])
       :icon-position "left"
       :loading @(rf/subscribe [:signup/email-validating?])
       :autoFocus true
       :error (not valid?)
       :value @(rf/subscribe [:signup/email])
       :placeholder "E-mail address"
       :on-focus #(.setSelectionRange (-> % .-target)
                                      (-> % .-target .-value .-length)
                                      (-> % .-target .-value .-length))
       :on-blur #(if valid?
                   (rf/dispatch [:signup/validate-email-available?
                                 (-> % .-target .-value)]))
       :on-change #(do (rf/dispatch [:signup/set-email
                                     (-> % .-target .-value)])
                       (rf/dispatch [:signup/validate-email
                                     (-> % .-target .-value)]))}]]))

(defn signup-username []
  (let [valid? @(rf/subscribe [:signup/username-valid?])]
    [:> ui/Form.Field
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:signup/username-error])
                   :path-to-ctrl [:signup-form :username]}]
     [:> ui/Form.Input
      {:fluid true
       :id "signup-username"
       :icon (r/as-element [:> ui/Icon
                            {:name "user"
                             :color (if valid? "teal" "red")}])
       :icon-position "left"
       :loading @(rf/subscribe [:signup/username-validating?])
       :autoFocus true
       :error (not valid?)
       :value @(rf/subscribe [:signup/username])
       :placeholder "Username"
       :on-focus #(.setSelectionRange (-> % .-target)
                                      (-> % .-target .-value .-length)
                                      (-> % .-target .-value .-length))
       :on-blur #(if valid?
                   (rf/dispatch [:signup/validate-username-available?
                                 (-> % .-target .-value)]))
       :on-change #(do (rf/dispatch [:signup/set-username
                                     (-> % .-target .-value)])
                       (rf/dispatch [:signup/validate-username
                                     (-> % .-target .-value)]))}]]))

(defn signup-password []
  (let [valid? @(rf/subscribe [:signup/password-valid?])]
    [:> ui/Form.Field
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:signup/password-error])
                   :path-to-ctrl [:signup-form :password]}]
     [:> ui/Form.Input
      {:fluid true
       :id "signup-password"
       :icon (r/as-element [:> ui/Icon
                            {:name "lock"
                             :color (if valid? "teal" "red")}])
       :icon-position "left"
       :loading @(rf/subscribe [:signup/password-validating?])
       :autoFocus true
       :error (not valid?)
       :value @(rf/subscribe [:signup/password])
       :placeholder "Password"
       :type "password"
       :on-focus #(.setSelectionRange (-> % .-target)
                                      (-> % .-target .-value .-length)
                                      (-> % .-target .-value .-length))
       :on-change #(do (rf/dispatch [:signup/set-password
                                     (-> % .-target .-value)])
                       (rf/dispatch [:signup/validate-password
                                     (-> % .-target .-value)]))}]]))

(defn signup-repassword []
  (let [valid? @(rf/subscribe [:signup/repassword-valid?])]
    [:> ui/Form.Field
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:signup/repassword-error])
                   :path-to-ctrl [:signup-form :repassword]}]
     [:> ui/Form.Input
      {:fluid true
       :id "signup-repassword"
       :icon (r/as-element [:> ui/Icon
                            {:name "lock"
                             :color (if valid? "teal" "red")}])
       :icon-position "left"
       :loading @(rf/subscribe [:signup/repassword-validating?])
       :autoFocus true
       :error (not valid?)
       :value @(rf/subscribe [:signup/repassword])
       :placeholder "Confirm Password"
       :type "password"
       :on-focus #(.setSelectionRange (-> % .-target)
                                      (-> % .-target .-value .-length)
                                      (-> % .-target .-value .-length))
       :on-change #(do (rf/dispatch [:signup/set-repassword
                                     (-> % .-target .-value)])
                       (rf/dispatch [:signup/validate-repassword
                                     (-> % .-target .-value)]))}]]))

(defn signup-form []
  [:div.signup-form
   [:> ui/Grid {:text-align "center"
             :style {:height "100%"}
             :vertical-align "middle"}
    [:> ui/Grid.Column {:style (if @(rf/subscribe [:signup/signed-up?])
                              {:max-width "50%"}
                              {:max-width 450})}
     (if-not @(rf/subscribe [:signup/signed-up?])
       [:div
        [:> ui/Header {:as "h2"  :text-align "center" :color "teal"}
         "Create your new account "]
        [:> ui/Form {:size "large"
                  :error @(rf/subscribe [:signup/errors?])
                  :success true}
         [:> ui/Segment {:stacked true}
          [signup-email]
          [signup-username]
          [signup-password]
          [signup-repassword]
          [:> ui/Button
           {:color (if @(rf/subscribe [:signup/button-enabled]) "teal" "red")
            :fluid true
            :disabled (not @(rf/subscribe [:signup/button-enabled]))
            :size "large"
            :loading @(rf/subscribe [:signup/processing?])
            :on-click #(rf/dispatch [:signup/signup])}
           "Sign Up"]]
         [:> ui/Transition {:visible @(rf/subscribe [:signup/errors?])
                         :animation "scale"
                         :duration 500}
          [:> ui/Message {:error true
                       :header @(rf/subscribe [:signup/error-header])
                       :content @(rf/subscribe [:signup/error-msg])}]]]
        [:> ui/Message "Already a member ? "
         [:a {:href (path-for routes :login)} "Log In"]]]
       [:> ui/Message
        {:success true
         :size "massive"
         :icon "smile"
         :header "Account is successfully created"
         :content (r/as-element
                   [:div
                    [:p "A confirmation email has been sent to you"]
                    [:a {:href "#/login"} "Login to your new account"]])}])]]])
