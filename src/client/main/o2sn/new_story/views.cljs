(ns o2sn.new-story.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [o2sn.maps.views :as m]
            [o2sn.operation.views :as op]
            [o2sn.common.views :refer [error-label]]))

(defonce title-v (r/atom ""))
(defonce desc-v (r/atom ""))

(defn title [all-valid?]
  (let [valid? @(rf/subscribe [:new-story/title-valid?])]
    [:> ui/Form.Field
     {:style {:text-align "center"}}
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:new-story/title-error])
                   :path-to-ctrl [:new-story :title]}]
     [:> ui/Form.Input
      {:fluid true
       :loading @(rf/subscribe [:new-story/title-validating?])
       :autoFocus true
       :error (or (not valid?) (not all-valid?))
       :value @title-v
       :placeholder "Story's Title"
       :on-change #(do (reset! title-v (-> % .-target .-value))
                       (rf/dispatch [:new-story/validate-title
                                     (-> % .-target .-value)]))
       :on-blur #(rf/dispatch [:new-story/set-title @title-v])}]]))

(defn description [all-valid?]
  (let [valid? @(rf/subscribe [:new-story/desc-valid?])]
    [:> ui/Form.Field
     {:style {:text-align "center"}
      :error (or (not valid?) (not all-valid?))}
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:new-story/desc-error])
                   :path-to-ctrl [:new-story :description]}]
     [:> ui/TextArea
      {:fluid true
       :auto-height true
       :icon-position "left"
       :rows 2
       :style {:min-height "50px"
               :width "100%"}
       :value @desc-v
       :placeholder "Story's Description"
       :on-change #(do (reset! desc-v (-> % .-target .-value))
                       (rf/dispatch [:new-story/validate-desc
                                     (-> % .-target .-value)]))
       :on-blur #(rf/dispatch [:new-story/set-desc @desc-v])}]]))

(defn location-map []
  [:div#map
   [m/wrapped-map {:lat @(rf/subscribe [:new-story/marker-lat])
                   :lng @(rf/subscribe [:new-story/marker-lng])
                   :on-click (fn [e]
                               (when-let [latLng (goog.object/get e "latLng")]
                                 (let [lat-fn (goog.object/get latLng "lat")
                                       lng-fn (goog.object/get latLng "lng")]
                                   (rf/dispatch [:new-story/move-marker
                                                 (hash-map :lat (lat-fn)
                                                           :lng (lng-fn))]))))}]])

(defn- add-img [img file]
  (rf/dispatch [:new-story/add-img {:img img :file file}]))

(defn images []
  [:div
   [:> ui/Segment
    [:div.new-story-images
     (let [imgs @(rf/subscribe [:new-story/images])]
       (if (pos? (count imgs))
         (for [img @(rf/subscribe [:new-story/images])]
           ^{:key (:fname img)}
           [:> ui/Image {:src (.-src (:img img))
                         :label (clj->js
                                 {:as "a"
                                  :corner "right"
                                  :icon "trash"
                                  :onClick #(rf/dispatch [:new-story/remove-img
                                                          (:fname img)])})}])
         [:> ui/Header {:as "h5"
                        :text-align "center"
                        :color "grey"
                        :content "No Image Selected"}]))]]
   [:div
    [:> ui/Container {:text-align "center"}
     [:> ui/Button {:content "Add Picture"
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

(defn category [all-valid?]
  (let [valid? @(rf/subscribe [:new-story/category-valid?])]
    [:> ui/Form.Field
     [error-label {:valid? valid?
                   :message @(rf/subscribe [:new-story/category-error])
                   :path-to-ctrl [:new-story :category]}]
     [:> ui/Select
      {:placeholder "Select a category"
       :on-change #(do (rf/dispatch [:new-story/set-category
                                     (-> %2 .-value)])
                       (rf/dispatch [:new-story/validate-category
                                     (-> %2 .-value)]))
       :error (or (not valid?) (not all-valid?))
       :fluid true
       :scrolling true
       :options (clj->js
                 (for [c @(rf/subscribe [:new-story/categories])]
                   {:key (:_key c)
                    :value (:name c)
                    :text (:name c)
                    :label (r/as-element
                            [:> ui/Label {:content (-> c :name first .toUpperCase)
                                          :color (:color c)
                                          :circular true}])}))}]]))

(defn date [all-valid?]
  [:> ui/Grid {:columns 16}
   [:> ui/Grid.Column {:width "7"}
    [:> ui/Input {:type "date"
                  :value @(rf/subscribe [:new-story/date])
                  :fluid true
                  :error (not all-valid?)
                  :on-change #(rf/dispatch [:new-story/set-date
                                            (-> % .-target .-value)])}]]
   [:> ui/Grid.Column {:width "1"}]
   [:> ui/Grid.Column {:width "7"}
    [:> ui/Input {:type "time"
                  :value @(rf/subscribe [:new-story/time])
                  :fluid true
                  :error (not all-valid?)
                  :on-change #(rf/dispatch [:new-story/set-time
                                            (-> % .-target .-value)])}]]])

(defn action-btns [all-valid?]
  [:> ui/Grid
   [:> ui/Grid.Column {:width "1"}]
   [:> ui/Grid.Column {:width "6"}
    [:> ui/Button {:fluid true
                   :size "large"
                   :color (if all-valid? "teal" "red")
                   :content "Save"
                   :icon "save"
                   :disabled (not all-valid?)
                   :on-click #(rf/dispatch [:new-story/validate-all])}]]
   [:> ui/Grid.Column {:width "2"}]
   [:> ui/Grid.Column {:width "6"}
    [:> ui/Button {:fluid true
                   :size "large"
                   :content "Reset"
                   :icon "undo"
                   :on-click #(do (rf/dispatch [:new-story/reset])
                                  (reset! title-v "")
                                  (reset! desc-v ""))}]]
   [:> ui/Grid.Column {:width "1"}]])

(defn new-story-page []
  (let [all-valid? @(rf/subscribe [:new-story/valid?])]
    [:div
     [:> ui/Segment {:attached (if (not all-valid?)
                                 "top"
                                 false)}
      [:> ui/Header {:as "h2"
                     :text-align "left"
                     :color (if all-valid? "teal" "red")}
       [:> ui/Icon {:name "pencil"}]
       [:> ui/Header.Content
        "New Story"
        [:> ui/Header.Subheader
         "Create a new story about a recent event or accident"]]]
      [:> ui/Form
       {:error (not all-valid?)}
       [:> ui/Divider]
       [title all-valid?]
       [:> ui/Divider {:hidden true}]
       [location-map]
       [:> ui/Divider {:hidden true}]
       [images]
       [:> ui/Divider {:hidden true}]
       [:> ui/Grid {:columns 16}
        [:> ui/Grid.Column {:width "6"}
         [category all-valid?]]
        [:> ui/Grid.Column {:width "10"}
         [date all-valid?]]]
       [:> ui/Divider {:hidden true}]
       [description all-valid?]
       [:> ui/Divider {:hidden true}]
       [action-btns all-valid?]]]
     (when (not all-valid?)
       [:> ui/Message {:attached "bottom"
                       :negative true}
        [:> ui/Message.Header "Validation Errors"]
        (for [item @(rf/subscribe [:new-story/errors])]
          [:> ui/Message.Item
           [:span {:style {:font-weight "bold"
                           :font-size "1.1rem"
                           :font-style "italic"}}
            (-> item first name clojure.string/capitalize)] " : " (second item)])])]))

