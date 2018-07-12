(ns o2sn.views.search
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]))

(defn story-result [s]
  ^{:key (:_id s)}
  [:div
   [ui/header {:color "teal"
               :as "h4"
               :style {:margin "3px"}}
    (:title s)]
   [ui/header {:color "grey"
               :as "h5"
               :style {:margin "3px"
                       :font-weight "normal"}}
    (:channel s)]])

(defn user-result [u]
  ^{:key (:_id u)}
  [ui/grid
   [ui/grid-column {:width 4}
    [ui/image {:src (str "avatars/" (:avatar u))
               :style {:max-width "40px"
                       :max-height "40px"}}]]
   [ui/grid-column {:width 12}
    [ui/header {:color "teal"
                :as "h4"
                :style {:margin "3px"}}
     (:fullname u)]
    [ui/header {:color "grey"
                :as "h5"
                :style {:margin "3px"
                        :font-weight "normal"}}
     (str "@" (:username u))]]])

(defn search-result [r]
  #_(js/console.log r)
  (if (= (:type r) "story")
    [story-result r]
    [user-result r]))


(defn search-input []
  [:div
   [ui/search {:class-name "top-menu-search"
               :size "small"
               :category true
               :value @(rf/subscribe [:search/value])
               :results @(rf/subscribe [:search/content])
               :loading @(rf/subscribe [:search/loading?])
               :on-search-change #(rf/dispatch [:search/set-value
                                                (-> %2 .-value)])
               :on-result-select #(rf/dispatch [:search/view-result
                                                (-> %2 .-result)])
               :result-renderer
               (fn [props]
                 #_(js/console.log (js->clj props :keywordize-keys true))
                 (let [p (js->clj props :keywordize-keys true)]
                   (r/as-element
                    [search-result p])))}]])

#_(def rs {"stories"
        {:name "stories",
         :results
         [{:_id "stories/1"
           :type :story
           :title "the first story here"
           :channel "el kelaa des sraghna"}
          {:_id "stories/2"
           :type :story
           :title "the second story here"
           :channel "el kelaa des sraghna"}]},
        "users"
        {:name "users",
         :results
         [{:_id "users/1"
           :type :user
           :fullname "el arbaoui oussama"
           :username "oussama"
           :avatar "myAvatar.svg"}
          {:_id "users/2"
           :type :user
           :fullname "el arbaoui oussama"
           :username "oussama"
           :avatar "myAvatar.svg"}]}})
