(ns o2sn.profile.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [bidi.bidi :refer [path-for]]
            [o2sn.common.routes :refer [routes]]))

(defn infos-segment []
  [:div.infos
   [:div.img-container
    [:img {:src @(rf/subscribe [:profile/avatar])}]]
   [:div.names
    [:h1.fullname @(rf/subscribe [:profile/fullname])]
    [:h2.username "@" @(rf/subscribe [:profile/username])]
    [:h3.email @(rf/subscribe [:profile/email])]]
   [:div.basic-infos
    [:div.age
     [:div.number @(rf/subscribe [:profile/age])]
     [:div.years "years"]]
    [:div.gender-container
     [:> ui/Icon {:name @(rf/subscribe [:profile/gender])
                  :class-name "icon"
                  :color (if (= @(rf/subscribe [:profile/gender]) "male")
                           "blue" "pink")
                  :size "big"}]]
    [:div.flag-container
     [:img {:src (str "/img/flags/"
                      @(rf/subscribe [:profile/country])
                      ".svg")}]]]])

(defn stats-segment []
  (let [stats @(rf/subscribe [:profile/stats])]
    [:div.stats.side-segment
     [:h1 "Stats"]
     [:hr]
     [:div.all-stats
      [:div.stats-container
       [:div.label "Owned Stories"]
       [:div.number (:stories stats)]
       [:div.label "Given Truths"]
       [:div.number (:truths stats)]
       [:div.label "Given Lies"]
       [:div.number (:lies stats)]]
      [:div.stats-container
       [:div.label "Given Likes"]
       [:div.number (:likes stats)]
       [:div.label "Given Dislikes"]
       [:div.number (:dislikes stats)]]]]))

(defn activities-segment []
  (let [activities @(rf/subscribe [:profile/activities])]
    [:div.activities.side-segment
     [:h1 "Recent Activities"]
     [:hr]
     [:div.table-container 
      [:table
       [:thead
        [:th "Story"]
        [:th "Type"]]
       [:tbody
        (for [activity activities]
          ^{:key (:_key activity)}
          [:tr
           [:td
            [:a {:href (path-for
                        routes
                        :view-story
                        :story
                        (get-in activity [:target :_key]))}
             (get-in activity [:target :title])]]
           [:td (:type activity)]])]]]]))

(defn credibility-segment []
  (let [rating @(rf/subscribe [:profile/rating])
        truths (:truths rating)
        lies (:lies rating)]
    [:div.credibility.side-segment
     [:h1 "Credibility"]
     [:hr]
     [:div.rating-container
      [:> ui/Rating {:rating (* (/ truths (+ truths lies)) 5)
                     :max-rating 5
                     :icon "star"
                     :size "massive"}]]
     [:div.stats
      [:div.truths
       [:div.label "Truths "]
       [:div.number truths]]
      [:div.lies
       [:div.label "Lies"]
       [:div.number lies]]]]))

(defn stories-segment []
  [:div.stories
   [:h1 "Stories"]
   [:hr]
   [:> ui/Button.Group {:widths 3
                        :class-name "buttons"}
    [:> ui/Button {:class-name "button"} "Owned"]
    [:> ui/Button {:class-name "button"} "Marked As Truth"]
    [:> ui/Button {:class-name "button"} "Marked As Lie"]]
   [:div.table-container
    [:table {:border "0px"}
     [:thead
      [:th "Story"]
      [:th "Date"]]
     [:tbody
      [:tr
       [:td [:a {:href "#"} "My First Story Title"]]
       [:td "09-03-2015"]]
      [:tr
       [:td [:a {:href "#"} "My Second Story Title"]]
       [:td "11-10-2017"]]
      [:tr
       [:td [:a {:href "#"} "My Third Story Title"]]
       [:td "11-10-2017"]]]]]])

(defn profile-page []
  [:div#profile-page
   [infos-segment]
   [:div.left
    [stories-segment]]
   [:div.right
    [credibility-segment]
    [stats-segment]
    [activities-segment]]])

