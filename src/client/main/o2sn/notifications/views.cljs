(ns o2sn.notifications.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [bidi.bidi :refer [path-for]]
            [o2sn.common.routes :refer [routes]]))

(def green "#E0F2F1")
(def green-f "#1B5E20")
(def red "#FFEBEE")
(def red-f "#B71C1C")

(defn get-color [t]
  (let [type (keyword t)]
    (cond (contains? #{:new-story :like :truth} type)
          [green green-f]
          (contains? #{:dislike :lie} type)
          [red red-f]
          :else "")))

(defn make-msg [msg]
  (let [user-k (get-in msg [:by :_key])
        username [:a {:href (path-for routes :profile :user user-k)}
                  (str "@" (get-in msg [:by :username]))]
        target (:target msg)
        target-url (if target
                     (path-for routes :view-story :story
                               (get-in msg [:target :_key]))
                     "#")
        story-title [:a {:href target-url
                         :on-click #(rf/dispatch [:notifs/notif-click msg])
                         :style {:color (second (get-color (:type msg)))
                                 :font-weight "bold"}}
                     (get-in msg [:target :title])]]
    (case (keyword (:type msg))
      :like [:span username " Liked Story " story-title]
      :dislike [:span username " Disliked Story " story-title]
      :truth [:span username " Marked As Truth Story " story-title]
      :lie [:span username " Marked As Lie Story " story-title]
      :new-story [:span "New Story by " username " : "  story-title]
      "A New Notification")))

(defn make-msg-toast [msg]
  (case (keyword (:type msg))
    :like "Someone Liked A Relevant Story"
    :dislike "Someone Disliked A Relevant Story"
    :truth "Someone Marked As Truth A Relevant Story"
    :lie "Someone Marked As Lie A Relevant Story"
    :new-story "Someone Added A New Story"
    "A New Notification"))

(defn notif-segment [msg]
  [:> ui/Segment {:compact false
                  :style {:padding "3px"
                          :background-color (first (get-color (:type msg)))
                          :cursor "pointer"}}
   [:> ui/Grid
    [:> ui/Grid.Column {:width 4
                        :vertical-align "middle"
                        :text-align "center"}
     [:> ui/Image {:src "img/user.svg"
                   :avatar true
                   :style {:min-height "50px"
                           :min-width "50px"}}]]
    [:> ui/Grid.Column {:width 12
                        :text-align "left"
                        :vertical-align "middle"}
     [:span (make-msg msg)]]]])

(defn menu-notifications []
  (let [notifs @(rf/subscribe [:notifs/unreads])]
    (if (pos? (count notifs))
      [:div
       [:> ui/Container {:text-align "center"
                         :style {:margin-bottom "3px"}}
        [:> ui/Button {:as "div"
                       :label-position "right"
                       :size "mini"}
         [:> ui/Button {:color "teal"
                        :size "mini"
                        :on-click #(rf/dispatch [:notifs/mark-read-all])}
          [:> ui/Icon {:name "check"}]
          "Mark All As Read"]
         [:> ui/Label {:as "div"
                       :basic true
                       :color "teal"
                       :pointing "left"}
          (count notifs)]]]
       [:div {:style {:overflow-y "auto"
                      :overflow-x "hidden"
                      :min-width "180px"
                      :max-height "300px"
                      :padding-right "6px"}}
        (for [n notifs]
          ^{:key (:_key n)}
          [notif-segment n])]]
      [:div
       [:> ui/Header {:as "h5"
                      :color "grey"
                      :content "You don't have new notifications"}]])))

(defn notifications-page []
  [:> ui/Segment
   [:> ui/Container {:text-align "center"}
    [:> ui/Header {:as "h1"
                   :color "teal"
                   :style {:display "inline"}}
     [:> ui/Icon {:name "bell"}]
     [:> ui/Header.Content "All Notifications"]]]
   [:> ui/Divider]
   [:div {:style {:display "flex"
                  :flex-flow "row wrap"
                  :justify-content "center"}}
    (let [all-notifs @(rf/subscribe [:notifs/all])]
      (if (seq all-notifs)
        (for [notif all-notifs]
          ^{:key (:_key notif)}
          [:div {:style {:margin "15px"
                         :max-width "15%"}}
           [notif-segment notif]])
        [:> ui/Header {:as "h3"
                       :color "grey"
                       :content "No Notifications Available Yet"}]))]])

;; notification alert

(defn notifications-alert []
  [:div {:style {:position "absolute"
                 :bottom "10px"
                 :height "inherit"
                 :right "10px"
                 :width "400px"
                 :display "flex"
                 :flex-direction "column-reverse"
                 :background-color "transparent"
                 :pointer-events "none"}}
   (for [alert @(rf/subscribe [:notifs/alerts])]
     ^{:key (:_key alert)}
     [:div {:on-click #(rf/dispatch [:notifs/remove-alert (:_key alert)])
            :on-mouse-over #(rf/dispatch [:notifs/pause-alert (:_key alert)])
            :style {:height "150px"
                    :width "100%"
                    :min-width "300px"
                    :background-color "#616161"
                    :padding "15px"
                    :margin-top "10px"
                    :border-radius "5px"
                    :z-index "9000"
                    :pointer-events "auto"
                    :box-shadow "-2px -2px 10px 2px #E0E0E0"}}
      [:div {:style {:display "grid"
                     :grid-template-columns "20% 80%"
                     :grid-column-gap "10px"
                     :height "100%"}}
       [:div {:style {:display "flex"
                      :flex-direction "column"
                      :justify-content "center"}}
        [:> ui/Image {:src "img/user.svg"
                      :avatar true
                      :size "big"}]]
       [:div {:style {:display "flex"
                      :flex-direction "column"
                      :justify-content "center"}}
        [:> ui/Header {:as "h2"
                       :style {:color "white"}}
         (:header alert)]
        [:div {:style {:font-size "1.2rem"
                       :margin-right "5px"}}
         [:a {:href (path-for routes :profile :user (get-in alert [:by :_key]))
              :style {:color "#B2DFDB"}}
          (get-in alert [:by :username])]
         [:span {:style {:color "#64FFDA"}}
          (str " " (:action alert) " ")]
         [:a {:href (path-for routes :view-story :story (get-in alert [:target :_key]))
              :style {:color "#E0F2F1"}}
          (get-in alert [:target :title])]]]]])])
