(ns o2sn.channels.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [o2sn.maps.views :as m]
            [o2sn.operation.views :as op]))


(defn map-view []
  [:div#map
   [m/wrapped-map {:lat 32.053250726144796 ;; el kelaa cords
                   :lng -7.407108638525983
                   :on-click (fn [e]
                               (when-let [latLng (goog.object/get e "latLng")]
                                 (let [lat-fn (goog.object/get latLng "lat")
                                       lng-fn (goog.object/get latLng "lng")]
                                   (rf/dispatch [:channels/select-point
                                                 (hash-map :lat (lat-fn)
                                                           :lng (lng-fn))]))))}]])

(defn location-view []
  [:div
   [:> ui/Divider {:hidden true}]
   [:> ui/Container {:text-align "center"}
    [:> ui/Button.Group
     (doall
      (interpose (r/as-element [:> ui/Button.Or])
                 (map #(r/as-element
                        ^{:key (:type %)}
                        [:> ui/Button {:color @(rf/subscribe [:channels/color
                                                           (keyword (:type %))])
                                    :size "large"
                                    :on-click (fn [e]
                                                (rf/dispatch
                                                 [:channels/create-chan (:type %)]))}
                         (:name %)])
                      @(rf/subscribe [:channels/locations]))))]]
   [:> ui/Divider {:hidden true}]
   [:> ui/Divider]
   [:> ui/Container {:text-align "center"}
    [:> ui/Button {:color "teal"
                :circular true
                :icon "map"
                :content "Back To Map"
                :on-click #(rf/dispatch [:channels/set-location-selected false])}]]])

(defn add-channel-page []
  [:div
   [:> ui/Container {:text-align "center"}
    [:> ui/Header {:as "h2"
                :color "teal"
                :content "Add A New Channel By Location"}]]
   [:> ui/Divider]
   [:> ui/Grid
    [:> ui/Grid.Column {:width 2}]
    [:> ui/Grid.Column {:width 12}
     (if @(rf/subscribe [:channels/location-selected?])
       [location-view]
       [map-view])
     [:> ui/Grid.Column {:width 2}]]]])

(defn list-channels-page []
  [:div#my-channels-tab
   (let [chans @(rf/subscribe [:channels/all])]
     (if (seq chans)
       (doall
        (for [chan chans
              :let [color @(rf/subscribe [:channels/color (:type chan)])]]
          ^{:key (:_key chan)}
          [:div {:style {:margin-top "15px"}}
           [:> ui/Confirm {:open
                        @(rf/subscribe [:channels/confirm-visible? (:_key chan)])
                        :on-confirm #(do (rf/dispatch [:channels/delete (:_key chan)])
                                         (rf/dispatch [:channels/cancel-delete (:_key chan)]))
                        :on-cancel #(rf/dispatch [:channels/cancel-delete (:_key chan)])}]
           [:> ui/Segment {:color color
                        :attached "top"
                        :style {:min-width "250px"}}
            [:> ui/Header {:as "h4"
                        :color color}
             [:> ui/Icon {:name "tv"}]
             (:name chan)]
            [:> ui/Divider]
            [:> ui/Grid
             [:> ui/Grid.Column {:width 8
                              :text-align "center"}
              [:> ui/Popup {:trigger (r/as-element
                                   [:> ui/Label {:color color}
                                    (:type chan)])
                         :content "Location Type"}]]
             [:> ui/Grid.Column {:width 8
                              :text-align "center"}
              [:> ui/Popup {:trigger (r/as-element [:> ui/Label (:subscribers chan)])
                         :content "Number of Subscribers"}]]]]
           [:> ui/Button {:attached "bottom"
                       :color color
                       :icon "trash"
                       :size "mini"
                       :content "Delete"
                       :on-click
                       #(rf/dispatch [:channels/confirm-delete (:_key chan)])}]]))
       [:> ui/Segment
        [:> ui/Container {:text-align "center"}
         [:> ui/Header {:as "h2"
                     :color "grey"
                     :content "You Are Not Subscribed To Any Channel Yet !"}]]]))])
