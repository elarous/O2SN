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

;; helper functions

(defn format-date [date]
  (str (:day date) "/" (:month date) "/" (:year date)
       " "
       (if (< (:hour date) 10) (str "0" (:hour date)) (:hour date))
       ":"
       (if (< (:minute date) 10) (str "0" (:minute date)) (:minute date))))

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

(defn top-menu-sidebar-btn []
  [:div
   [ui/icon {:name "sidebar"
             :size "large"
             :link true
             :on-click #(rf/dispatch [:toggle-sidebar])}]])

(defn top-menu-logo []
  [:div
   [:h4 "O2SN LOGO"]])

(defn top-menu-search []
  [:div
   [ui/search {:class-name "top-menu-search"
               :size "small"}]])

(defn top-menu-feed []
  [ui/popup {:hoverable true
             :position "bottom right"
             :style {:height "auto"}
             :trigger (r/as-element
                       [:span.menu-action
                        [ui/icon {:name "feed"
                                  :size "large"
                                  :link true}]])}
   [ui/feed
    [ui/feed-event {:icon "pencil"
                    :date "Today"
                    :summary "my first event ever"}]
    [ui/feed-event {:icon "pencil"
                    :date "Today"
                    :summary "my second event ever"}]]])


(defn top-menu-messages []
  [ui/popup {:hoverable true
             :position "bottom right"
             :style {:height "auto"}
             :trigger (r/as-element
                       [:span.menu-action
                        [ui/icon {:name "envelope"
                                  :size "large"
                                  :link true}]])}
   [ui/feed
    [ui/feed-event {:image "img/myAvatar.svg"
                    :content "my first message"}]
    [ui/feed-event {:image "img/myAvatar.svg"
                    :content "my second message"}]]])

(defn top-menu-add []
  [ui/dropdown
   {:icon false
    :pointing "top right"
    :trigger (r/as-element
              [ui/icon {:name  "plus"
                        :size "large"}])}
   [ui/dropdown-menu
    [ui/dropdown-item {:icon "file"
                       :text "New Story"
                       :on-click #(secretary/dispatch! "#/story/new")}]]])


(defn top-menu-actions []
  [:div.top-menu-actions
   [top-menu-add]
   [top-menu-feed]
   [top-menu-messages]
   [:span.menu-action
    [ui/dropdown
     {:icon false
      :pointing "top right"
      :trigger (r/as-element
                [ui/image {:src "img/myAvatar.svg"
                           :avatar true
                           :class-name "top-menu-avatar"}])}
     [ui/dropdown-menu
      [ui/dropdown-item {:icon "user"
                         :text "my profile"
                         :on-click #(js/alert "to my profile ... ")}]
      [ui/dropdown-item {:icon "sign out"
                         :text "logout"
                         :on-click #(js/alert "logging out ...")}]]]]])

(defn main-menu []
  [ui/segment {:class-name "top-menu"}
   [ui/grid {:columns 16
             :vertical-align "middle"
             :padded "horizontally"}
    [ui/grid-column {:width 2}
     [top-menu-sidebar-btn]]

    [ui/grid-column {:width 2}
     [top-menu-logo]]

    [ui/grid-column {:width 9}
     [top-menu-search]]

    [ui/grid-column {:width 3}
     [top-menu-actions]]]])

;; story details modal
(defn card-modal-header [story]
  (let [marked-truth @(rf/subscribe [:marked-story-truth? (:_key story)])
        marked-lie @(rf/subscribe [:marked-story-lie? (:_key story)])]
    [ui/modal-header
     [:div
      (cond
        marked-truth [ui/label {:ribbon "right"
                                :color "green"}
                      "Marked as Truth"]
        marked-lie [ui/label {:ribbon "right"
                              :color "red"}
                    "Marked as Lie"]
        :else [ui/label {:ribbon "right"
                         :color "grey"}
               "Not Marked"])]
     (:title story)
     [ui/label {:color @(rf/subscribe [:category-color
                                       (get-in story [:category :name])])}
      (get-in story [:category :name])]]))

(defn card-modal-imgs [story]
  [ui/grid
   [ui/grid-column {:width 1
                    :vertical-align "middle"}
    [ui/icon {:name "chevron left"
              :link true
              :size "big"
              :on-click #(rf/dispatch [:previous-story-modal-img])}]]
   [ui/grid-column {:width 14
                    :text-align "center"}
    [ui/image {:src (str "img/" @(rf/subscribe [:story-modal-img]))
               :style {:display "inline"}}]]
   [ui/grid-column {:width 1
                    :vertical-align "middle"}
    [ui/icon {:name "chevron right"
              :link true
              :size "big"
              :on-click #(rf/dispatch [:next-story-modal-img])}]]])

(defn wrapped-map [{:keys [long lat]}]
  (let [map-class
        (r/adapt-react-class
         (ui/with-scriptjs
           (ui/with-google-map
             (fn [p]
               (r/create-element
                ui/google-map
                #js {:defaultZoom 10
                     :defaultCenter #js {:lat lat, :lng long}}
                (r/as-element [ui/marker {:position {:lat lat :lng long}}]))))))]
    [map-class
     {:container-element (r/as-element [:div {:style {:height "400px"}}])
      :map-element (r/as-element [:div {:style {:height "100%"}}])
      :google-map-URL "https://maps.googleapis.com/maps/api/js?key=AIzaSyBUGwGf5iRDVzcJ-22B-JhzpTrCA2FMW1o&v=3.exp&libraries=geometry,drawing,places"
      :loading-element (r/as-element [:div {:style {:height "100%"}}])}]))

(defn card-modal-map [story]
  [ui/grid
   [ui/grid-column {:width 1}]
   [ui/grid-column {:width 14
                    :text-align "center"}
    [:div#map
     [wrapped-map {:long (get-in story [:location :long])
                   :lat (get-in story [:location :lat])}]]]
   [ui/grid-column {:width 1}]])

(defn card-modal-date [story]
  [ui/button {:icon true
              :label-position "left"
              :color "teal"
              :size "small"}
   [ui/icon {:name "calendar"}]
   (format-date (:date story))])

(defn card-modal-location [story]
  (if-not @(rf/subscribe [:story-map-visible?])
    [ui/button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:toggle-map-visiblity])}
     [ui/icon {:name "world"}]
     "View On Map"]
    [ui/button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:toggle-map-visiblity])}
     [ui/icon {:name "picture"}]
     "View Pictures"]))

(defn card-modal-description [story]
  [ui/segment {:stacked true}
   (:description story)])

(defn card-modal-truth [story]
  [ui/popup
   {:hoverable true
    :trigger
    (r/as-element
     [ui/button {:as "div"
                 :active false
                 :label-position "right"
                 :on-click #(rf/dispatch [:toggle-truth-story (:_key story)])}
      [ui/button {:color (if @(rf/subscribe [:marked-story-truth? (:_key story)])
                           "green"
                           "grey")
                  :active false}
       [ui/icon {:name "check"}]
       "Truth"]
      [ui/label {:as "a"
                 :basic true
                 :color (if @(rf/subscribe [:marked-story-truth? (:_key story)])
                          "green"
                          "grey")
                 :pointing "left"}
       (count (:truth story))]])}
   [ui/list {:vertical-align "middle"}
    (for [u (:truth story)]
      ^{:key (:_key u)}
      [ui/list-item
       [ui/image {:src "img/myAvatar.svg" :avatar true}]
       [ui/list-content
        [ui/list-header (:username u)]]])]])

(defn card-modal-lie [story]
  [ui/popup
   {:hoverable true
    :trigger
    (r/as-element
     [ui/button {:as "div"
                 :label-position "right"
                 :on-click #(rf/dispatch [:toggle-lie-story (:_key story)])}
      [ui/button {:color (if @(rf/subscribe [:marked-story-lie? (:_key story)])
                           "red"
                           "grey")}
       [ui/icon {:name "x"}]
       "Lie"]
      [ui/label {:as "a"
                 :basic true
                 :color (if @(rf/subscribe [:marked-story-lie? (:_key story)])
                          "red"
                          "grey")
                 :pointing "left"}
       (count (:lie story))]])}
   [ui/list {:vertical-align "middle"}
    (for [u (:lie story)]
      ^{:key (:_key u)}
      [ui/list-item
       [ui/image {:src "img/myAvatar.svg" :avatar true}]
       [ui/list-content
        [ui/list-header (:username u)]]])]])

(defn card-modal [story]
  [ui/modal {:open @(rf/subscribe [:story-modal-visible?])}
   [card-modal-header story]
   [ui/modal-content {:scrolling true}
    (if @(rf/subscribe [:story-map-visible?])
      [card-modal-map story]
      [card-modal-imgs story])
    [ui/grid {:style {:margin-bottom "5px"}}
     [ui/grid-column {:width 8
                      :text-align "center"}
      [card-modal-date story]]
     [ui/grid-column {:width 8
                      :text-align "center"}
      [card-modal-location story]]]
    [ui/modal-description
     [card-modal-description story]
     [ui/grid
      [ui/grid-column {:width 8
                       :text-align "center"}
       [card-modal-truth story]]
      [ui/grid-column {:width 8
                       :text-align "center"}
       [card-modal-lie story]]]]]
   [ui/modal-actions
    [ui/button {:primary true
                :icon "x"
                :content "Close"
                :on-click #(rf/dispatch [:hide-story-modal])}]]])

(defn card-likes-modal []
  [ui/modal {:open @(rf/subscribe [:story-like-modal-visible])
             :close-icon true
             :size "mini"
             :on-close #(rf/dispatch [:hide-story-users-like-modal])}
   [ui/modal-content
    [ui/list {:selection true
              :vertical-align "middle"}
     (for [u @(rf/subscribe [:story-like-modal-users])]
       ^{:key (:_key u)}
       [ui/list-item
        [ui/image {:src "img/myAvatar.svg"
                   :avatar true
                   :size "mini"}]
        [ui/list-content
         [ui/list-header
          [ui/header {:as "h3"}
           (:username u)]]]])]]])



(defn news-card [story]
  [:div#news-card
   [ui/card {:color @(rf/subscribe [:category-color
                                    (get-in story [:category :name])])}
    [ui/card-content
     [ui/label {:corner "right"
                :as "a"
                :on-click #(rf/dispatch [:show-story-modal (:_key story)])
                :icon
                (r/as-element
                 [ui/icon {:name "content"
                           :link true}])}]
     [ui/label {:ribbon true
                :color @(rf/subscribe [:category-color
                                      (get-in story [:category :name])])}
      (get-in story [:category :name])]
     [ui/card-header (:title story)]
     [ui/card-meta
      [ui/grid {:columns 16}
       [ui/grid-column {:width 7}
        [:span {:style {:font-size ".7rem"}}
         (format-date (:date story))]]
       [ui/grid-column {:width 7}
        [:span {:style {:font-size ".7rem"}} (get-in story [:location :name])]]]]
     [ui/card-description (:description story)]]
    [ui/card-content {:extra true}
     [ui/grid {:vertical-align "middle"
               :text-align "center"}
      [ui/grid-column {:width 4}
       [ui/icon {:name "thumbs up"
                 :link true
                 :color (if @(rf/subscribe [:like-story? (:_key story)]) "green" "grey")
                 :on-click #(rf/dispatch [:toggle-like-story (:_key story)])}]
       [:a {:on-click #(rf/dispatch
                        [:show-story-users-like-modal (:likes story)])}
        (count (:likes story))]]
      [ui/grid-column {:width 4}
       [ui/icon {:name "thumbs down"
                 :link true
                 :color (if @(rf/subscribe [:dislike-story? (:_key story)]) "red" "grey")
                 :on-click #(rf/dispatch [:toggle-dislike-story (:_key story)])}]
       [:a {:on-click #(rf/dispatch
                        [:show-story-users-like-modal (:dislikes story)])}
        (count (:dislikes story))]]]]]])

(defn chan-contents []
  [:div#news-cards
   [card-likes-modal]
   [card-modal @(rf/subscribe [:current-story])]
   (for [story @(rf/subscribe [:stories])]
     ^{:key (:_key story)}
     [news-card story])])

(defn chan-select []
  [ui/dropdown
   {:placeholder "Select a subscription channel"
    :search true
    :selection true
    :fluid true
    :on-change (fn [_ data]
                 (rf/dispatch [:set-selected-user-chan
                               (-> data .-value)]))
    :on-focus #(rf/dispatch [:load-user-channels])
    :options (clj->js @(rf/subscribe [:user-channels]))}])

(defn home-main []
  [:div
   [ui/grid {:columns 16
             :vertical-align "middle"
             :text-align "center"}
    [ui/grid-column {:width 4}
     [ui/header {:as "h3"} "News Channel : "]]
    [ui/grid-column {:width 9}
     [chan-select]]]
   [chan-contents]])

(defn home-page []
  [:div
   [main-menu]
   [ui/sidebar-pushable {:as (ui/component "Segment")}
    [ui/sidebar {:as (ui/component "Menu")
                 :animation "push"
                 :width "thin"
                 :visible @(rf/subscribe [:sidebar-visible])
                 :icon "labeled"
                 :vertical true
                 :color "teal"
                 :inverted true}
     [ui/menu-item {:name "home"
                    :link true}
      [ui/icon {:name "home"}]
      "Home"]
     [ui/menu-item {:name "subscriptions"
                    :link true}
      [ui/icon {:name "podcast"}]
      "Subscriptions"]
     [ui/menu-item {:name "messages"
                    :link true}
      [ui/icon {:name "envelope"}]
      "Messages"]

     [ui/menu-item {:name "friends"
                    :link true}
      [ui/icon {:name "users"}]
      "Friends"]

     [ui/menu-item {:name "settings"
                    :link true}
      [ui/icon {:name "settings"}]
      "Settings"]]
    [ui/sidebar-pusher
     [ui/segment {:basic true}
      [home-main]]]]])

(defn error-label [valid? msg ctrl form]
  (let [[start-x end-x] (if valid? [35 0] [0 35])
        [start-scale end-scale] (if valid? [1 0] [0 1])
        anim-opts (if valid? (clj->js {:stiffness 65
                                       :damping 17})
                      (.-wobbly ui/presets))
        activated? @(rf/subscribe [:form-control-activated? form ctrl])]
    (when (or activated? (not valid?))
      (rf/dispatch [:set-form-control-activated? form ctrl])
      [ui/motion {:default-style {:x start-x :s start-scale}
                  :style {:s (ui/spring end-scale anim-opts)
                          :x (ui/spring end-x anim-opts)}}
       (fn [v]
         (r/as-element
          [:div {:style {:transform (str "scale(" (.-s v) "," (.-s v) ")")
                         :opacity (.-s v)
                         :height (str (.-x v) "px")}}
           [ui/label {:basic true
                      :color (if valid? "teal" "red")
                      :pointing "below"}
            (if-not valid? msg "(^ _ ^)'")]]))])))

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
        [:a {:href "/#/signup"} "Sign Up"]]]]]))

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

(defn page-trans [child]
  [ui/transition {:visible (not @(rf/subscribe [:page-hiding?]))
                  :animation "scale"
                  :duration 500
                  :transition-on-mount true
                  :mount-on-show true}
   [:div child]])

(defn login-page []
  [page-trans [login-form]])

(defn signup-page []
  [page-trans [signup-form]])

(defn welcome-page []
  [:div "Welcome page"])


#_(defn new-story []
  [:div
   [ui/header {:as "h3"} "Publish A New Story"]
   [ui/form
    [ui/form-field
     [:label "Story's title"]
     [ui/form-input {:placeholder "Title"
                     :type "text"}]]
    [ui/form-field
     [:label "Story's Date"]
     [ui/grid {:columns 16}
      [ui/grid-column {:width 8}
       [ui/form-input {:type "date"}]]
      [ui/grid-column {:width 8}
       [ui/grid
        [ui/grid-column {:width 8}
         [ui/form-input {:type "number"
                         :placeholder "hh"}]]
        [ui/grid-column {:width 8}
         [ui/form-input {:type "number"
                         :placeholder "mm"}]]]]]]
    [ui/form-field
     [:label "Story's location"]
     [ui/button {:icon true
                 :color "teal"
                 :label-position "left"}
      [ui/icon {:name "world"}]
      "where ?"]]
    [ui/form-field
     [:label "Story's Contents"]
     [ui/textarea]]
    [ui/form-field
     [ui/button {:color "teal"
                 :content "OK"}]]]])

(def pages
  {:home #'home-page
   :about #'about-page
   :welcome #'welcome-page
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
