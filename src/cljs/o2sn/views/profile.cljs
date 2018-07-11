(ns o2sn.views.profile
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]))

(defn infos-segment []
  [ui/segment  {:loading @(rf/subscribe [:profile/loading? :infos])}
   [ui/grid
    [ui/grid-column {:width 6}
     [ui/container {:text-align "center"}
      [ui/image {:src (str "avatars/" @(rf/subscribe [:profile/avatar]))
                 :circular true
                 :inline true
                 :size "small"
                 :style {:max-height "250px"
                         :max-width "250px"}}]]]
    [ui/grid-column {:width 10
                     :vertical-align "middle"
                     :text-align "center"}
     [ui/segment
      [ui/header {:as "h2"
                  :color "teal"
                  :text-align "center"}
       @(rf/subscribe [:profile/fullname])
       [ui/header-subheader
        (str "@" @(rf/subscribe [:profile/username]))]]
      [ui/header {:as "h3"
                  :color "grey"
                  :text-align "center"}
       @(rf/subscribe [:profile/email])]
      [ui/grid
       [ui/grid-column {:text-align "center"
                        :vertical-align "middle"
                        :width 5}
        [ui/header {:as "h4"
                    :color "grey"
                    :content (str @(rf/subscribe [:profile/age]) " years")}]]
       [ui/grid-column {:width 5
                        :text-align "center"}
        [ui/icon {:name @(rf/subscribe [:profile/gender])
                  :color (if (= @(rf/subscribe [:profile/gender]) "male")
                           "blue" "pink")
                  :size "big"}]]
       [ui/grid-column {:width 6
                        :text-align "center"}
        [ui/image {:src (str "flags/"
                             @(rf/subscribe [:profile/country])
                             ".svg")
                   :size "mini"
                   :inline true}]]]]]]])

(defn stats-segment []
  (let [stats @(rf/subscribe [:profile/stats])]
    [ui/segment {:loading @(rf/subscribe [:profile/loading? :stats])}
     [ui/header {:as "h2"
                 :text-align "center"
                 :color "teal"
                 :content "User Actions Statistics"}]
     [ui/divider]
     [:div {:style {:text-align "center"}}
      [ui/statistic-group {:size "large"
                           :style {:display "inline"}}
       [ui/statistic {:color "purple"}
        [ui/statistic-value (:stories stats)]
        [ui/statistic-label "Stories"]]
       [ui/statistic {:color "violet"}
        [ui/statistic-value (:truths stats)]
        [ui/statistic-label "Truths"]]
       [ui/statistic {:color "blue"}
        [ui/statistic-value (:lies stats)]
        [ui/statistic-label "Lies"]]
       [ui/statistic {:color "orange"}
        [ui/statistic-value (:likes stats)]
        [ui/statistic-label "Likes"]]
       [ui/statistic {:color "yellow"}
        [ui/statistic-value (:dislikes stats)]
        [ui/statistic-label "Dislikes"]]]]]))

(defn feed-segment []
  (let [activities @(rf/subscribe [:profile/activities])]
    [ui/segment {:loading @(rf/subscribe [:profile/loading? :activities])}
     [ui/header {:as "h2"
                 :text-align "center"
                 :color "teal"
                 :content "Recent Activities"}]
     [ui/divider]
     [:div {:style {:padding-left "40px"}}
      [ui/feed
       (for [a activities]
         ^{:key (:_key a)}
         [ui/feed-event {:icon
                         (case (keyword (:type a))
                           :new-story "plus"
                           :like "thumbs up"
                           :dislike "thumbs down"
                           :truth "check"
                           :lie "x"
                           "feed")
                         :date "Today"
                         :summary (get-in a [:target :title])}])]]]))

(defn rating-segment []
  (let [rating @(rf/subscribe [:profile/rating])
        truths (:truths rating)
        lies (:lies rating)]
    [ui/segment {:loading @(rf/subscribe [:profile/loading? :rating])}
     [ui/header {:as "h2"
                 :text-align "center"
                 :color "teal"
                 :content "Rating"}]
     [ui/divider]
     [ui/container {:text-align "center"}
      [ui/rating {:rating (* (/ truths (+ truths lies)) 5)
                  :max-rating 5
                  :icon "star"
                  :size "massive"}]]
     [ui/grid
      [ui/grid-column {:width 8
                       :text-align "center"}
       [ui/statistic {:size "large"
                      :color "green"}
        [ui/statistic-value truths]
        [ui/statistic-label "Truths"]]]
      [ui/grid-column {:width 8
                       :text-align "center"}
       [ui/statistic {:size "large"
                      :color "red"}
        [ui/statistic-value lies]
        [ui/statistic-label "Lies"]]]]]))

(defn profile-panel []
  [:div
   [ui/grid
    [ui/grid-column {:width 8}
     [infos-segment]]
    [ui/grid-column {:width 8}
     [stats-segment]]]
   [ui/grid
    [ui/grid-column {:width 8}
     [feed-segment]]
    [ui/grid-column {:width 8}
     [rating-segment]]]])

