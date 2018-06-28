(ns o2sn.views.new-story
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]
            [o2sn.views.maps :as m]
            [o2sn.views.operation :as op]))

(defn title []
  [ui/input {:type "text"
             :fluid true
             :required true
             :placeholder "Story's Title"
             :value @(rf/subscribe [:new-story/title])
             :on-change #(rf/dispatch [:new-story/set-title (-> % .-target .-value)])}])

(defn location-map []
  [:div#map
   [m/wrapped-map {:lat @(rf/subscribe [:new-story/marker-lat])
                   :lng @(rf/subscribe [:new-story/marker-lng])
                   :on-click #(rf/dispatch [:new-story/move-marker
                                            (m/extract-lat-lng %)])}]])

(defn description []
  [ui/form
   [ui/textarea {:auto-height true
                 :placeholder "Story's Description"
                 :rows 2
                 :style {:min-height "50px"
                         :width "100%"}
                 :value @(rf/subscribe [:new-story/desc])
                 :on-change #(rf/dispatch [:new-story/set-desc (-> % .-target .-value)])}]])

(defn- add-img [img file]
  (rf/dispatch [:new-story/add-img {:img img :file file}]))

(defn images []
  [:div
   [ui/segment
    [:div.new-story-images
     (let [imgs @(rf/subscribe [:new-story/images])]
       (if (pos? (count imgs))
         (for [img @(rf/subscribe [:new-story/images])]
           ^{:key (:fname img)}
           [ui/image {:src (.-src (:img img))
                      :label (clj->js
                              {:as "a"
                               :corner "right"
                               :icon "trash"
                               :onClick #(rf/dispatch [:new-story/remove-img
                                                       (:fname img)])})}])
         [ui/header {:as "h5"
                     :text-align "center"
                     :color "grey"
                     :content "No Image Selected"}]))]]
   [:div
    [ui/container {:text-align "center"}
     [ui/button {:content "Add Picture"
                 :icon "picture"
                 :on-click #(.click (.getElementById js/document
                                                     "new-story-add-img-input"))}]]]
   [:div
    [:input#new-story-add-img-input
     {:type "file"
      :accept "image/*"
      :on-change
      (fn [e]
        (let [f (aget (-> e .-target .-files) 0)
              img (js/Image.)
              fr (js/FileReader.)]
          (set! (.-onload fr)
                (fn [ev] (set! (.-src img) (-> ev .-target .-result))))
          (set! (.-onload img)
                (fn [ev] (add-img img f)))
          (.readAsDataURL fr f)))}]]])

(defn category []
  [ui/select
   {:placeholder "Select a category"
    :on-change #(rf/dispatch [:new-story/set-category
                              (-> %2 .-value)])
    :fluid true
    :scrolling true
    :options (clj->js
              (for [c @(rf/subscribe [:new-story/categories])]
                {:key (:_key c)
                 :value (:name c)
                 :text (:name c)
                 :label (r/as-element
                         [ui/label {:content (-> c :name first .toUpperCase)
                                    :color (:color c)
                                    :circular true}])}))}])

(defn date []
  [ui/grid {:columns 16}
   [ui/grid-column {:width "7"}
    [ui/input {:type "date"
               :value @(rf/subscribe [:new-story/date])
               :fluid true
               :on-change #(rf/dispatch [:new-story/set-date
                                         (-> % .-target .-value)])}]]
   [ui/grid-column {:width "1"}]
   [ui/grid-column {:width "7"}
    [ui/input {:type "time"
               :value @(rf/subscribe [:new-story/time])
               :fluid true
               :on-change #(rf/dispatch [:new-story/set-time
                                         (-> % .-target .-value)])}]]])

(defn action-btns []
  [ui/grid
   [ui/grid-column {:width "1"}]
   [ui/grid-column {:width "6"}
    [ui/button {:fluid true
                :size "large"
                :color "teal"
                :content "Save"
                :icon "save"
                :on-click #(rf/dispatch [:new-story/validate])}]]
   [ui/grid-column {:width "2"}]
   [ui/grid-column {:width "6"}
    [ui/button {:fluid true
                :size "large"
                :content "Reset"
                :icon "undo"
                :on-click #(rf/dispatch [:new-story/reset])}]]
   [ui/grid-column {:width "1"}]])

(defn saving-segment []
  (op/operation-segment
   {:state @(rf/subscribe [:new-story/saving-state])
    :title "Publish A Story"
    :progress  @(rf/subscribe [:new-story/progress])
    :sub-title "Publishing your story"
    :m-success {:sub-title "Your Story Has Been Published"
                :on-click #(do (secretary/dispatch! "/home")
                               (rf/dispatch [:new-story/reset]))
                :btn-txt "Back To Home"
                :done-msg "Your story is now public and everyone can rate it and interact with it."}
    :m-error {:sub-title "Something Went Wrong !"
              :on-click #(rf/dispatch [:new-story/set-phase :editing])
              :btn-txt "Back"
              :done-msg @(rf/subscribe [:new-story/saving-msg])}}))

(defn editing-segment []
  [:div
   [ui/segment {:attached (if @(rf/subscribe [:new-story/not-valid?])
                            "top"
                            false)}
    [ui/header {:as "h2"
                :text-align "left"
                :color "teal"}
     [ui/icon {:name "pencil"}]
     [ui/header-content
      "New Story"
      [ui/header-subheader
       "Create a new story about a recent event or accident"]]]
    [ui/divider]
    [title]
    [ui/divider {:hidden true}]
    [location-map]
    [ui/divider {:hidden true}]
    [images]
    [ui/divider {:hidden true}]
    [ui/grid {:columns 16}
     [ui/grid-column {:width "6"}
      [category]]
     [ui/grid-column {:width "10"}
      [date]]]
    [ui/divider {:hidden true}]
    [description]
    [ui/divider {:hidden true}]
    [action-btns]]
   (when @(rf/subscribe [:new-story/not-valid?])
     [ui/message {:attached "bottom"
                  :negative true
                  :header "Validation Errors"
                  :list @(rf/subscribe [:new-story/errors])}])])

(defn new-story-panel []
  (let [phase @(rf/subscribe [:new-story/phase])]
    [:div.new-story-panel
     (if (= phase :saving)
       [saving-segment]
       [editing-segment])]))
