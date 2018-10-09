(ns o2sn.notifications.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [bidi.bidi :refer [path-for]]
            [o2sn.common.routes :refer [routes]]))

(def actions-str
  {:like " Liked Story "
   :dislike " Disliked Story "
   :truth " Marked As Truth "
   :lie " Marked As Lie "
   :new-story " Created A New Story "})

(defn notif-segment [msg]
  (let [user-k (get-in msg [:by :_key])
        target-url (if (:target msg)
                     (path-for routes :view-story :story
                               (get-in msg [:target :_key])) "#")]
    [:div.notif-segment
     [:div.notif-img
      [:img {:src "img/user.svg"}]]
     [:div.notif-body
      [:div
       [:a.notif-username {:href (path-for routes :profile :user user-k)}
        (str "@" (get-in msg [:by :username]))]
       [:span.notif-action
        (get actions-str (keyword (:type msg)))]]
      [:a.notif-target {:href target-url
                        :on-click #(rf/dispatch [:notifs/notif-click msg])}
       (get-in msg [:target :title])]]]))

(defn menu-notifications []
  (let [notifs @(rf/subscribe [:notifs/unreads])]
    (if (pos? (count notifs))
      [:div
       [:div#links
        [:a {:on-click #(rf/dispatch [:notifs/mark-read-all])
             :style {:cursor "pointer"}}
         "Mark All As Read"]
        [:a {:href (path-for routes :notifications)}
         "All Notifications"]]
       [:div.notifs
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
