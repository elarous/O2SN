(ns o2sn.profile.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]))

(defn infos-segment []
  [:> ui/Segment  {:loading @(rf/subscribe [:profile/loading? :infos])}
   [:> ui/Grid
    [:> ui/Grid.Column {:width 6}
     [:> ui/Container {:text-align "center"}
      [:> ui/Image {:src  @(rf/subscribe [:profile/avatar])
                    :circular true
                    :inline true
                    :size "small"
                    :style {:max-height "250px"
                            :max-width "250px"}}]]]
    [:> ui/Grid.Column {:width 10
                        :vertical-align "middle"
                        :text-align "center"}
     [:> ui/Segment
      [:> ui/Header {:as "h2"
                     :color "teal"
                     :text-align "center"}
       @(rf/subscribe [:profile/fullname])
       [:> ui/Header.Subheader
        (str "@" @(rf/subscribe [:profile/username]))]]
      [:> ui/Header {:as "h3"
                     :color "grey"
                     :text-align "center"}
       @(rf/subscribe [:profile/email])]
      [:> ui/Grid
       [:> ui/Grid.Column {:text-align "center"
                           :vertical-align "middle"
                           :width 5}
        [:> ui/Header {:as "h4"
                       :color "grey"
                       :content (str @(rf/subscribe [:profile/age]) " years")}]]
       [:> ui/Grid.Column {:width 5
                           :text-align "center"}
        [:> ui/Icon {:name @(rf/subscribe [:profile/gender])
                     :color (if (= @(rf/subscribe [:profile/gender]) "male")
                              "blue" "pink")
                     :size "big"}]]
       [:> ui/Grid.Column {:width 6
                           :text-align "center"}
        [:> ui/Image {:src (str "flags/"
                                @(rf/subscribe [:profile/country])
                                ".svg")
                      :size "mini"
                      :inline true}]]]]]]])

(defn stats-segment []
  (let [stats @(rf/subscribe [:profile/stats])]
    [:> ui/Segment {:loading @(rf/subscribe [:profile/loading? :stats])}
     [:> ui/Header {:as "h2"
                    :text-align "center"
                    :color "teal"
                    :content "User Actions Statistics"}]
     [:> ui/Divider]
     [:div {:style {:text-align "center"}}
      [:> ui/Statistic.Group {:size "large"
                              :style {:display "inline"}}
       [:> ui/Statistic {:color "purple"}
        [:> ui/Statistic.Value (:stories stats)]
        [:> ui/Statistic.Label "Stories"]]
       [:> ui/Statistic {:color "violet"}
        [:> ui/Statistic.Value (:truths stats)]
        [:> ui/Statistic.Label "Truths"]]
       [:> ui/Statistic {:color "blue"}
        [:> ui/Statistic.Value (:lies stats)]
        [:> ui/Statistic.Label "Lies"]]
       [:> ui/Statistic {:color "orange"}
        [:> ui/Statistic.Value (:likes stats)]
        [:> ui/Statistic.Label "Likes"]]
       [:> ui/Statistic {:color "yellow"}
        [:> ui/Statistic.Value (:dislikes stats)]
        [:> ui/Statistic.Label "Dislikes"]]]]]))

(defn feed-segment []
  (let [activities @(rf/subscribe [:profile/activities])]
    [:> ui/Segment {:loading @(rf/subscribe [:profile/loading? :activities])}
     [:> ui/Header {:as "h2"
                    :text-align "center"
                    :color "teal"
                    :content "Recent Activities"}]
     [:> ui/Divider]
     (if (pos? (count activities))
       [:div {:style {:padding-left "40px"}}
        [:> ui/Feed
         (for [a activities]
           ^{:key (:_key a)}
           [:> ui/Feed.Event {:icon
                              (case (keyword (:type a))
                                :new-story "plus"
                                :like "thumbs up"
                                :dislike "thumbs down"
                                :truth "check"
                                :lie "x"
                                "feed")
                              :date "Today"
                              :summary (get-in a [:target :title])}])]]
       [:> ui/Container {:text-align "center"}
        [:> ui/Header {:as "h3"
                       :color "grey"
                       :content "User has no activities yet"}]])]))

(defn rating-segment []
  (let [rating @(rf/subscribe [:profile/rating])
        truths (:truths rating)
        lies (:lies rating)]
    [:> ui/Segment {:loading @(rf/subscribe [:profile/loading? :rating])}
     [:> ui/Header {:as "h2"
                    :text-align "center"
                    :color "teal"
                    :content "Rating"}]
     [:> ui/Divider]
     [:> ui/Container {:text-align "center"}
      [:> ui/Rating {:rating (* (/ truths (+ truths lies)) 5)
                     :max-rating 5
                     :icon "star"
                     :size "massive"}]]
     [:> ui/Grid
      [:> ui/Grid.Column {:width 8
                          :text-align "center"}
       [:> ui/Statistic {:size "large"
                         :color "green"}
        [:> ui/Statistic.Value truths]
        [:> ui/Statistic.Label "Truths"]]]
      [:> ui/Grid.Column {:width 8
                          :text-align "center"}
       [:> ui/Statistic {:size "large"
                         :color "red"}
        [:> ui/Statistic.Value lies]
        [:> ui/Statistic.Label "Lies"]]]]]))

(defn profile-page []
  [:div
   [:> ui/Grid
    [:> ui/Grid.Column {:width 8}
     [infos-segment]]
    [:> ui/Grid.Column {:width 8}
     [stats-segment]]]
   [:> ui/Grid
    [:> ui/Grid.Column {:width 8}
     [feed-segment]]
    [:> ui/Grid.Column {:width 8}
     [rating-segment]]]])

