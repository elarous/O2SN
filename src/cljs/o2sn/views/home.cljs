(ns o2sn.views.home
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [o2sn.ui :as ui]
            [o2sn.views.maps :as m]
            [secretary.core :as secretary]))

;; helper functions

(defn format-date [datetime]
  (str (:date datetime) " " (:time datetime)))

;; story details modal
(defn card-modal-header [story]
  (let [marked-truth @(rf/subscribe [:marked-story-truth? (:_key story)])
        marked-lie @(rf/subscribe [:marked-story-lie? (:_key story)])]
    [ui/modal-header
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
     (:title story)
     [ui/label {:color (get-in story [:category :color])}
      (get-in story [:category :name])]]))

(defn card-modal-imgs [story]
  (if (seq (:images story))
    [ui/grid
     [ui/grid-column {:width 1
                      :vertical-align "middle"}
      [ui/icon {:name "chevron left"
                :link true
                :size "big"
                :on-click #(rf/dispatch [:previous-story-modal-img])}]]
     [ui/grid-column {:width 14
                      :text-align "center"}
      [ui/image {:src (str "img/" @(rf/subscribe [:story-modal-img]))
                 :style {:display "inline"}}]]
     [ui/grid-column {:width 1
                      :vertical-align "middle"}
      [ui/icon {:name "chevron right"
                :link true
                :size "big"
                :on-click #(rf/dispatch [:next-story-modal-img])}]]]
    [ui/container {:text-align "center"}
     [ui/header {:as "h3"
                 :content "This Story Has No Images"
                 :color "grey"}]]))

(defn card-modal-map [story]
  [ui/grid
   [ui/grid-column {:width 1}]
   [ui/grid-column {:width 14
                    :text-align "center"}
    [:div#map
     [m/wrapped-map {:lng (get-in story [:location :lng])
                     :lat (get-in story [:location :lat])
                     :on-click (fn [_] _)}]]]
   [ui/grid-column {:width 1}]])

(defn card-modal-date [story]
  [ui/button {:icon true
              :label-position "left"
              :color "teal"
              :size "small"}
   [ui/icon {:name "calendar"}]
   (format-date (:datetime story))])

(defn card-modal-location [story]
  (if-not @(rf/subscribe [:story-map-visible?])
    [ui/button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:toggle-map-visiblity])}
     [ui/icon {:name "world"}]
     "View On Map"]
    [ui/button {:icon  true
                :label-position "left"
                :color "teal"
                :size "small"
                :on-click #(rf/dispatch [:toggle-map-visiblity])}
     [ui/icon {:name "picture"}]
     "View Pictures"]))

(defn card-modal-description [story]
  [ui/segment {:stacked true}
   (:description story)])

(defn card-modal-truth [story]
  [ui/popup
   {:hoverable true
    :trigger
    (r/as-element
     [ui/button {:as "div"
                 :active false
                 :label-position "right"
                 :on-click #(rf/dispatch [:toggle-truth-story (:_key story)])}
      [ui/button {:color (if @(rf/subscribe [:marked-story-truth? (:_key story)])
                           "green"
                           "grey")
                  :active false}
       [ui/icon {:name "check"}]
       "Truth"]
      [ui/label {:as "a"
                 :basic true
                 :color (if @(rf/subscribe [:marked-story-truth? (:_key story)])
                          "green"
                          "grey")
                 :pointing "left"}
       (count (:truth story))]])}
   [ui/list {:vertical-align "middle"}
    (for [u (:truth story)]
      ^{:key (:_key u)}
      [ui/list-item
       [ui/image {:src "img/myAvatar.svg" :avatar true}]
       [ui/list-content
        [ui/list-header (:username u)]]])]])

(defn card-modal-lie [story]
  [ui/popup
   {:hoverable true
    :trigger
    (r/as-element
     [ui/button {:as "div"
                 :label-position "right"
                 :on-click #(rf/dispatch [:toggle-lie-story (:_key story)])}
      [ui/button {:color (if @(rf/subscribe [:marked-story-lie? (:_key story)])
                           "red"
                           "grey")}
       [ui/icon {:name "x"}]
       "Lie"]
      [ui/label {:as "a"
                 :basic true
                 :color (if @(rf/subscribe [:marked-story-lie? (:_key story)])
                          "red"
                          "grey")
                 :pointing "left"}
       (count (:lie story))]])}
   [ui/list {:vertical-align "middle"}
    (for [u (:lie story)]
      ^{:key (:_key u)}
      [ui/list-item
       [ui/image {:src "img/myAvatar.svg" :avatar true}]
       [ui/list-content
        [ui/list-header (:username u)]]])]])

(defn card-modal [story]
  [ui/modal {:open @(rf/subscribe [:story-modal-visible?])}
   [card-modal-header story]
   [ui/modal-content {:scrolling true}
    (if @(rf/subscribe [:story-map-visible?])
      [card-modal-map story]
      [card-modal-imgs story])
    [ui/grid {:style {:margin-bottom "5px"}}
     [ui/grid-column {:width 8
                      :text-align "center"}
      [card-modal-date story]]
     [ui/grid-column {:width 8
                      :text-align "center"}
      [card-modal-location story]]]
    [ui/modal-description
     [card-modal-description story]
     [ui/grid
      [ui/grid-column {:width 8
                       :text-align "center"}
       [card-modal-truth story]]
      [ui/grid-column {:width 8
                       :text-align "center"}
       [card-modal-lie story]]]]]
   [ui/modal-actions
    [ui/button {:primary true
                :icon "x"
                :content "Close"
                :on-click #(rf/dispatch [:hide-story-modal])}]]])

(defn card-likes-modal []
  [ui/modal {:open @(rf/subscribe [:story-like-modal-visible])
             :close-icon true
             :size "mini"
             :on-close #(rf/dispatch [:hide-story-users-like-modal])}
   [ui/modal-content
    [ui/list {:selection true
              :vertical-align "middle"}
     (for [u @(rf/subscribe [:story-like-modal-users])]
       ^{:key (:_key u)}
       [ui/list-item  {:on-click #(do (rf/dispatch [:hide-story-users-like-modal])
                                      (rf/dispatch [:set-active-panel :profile])
                                      (rf/dispatch [:profile/load-by-user
                                                    (:_key u)]))}
        [ui/image {:src "img/myAvatar.svg"
                   :avatar true
                   :size "mini"}]
        [ui/list-content
         [ui/list-header
          [ui/header {:as "h3"}
           (:username u)]]]])]]])

(defn news-card [story]
  [:div#news-card
   [ui/card {:color (get-in story [:category :color])}
    [ui/card-content
     [ui/label {:corner "right"
                :as "a"
                :on-click #(rf/dispatch [:show-story-modal (:_key story)])
                :icon
                (r/as-element
                 [ui/icon {:name "content"
                           :link true}])}]
     [ui/label {:ribbon true
                :color (get-in story [:category :color])}
      (get-in story [:category :name])]
     [ui/card-header (:title story)]
     [ui/card-meta
      [ui/grid {:columns 16}
       [ui/grid-column {:width 7}
        [:span {:style {:font-size ".7rem"}}
         (format-date (:datetime story))]]
       #_[ui/grid-column {:width 7}
          [:span {:style {:font-size ".7rem"}} (get-in story [:location :name])]]]]
     [ui/card-description (:description story)]]
    [ui/card-content {:extra true}
     [ui/grid {:vertical-align "middle"
               :text-align "center"}
      [ui/grid-column {:width 4}
       [ui/icon {:name "thumbs up"
                 :link true
                 :color (if @(rf/subscribe [:like-story? (:_key story)]) "green" "grey")
                 :on-click #(rf/dispatch [:toggle-like-story (:_key story)])}]
       [:a {:on-click #(rf/dispatch
                        [:show-story-users-like-modal (:likes story)])}
        (count (:likes story))]]
      [ui/grid-column {:width 4}
       [ui/icon {:name "thumbs down"
                 :link true
                 :color (if @(rf/subscribe [:dislike-story? (:_key story)]) "red" "grey")
                 :on-click #(rf/dispatch [:toggle-dislike-story (:_key story)])}]
       [:a {:on-click #(rf/dispatch
                        [:show-story-users-like-modal (:dislikes story)])}
        (count (:dislikes story))]]]]]])

(defn chan-contents []
  [ui/segment {:loading @(rf/subscribe [:loading-stories?])
               :style {:background-color "#FAFAFA"
                       :height "100%"}}
   [:div#news-cards
    [card-likes-modal]
    [card-modal @(rf/subscribe [:current-story])]
    (let [stories @(rf/subscribe [:stories])]
      (if (seq stories)
        (for [story stories]
          ^{:key (:_key story)}
          [news-card story])
        [ui/container {:text-align "center"}
         [ui/header {:as "h4"
                     :color "grey"
                     :content "No channel selected or channel is empty !"}]]))]])

(defn chan-select []
  [ui/dropdown
   {:placeholder "Select a subscription channel"
    :search true
    :selection true
    :fluid true
    :value @(rf/subscribe [:selected-channel])
    :on-change (fn [_ data]
                 (rf/dispatch [:set-selected-user-chan
                               (-> data .-value)]))
    :on-focus #(rf/dispatch [:channels/load])
    :options (clj->js
              (map #(hash-map
                     :key (:_key %)
                     :value (:_key %)
                     :text (:name %)
                     :label (r/as-element
                             [ui/label {:content (-> % :name first .toUpperCase)
                                        :color
                                        @(rf/subscribe [:channels/color-by-k
                                                        (:_key %)])
                                        :circular true}]))
                   @(rf/subscribe [:channels/all])))}])

(defn chan-selection []
  (let [chan-k @(rf/subscribe [:selected-channel])
        color @(rf/subscribe [:channels/color-by-k chan-k])]
    [ui/segment {:color color
                 :style {:width "75%"
                         :margin "auto"}}
     [ui/grid {:columns 16
               :vertical-align "middle"
               :text-align "center"}
      [ui/grid-column {:width 1}]
      [ui/grid-column {:width 4}
       [ui/header {:as "h3"
                   :color color}
        [ui/icon {:name "podcast"}]
        [ui/header-content "News Channel"]]]
      [ui/grid-column {:width 10}
       [chan-select]]
      [ui/grid-column {:width 1}]]]))

(defn home-main []
  [:div
   [chan-selection]
   [chan-contents]])

(defn home-page []
  [@(rf/subscribe [:active-panel])])

(defn messages-panel []
  [:div
   [:h2 "Messages : "]
   [:ul
    [:li "first ...."]
    [:li "second ...."]]])
