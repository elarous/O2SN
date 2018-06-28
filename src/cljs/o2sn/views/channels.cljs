(ns o2sn.views.channels
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]
            [o2sn.views.maps :as m]
            [o2sn.views.operation :as op]))

(defn saving-view []
  (op/operation-segment
   {:state @(rf/subscribe [:channels/saving-state])
    :title "Add A Channel"
    :progress @(rf/subscribe [:channels/progress])
    :sub-title "Adding A New Channel"
    :m-success {:sub-title "The Channel Has Been Added Successfully"
                :on-click #(rf/dispatch [:channels/hide-saving])
                :btn-txt "My Channels"
                :done-msg "You can now select this channel from the dropdown in the home page."}
    :m-error {:sub-title "Something Went Wrong !"
              :on-click #(rf/dispatch [:channels/hide-saving])
              :btn-txt "My Channels"
              :done-msg @(rf/subscribe [:channels/error-msg])}}))

(defn map-view []
  [:div#map
   [m/wrapped-map {:lat 32.053250726144796 ;; el kelaa cords
                   :lng -7.407108638525983
                   :on-click #(rf/dispatch [:channels/select-point
                                            (m/extract-lat-lng %)])}]])

(defn location-view []
  [:div
   [ui/divider {:hidden true}]
   [ui/container {:text-align "center"}
    [ui/button-group
     (doall
      (interpose (r/as-element [ui/button-or])
                 (map #(r/as-element
                        ^{:key (:type %)}
                        [ui/button {:color @(rf/subscribe [:channels/color
                                                           (keyword (:type %))])
                                    :size "large"
                                    :on-click (fn [e]
                                                (rf/dispatch
                                                 [:channels/create-chan (:type %)]))}
                         (:name %)])
                      @(rf/subscribe [:channels/locations]))))]]
   [ui/divider {:hidden true}]
   [ui/divider]
   [ui/container {:text-align "center"}
    [ui/button {:color "teal"
                :circular true
                :icon "map"
                :content "Back To Map"
                :on-click #(rf/dispatch [:channels/set-location-selected false])}]]])

(defn add-channel-tab []
  [:div
   [ui/container {:text-align "center"}
    [ui/header {:as "h2"
                :color "teal"
                :content "Add A New Channel By Location"}]]
   [ui/divider]
   [ui/grid
    [ui/grid-column {:width 2}]
    [ui/grid-column {:width 12}
     (if @(rf/subscribe [:channels/location-selected?])
       [location-view]
       [map-view])
     [ui/grid-column {:width 2}]]]])

(defn my-channels-tab []
  [:div#my-channels-tab
   (doall
    (for [chan @(rf/subscribe [:channels/all])
          :let [color @(rf/subscribe [:channels/color (:type chan)])]]
      ^{:key (:_key chan)}
      [:div {:style {:margin-top "15px"}}
       [ui/confirm {:open
                    @(rf/subscribe [:channels/confirm-visible? (:_key chan)])
                    :on-confirm #(do (rf/dispatch [:channels/delete (:_key chan)])
                                     (rf/dispatch [:channels/cancel-delete (:_key chan)]))
                    :on-cancel #(rf/dispatch [:channels/cancel-delete (:_key chan)])}]
       [ui/segment {:color color
                    :attached "top"
                    :style {:min-width "250px"}}
        [ui/header {:as "h4"
                    :color color}
         [ui/icon {:name "tv"}]
         (:name chan)]
        [ui/divider]
        [ui/grid
         [ui/grid-column {:width 8
                          :text-align "center"}
          [ui/popup {:trigger (r/as-element
                               [ui/label {:color color}
                                (:type chan)])
                     :content "Location Type"}]]
         [ui/grid-column {:width 8
                          :text-align "center"}
          [ui/popup {:trigger (r/as-element [ui/label (:subscribers chan)])
                     :content "Number of Subscribers"}]]]]
       [ui/button {:attached "bottom"
                   :color color
                   :icon "trash"
                   :size "mini"
                   :content "Delete"
                   :on-click
                   #(rf/dispatch [:channels/confirm-delete (:_key chan)])}]]))])

(defn channels-panel []
  (if @(rf/subscribe [:channels/saving-visible?])
    [ui/grid
     [ui/grid-column {:width 3}]
     [ui/grid-column {:width 10}
      [saving-view]]
     [ui/grid-column {:width 3}]]
    [:div
     [ui/tab {:active-index @(rf/subscribe [:channels/tab])
              :on-tab-change #(rf/dispatch [:channels/switch-tab (-> %2 .-activeIndex)])
              :panes
              [{:menuItem (clj->js {:key "channels"
                                    :icon "tv"
                                    :content "My Channels"})
                :render (fn [] (r/as-element
                                [ui/tab-pane [my-channels-tab]]))}
               {:menuItem (clj->js {:key "add"
                                    :icon "plus"
                                    :content "Add Channel"})
                :render (fn [] (r/as-element
                                [ui/tab-pane [add-channel-tab]]))}]}]]))
