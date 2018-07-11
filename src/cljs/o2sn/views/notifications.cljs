(ns o2sn.views.notifications
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]))

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
        username [:a {:on-click #(do (rf/dispatch [:profile/load-by-user user-k])
                                     (rf/dispatch [:set-active-panel :profile]))}
                  (str "@" (get-in msg [:by :username]))]
        story-title [:a {:on-click #(rf/dispatch [:notifs/notif-click msg])
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

(defn notif-segment[msg]
  [ui/segment {:compact false
               :style {:padding "3px"
                       :background-color (first (get-color (:type msg)))
                       :cursor "pointer"}}
   [ui/grid
    [ui/grid-column {:width 4
                     :vertical-align "middle"
                     :text-align "center"}
     [ui/image {:src (str "avatars/" (get-in msg [:by :avatar]))
                :avatar true
                :style {:min-height "50px"
                        :min-width "50px"}}]]
    [ui/grid-column {:width 12
                     :text-align "left"
                     :vertical-align "middle"}
     [:span (make-msg msg)]]]])

(defn menu-notifications []
  (let [notifs @(rf/subscribe [:notifs/unreads])]
    (if (pos? (count notifs))
      [:div
       [ui/container {:text-align "center"
                      :style {:margin-bottom "3px"}}
        [ui/button {:as "div"
                    :label-position "right"
                    :size "mini"}
         [ui/button {:color "teal"
                     :size "mini"
                     :on-click #(rf/dispatch [:notifs/mark-read-all])}
          [ui/icon {:name "check"}]
          "Mark All As Read"]
         [ui/label {:as "div"
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
       [ui/header {:as "h5"
                   :color "grey"
                   :content "You don't have new notifications"}]])))
