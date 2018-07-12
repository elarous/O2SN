(ns o2sn.views.home
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [o2sn.ui :as ui]
            [o2sn.views.maps :as m]
            [secretary.core :as secretary]
            [o2sn.helpers.stories :as helpers]))

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
        [ui/image {:src (str "avatars/" (:avatar u))
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
     [ui/label
      {:corner "right"
       :as "a"
       :on-click #(rf/dispatch [:story/set-current (:_key story) false])
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
         (helpers/format-date (:datetime story))]]]]
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
  [:div])
