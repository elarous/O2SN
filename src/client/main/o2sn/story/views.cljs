(ns o2sn.story.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [o2sn.maps.views :as m]
            [cljs-time.core :as t]
            [cljs-time.format :as f]))

(defn- format-dt [dt-str]
  (when dt-str
    (let [dt (f/parse (f/formatters :date-time-no-ms) dt-str)]
      (f/unparse (f/formatters :mysql) dt))))

(defn header [story]
  (let [marked-truth @(rf/subscribe [:story/marked-truth? (:_key story)])
        marked-lie @(rf/subscribe [:story/marked-lie? (:_key story)])]
    [:div
     [:div
      (cond
        marked-truth [:> ui/Label {:ribbon "right"
                                :color "green"}
                      "Marked as Truth"]
        marked-lie [:> ui/Label {:ribbon "right"
                              :color "red"}
                    "Marked as Lie"]
        :else [:> ui/Label {:ribbon "right"
                         :color "grey"}
               "Not Marked"])]
     [:> ui/Header {:as "h1"
                 :color "teal"
                 :content (:title story)
                 :style {:display "inline"}}]
     [:> ui/Label {:color (get-in story [:category :color])}
      (get-in story [:category :name])]
     [:> ui/Divider]]))

(defn imgs [story]
  (if (seq (:images story))
    [:> ui/Grid
     [:> ui/Grid.Column {:width 1
                      :vertical-align "middle"}
      [:> ui/Icon {:name "chevron left"
                :link true
                :size "big"
                :on-click #(rf/dispatch [:story/previous-img])}]]
     [:> ui/Grid.Column {:width 14
                      :text-align "center"}
      [:> ui/Image {:src @(rf/subscribe [:story/img])
                 :style {:display "inline"}}]]
     [:> ui/Grid.Column {:width 1
                      :vertical-align "middle"}
      [:> ui/Icon {:name "chevron right"
                :link true
                :size "big"
                :on-click #(rf/dispatch [:story/next-img])}]]]
    [:> ui/Container {:text-align "center"}
     [:> ui/Header {:as "h3"
                 :content "This Story Has No Images"
                 :color "grey"}]]))

(defn gmap [story]
  [:> ui/Grid
   [:> ui/Grid.Column {:width 1}]
   [:> ui/Grid.Column {:width 14
                    :text-align "center"}
    [:div#map
     [m/wrapped-map {:lng (get-in story [:location :lng])
                     :lat (get-in story [:location :lat])
                     :on-click (fn [_] _)}]]]
   [:> ui/Grid.Column {:width 1}]])

(defn date [story]
  [:> ui/Button {:icon true
              :label-position "left"
              :color "teal"
              :size "small"}
   [:> ui/Icon {:name "calendar"}]
   (format-dt (:datetime story))])

(defn location [story]
  (if-not @(rf/subscribe [:story/map-visible?])
    [:> ui/Button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:story/toggle-map])}
     [:> ui/Icon {:name "world"}]
     "View On Map"]
    [:> ui/Button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:story/toggle-map])}
     [:> ui/Icon {:name "picture"}]
     "View Pictures"]))

(defn description [story]
  [:> ui/Segment {:stacked true}
   (:description story)])

(defn truth-lie-comp [{:keys [marked? users color label icon on-click]}]
  [:> ui/Popup
   {:hoverable true
    :trigger
    (r/as-element
     [:> ui/Button {:as "div"
                 :active false
                 :label-position "right"
                 :on-click on-click}
      [:> ui/Button {:color (if marked? color "grey")
                  :active false}
       [:> ui/Icon {:name icon}]
       label]
      [:> ui/Label {:as "a"
                 :basic true
                 :color (if marked? color "grey")
                 :pointing "left"}
       (count users)]])}
   [:div {:style {:max-height "200px"}}
    (if (seq users)
      [:> ui/List {:vertical-align "middle"
                :selection true}
       (for [u users]
         ^{:key (:_key u)}
         [:> ui/List.Item {:on-click #(do
                                     (rf/dispatch [:profile/load-by-user (:_key u)])
                                     (rf/dispatch [:set-active-panel :profile]))}
          [:> ui/Image {:src (str "avatars/" (:avatar u))
                     :avatar true
                     :style {:min-height "40px"
                             :min-width "40px"}}]
          [:> ui/List.Content
           [:> ui/List.Header {:as (fn []
                                  (r/as-element
                                   [:> ui/Header
                                    {:as "h3"
                                     :color "grey"
                                     :content (:username u)}]))}]]])]
      [:> ui/Header {:as "h4"
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
  [:> ui/Segment
   {:loading @(rf/subscribe [:story/loading?])}
   [header story]
   [:div
    (if @(rf/subscribe [:story/map-visible?])
      [gmap story]
      [imgs story])
    [:> ui/Grid {:style {:margin-bottom "5px"}}
     [:> ui/Grid.Column {:width 8
                      :text-align "center"}
      [date story]]
     [:> ui/Grid.Column {:width 8
                      :text-align "center"}
      [location story]]]
    [:> ui/Modal.Description
     [description story]
     [:> ui/Grid
      [:> ui/Grid.Column {:width 8
                       :text-align "center"}
       [truth story]]
      [:> ui/Grid.Column {:width 8
                       :text-align "center"}
       [lie story]]]]]])

(defn story-page []
  [:> ui/Grid {:style {:overflow-y "scroll"}}
   [:> ui/Grid.Column {:width 2}]
   [:> ui/Grid.Column {:width 12}
    [story-segment @(rf/subscribe [:story/current])]]
   [:> ui/Grid.Column {:width 2}]])
