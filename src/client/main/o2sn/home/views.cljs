(ns o2sn.home.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            #_["/o2sn/client/js/playing" :as p]))

(defn- format-dt [dt-str]
  (when dt-str
    (let [dt (f/parse (f/formatters :date-time-no-ms) dt-str)]
      (f/unparse (f/formatters :mysql) dt))))

(defn card-likes-modal []
  [:> ui/Modal {:open @(rf/subscribe [:home/likes-modal-visible?])
                :close-icon true
                :size "mini"
                :on-close #(rf/dispatch [:home/hide-likes-modal])}
   [:> ui/Modal.Content
    [:> ui/List {:selection true
                 :vertical-align "middle"}
     (for [u @(rf/subscribe [:home/likes-modal-users])]
       ^{:key (:_key u)}
       [:> ui/List.Item  {:on-click #(rf/dispatch [:navigate :profile :user (:_key u)])}
        [:> ui/Image {:src (or (:avatar u)
                               "/img/user.svg")
                      :avatar true
                      :size "mini"}]
        [:> ui/List.Content
         [:> ui/List.Header
          [:> ui/Header {:as "h3"}
           (:username u)]]]])]]])

(defn news-card [story]
  [:div#news-card
   [:> ui/Card {:color (get-in story [:category :color])}
    [:> ui/Card.Content
     [:> ui/Label
      {:corner "right"
       :as "a"
       :on-click #(rf/dispatch [:story/set-current (:_key story) false])
       :icon
       (r/as-element
        [:> ui/Icon {:name "content"
                     :link true}])}]
     [:> ui/Label {:ribbon true
                   :color (get-in story [:category :color])}
      (get-in story [:category :name])]
     [:> ui/Card.Header (:title story)]
     [:> ui/Card.Meta
      [:> ui/Grid {:columns 16}
       [:> ui/Grid.Column {:width 16}
        [:span {:style {:font-size ".7rem"}}
         (format-dt (:datetime story))]]]]
     [:> ui/Card.Description (:description story)]]
    [:> ui/Card.Content {:extra true}
     [:> ui/Grid {:vertical-align "middle"
                  :text-align "center"}
      [:> ui/Grid.Column {:width 4}
       [:> ui/Icon {:name "thumbs up"
                    :link true
                    :color (if @(rf/subscribe [:home/like? (:_key story)])
                             "green" "grey")
                    :on-click #(rf/dispatch [:home/toggle-like (:_key story)])}]
       [:a {:on-click #(rf/dispatch
                        [:home/show-likes-modal (:likes story)])}
        (count (:likes story))]]
      [:> ui/Grid.Column {:width 4}
       [:> ui/Icon {:name "thumbs down"
                    :link true
                    :color (if @(rf/subscribe [:home/dislike? (:_key story)])
                             "red" "grey")
                    :on-click #(rf/dispatch [:home/toggle-dislike (:_key story)])}]
       [:a {:on-click #(rf/dispatch
                        [:home/show-likes-modal (:dislikes story)])}
        (count (:dislikes story))]]]]]])

(defn chan-contents []
  [:> ui/Segment {:loading @(rf/subscribe [:home/stories-loading?])
                  :style {:background-color "#FAFAFA"
                          :height "100%"}}
   [:div#news-cards
    [card-likes-modal]
    (let [stories @(rf/subscribe [:home/stories])]
      (if (seq stories)
        (for [story stories]
          ^{:key (:_key story)}
          [news-card story])
        [:> ui/Container {:text-align "center"}
         [:> ui/Header {:as "h4"
                        :color "grey"
                        :content "No channel selected or channel is empty !"}]]))]])

(defn chan-select []
  [:> ui/Dropdown
   {:placeholder "Select a subscription channel"
    :search true
    :selection true
    :fluid true
    :value @(rf/subscribe [:home/selected-channel])
    :on-change (fn [_ data]
                 (rf/dispatch [:home/set-channel
                               (-> data .-value)]))
    :on-focus #(rf/dispatch [:channels/load])
    :options (clj->js
              (map #(assoc %
                           :label
                           (r/as-element
                            [:> ui/Label {:content (-> % :text first .toUpperCase)
                                          :color
                                          @(rf/subscribe [:channels/color (:type %)])
                                          :circular true}]))
                   @(rf/subscribe [:home/channels])))}])

(defn chan-selection []
  (let [chan-k @(rf/subscribe [:home/selected-channel])
        color @(rf/subscribe [:channels/color-by-k chan-k])]
    [:> ui/Segment {:color color
                    :style {:width "75%"
                            :margin "auto"}}
     [:> ui/Grid {:columns 16
                  :vertical-align "middle"
                  :text-align "center"}
      [:> ui/Grid.Column {:width 1}]
      [:> ui/Grid.Column {:width 4}
       [:> ui/Header {:as "h3"
                      :color color}
        [:> ui/Icon {:name "podcast"}]
        [:> ui/Header.Content "News Channel"]]]
      [:> ui/Grid.Column {:width 10}
       [chan-select]]
      [:> ui/Grid.Column {:width 1}]]]))

(defn home-page []
  [:div
   [chan-selection]
   [chan-contents]])
