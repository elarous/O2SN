(ns o2sn.views.story
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [o2sn.ui :as ui]
            [o2sn.views.maps :as m]
            [secretary.core :as secretary]
            [o2sn.helpers.stories :as helpers]))

(defn header [story]
  (let [marked-truth @(rf/subscribe [:story/marked-truth? (:_key story)])
        marked-lie @(rf/subscribe [:story/marked-lie? (:_key story)])]
    [:div
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
     [ui/header {:as "h1"
                 :color "teal"
                 :content (:title story)
                 :style {:display "inline"}}]
     [ui/label {:color (get-in story [:category :color])}
      (get-in story [:category :name])]
     [ui/divider]]))

(defn imgs [story]
  (if (seq (:images story))
    [ui/grid
     [ui/grid-column {:width 1
                      :vertical-align "middle"}
      [ui/icon {:name "chevron left"
                :link true
                :size "big"
                :on-click #(rf/dispatch [:story/previous-img])}]]
     [ui/grid-column {:width 14
                      :text-align "center"}
      [ui/image {:src @(rf/subscribe [:story/img])
                 :style {:display "inline"}}]]
     [ui/grid-column {:width 1
                      :vertical-align "middle"}
      [ui/icon {:name "chevron right"
                :link true
                :size "big"
                :on-click #(rf/dispatch [:story/next-img])}]]]
    [ui/container {:text-align "center"}
     [ui/header {:as "h3"
                 :content "This Story Has No Images"
                 :color "grey"}]]))

(defn gmap [story]
  [ui/grid
   [ui/grid-column {:width 1}]
   [ui/grid-column {:width 14
                    :text-align "center"}
    [:div#map
     [m/wrapped-map {:lng (get-in story [:location :lng])
                     :lat (get-in story [:location :lat])
                     :on-click (fn [_] _)}]]]
   [ui/grid-column {:width 1}]])

(defn date [story]
  [ui/button {:icon true
              :label-position "left"
              :color "teal"
              :size "small"}
   [ui/icon {:name "calendar"}]
   (helpers/format-date (:datetime story))])

(defn location [story]
  (if-not @(rf/subscribe [:story/map-visible?])
    [ui/button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:story/toggle-map])}
     [ui/icon {:name "world"}]
     "View On Map"]
    [ui/button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:story/toggle-map])}
     [ui/icon {:name "picture"}]
     "View Pictures"]))

(defn description [story]
  [ui/segment {:stacked true}
   (:description story)])

(defn truth-lie-comp [{:keys [marked? users color label icon on-click]}]
  [ui/popup
   {:hoverable true
    :trigger
    (r/as-element
     [ui/button {:as "div"
                 :active false
                 :label-position "right"
                 :on-click on-click}
      [ui/button {:color (if marked? color "grey")
                  :active false}
       [ui/icon {:name icon}]
       label]
      [ui/label {:as "a"
                 :basic true
                 :color (if marked? color "grey")
                 :pointing "left"}
       (count users)]])}
   [:div {:style {:max-height "200px"}}
    (if (seq users)
      [ui/list {:vertical-align "middle"
                :selection true}
       (for [u users]
         ^{:key (:_key u)}
         [ui/list-item {:on-click #(do
                                     (rf/dispatch [:profile/load-by-user (:_key u)])
                                     (rf/dispatch [:set-active-panel :profile]))}
          [ui/image {:src (str "avatars/" (:avatar u))
                     :avatar true
                     :style {:min-height "40px"
                             :min-width "40px"}}]
          [ui/list-content
           [ui/list-header {:as (fn []
                                  (r/as-element
                                   [ui/header
                                    {:as "h3"
                                     :color "grey"
                                     :content (:username u)}]))}]]])]
      [ui/header {:as "h4"
                  :color "grey"
                  :content "this list is empty"}])]])

(defn truth [story]
  [truth-lie-comp
   {:marked? @(rf/subscribe [:story/marked-truth? (:_key story)])
    :users (:truth story)
    :color "green"
    :label "Truth"
    :icon "check"
    :on-click #(rf/dispatch [:story/toggle-truth (:_key story)])}])

(defn lie [story]
  [truth-lie-comp
   {:marked? @(rf/subscribe [:story/marked-lie? (:_key story)])
    :users (:lie story)
    :color "red"
    :label "Lie"
    :icon "x"
    :on-click #(rf/dispatch [:story/toggle-lie (:_key story)])}])

(defn story-segment [story]
  [ui/segment
   [header story]
   [:div
    (if @(rf/subscribe [:story/map-visible?])
      [gmap story]
      [imgs story])
    [ui/grid {:style {:margin-bottom "5px"}}
     [ui/grid-column {:width 8
                      :text-align "center"}
      [date story]]
     [ui/grid-column {:width 8
                      :text-align "center"}
      [location story]]]
    [ui/modal-description
     [description story]
     [ui/grid
      [ui/grid-column {:width 8
                       :text-align "center"}
       [truth story]]
      [ui/grid-column {:width 8
                       :text-align "center"}
       [lie story]]]]]])

(defn story-panel []
  [ui/grid {:style {:overflow-y "scroll"}}
   [ui/grid-column {:width 2}]
   [ui/grid-column {:width 12}
    [story-segment @(rf/subscribe [:story/current])]]
   [ui/grid-column {:width 2}]])
