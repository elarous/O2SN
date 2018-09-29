(ns o2sn.login.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [bidi.bidi :refer [path-for]]
            ["semantic-ui-react" :as ui]
            ["react-motion" :as motion]
            [o2sn.common.routes :refer [routes]]
            [o2sn.common.views :refer [error-label]]))


(defn login-username []
  (let [valid? @(rf/subscribe [:login/username-valid?])
        form-valid? (not @(rf/subscribe [:login/errors?]))]
    [:> ui/Form.Field
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:login/username-error])
                   :path-to-ctrl [:login-form :username]}]
     [:> ui/Form.Input
      {:fluid true
       :id "login-username"
       :icon (r/as-element
              [:> ui/Icon
               {:name "user"
                :color (if (and valid? form-valid?) "teal" "red")}])
       :icon-position "left"
       :loading @(rf/subscribe [:login/username-validating?])
       :autoFocus true
       :error (not (and valid? form-valid?))
       :value @(rf/subscribe [:login/username])
       :placeholder "Username"
       :on-focus #(.setSelectionRange (-> % .-target)
                                      (-> % .-target .-value .-length)
                                      (-> % .-target .-value .-length))
       :on-change #(do (rf/dispatch [:login/set-username
                                     (-> % .-target .-value)])
                       (rf/dispatch [:login/validate-username
                                     (-> % .-target .-value)]))}]]))

(defn login-password []
  (let [valid? @(rf/subscribe [:login/password-valid?])
        form-valid? (not @(rf/subscribe [:login/errors?]))]
    [:> ui/Form.Field
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:login/password-error])
                   :path-to-ctrl [:login-form :password]}]
     [:> ui/Form.Input
      {:fluid true
       :id "login-password"
       :icon (r/as-element
              [:> ui/Icon {:name "lock"
                        :color (if (and valid? form-valid?)
                                 "teal"
                                 "red")}])
       :icon-position "left"
       :loading @(rf/subscribe [:login/password-validating?])
       :autoFocus true
       :error (not (and valid? form-valid?))
       :value @(rf/subscribe [:login/password])
       :placeholder "Password"
       :type "password"
       :on-focus #(.setSelectionRange (-> % .-target)
                                      (-> % .-target .-value .-length)
                                      (-> % .-target .-value .-length))
       :on-change #(do (rf/dispatch [:login/set-password
                                     (-> % .-target .-value)])
                       (rf/dispatch [:login/validate-password
                                     (-> % .-target .-value)]))}]]))

(defn login-form []
  (if @(rf/subscribe [:common/checking-auth?])
    [:div.login-msg
     [:> ui/Message {:icon true
                     :info true}
      [:> ui/Icon {:name "circle notched" :loading true}]
      [:> ui/Message.Content
       [:> ui/Message.Header "Please Wait"]
       "Checking authentication"]]]
    (if-not @(rf/subscribe [:common/user])
      (let [form-errors? @(rf/subscribe [:login/errors?])]
        [:div.login-form
         [:> ui/Grid {:text-align "center"
                      :style {:height "100%"}
                      :vertical-align "middle"}
          [:> ui/Grid.Column {:style {:max-width 450}}
           [:> ui/Header {:as "h2" :text-align "center" :color "teal"}
            "Log to your account"]
           [:> ui/Form {:size "large"
                        :error form-errors?}
            [:> ui/Segment {:stacked true}
             [login-username]
             [login-password]
             [:> ui/Button
              {:color (if (and @(rf/subscribe [:login/button-enabled?])
                               (not form-errors?))
                        "teal" "red")
               :fluid true
               :disabled (not @(rf/subscribe [:login/button-enabled?]))
               :size "large"
               :loading @(rf/subscribe [:login/processing?])
               :on-click #(rf/dispatch [:login/login])}
              "Login"]]
            [:> ui/Transition {:visible @(rf/subscribe [:login/errors?])
                               :animation "scale"
                               :duration 500}
             [:> ui/Message {:error true
                             :header "Bad Credentials"
                             :content "Make sure you entered the correct username and password"}]]]
           [:> ui/Message "New to us? "
            [:a {:href (path-for routes :signup)} "Sign Up"]]]]])
      [:div.login-msg
       [:> ui/Message {:icon true
                       :success true}
        [:> ui/Icon {:name "check"}]
        [:> ui/Message.Content
         [:> ui/Message.Header "Success"]
         "You are now logged in."]]])))
