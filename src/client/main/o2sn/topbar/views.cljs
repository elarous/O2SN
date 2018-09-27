(ns o2sn.topbar.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]))

;; search components

(defn story-result [s]
  ^{:key (:_id s)}
  [:div
   [:> ui/Header {:color "teal"
                  :as "h4"
                  :style {:margin "3px"}}
    (:title s)]
   [:> ui/Header {:color "grey"
                  :as "h5"
                  :style {:margin "3px"
                          :font-weight "normal"}}
    (:channel s)]])

(defn user-result [u]
  ^{:key (:_id u)}
  [:> ui/Grid
   [:> ui/Grid.Column {:width 4}
    [:> ui/Image {:src "img/user.svg"
                  :style {:max-width "40px"
                          :max-height "40px"}}]]
   [:> ui/Grid.Column {:width 12}
    [:> ui/Header {:color "teal"
                   :as "h4"
                   :style {:margin "3px"}}
     (:fullname u)]
    [:> ui/Header {:color "grey"
                   :as "h5"
                   :style {:margin "3px"
                           :font-weight "normal"}}
     (str "@" (:username u))]]])

(defn search-result [r]
  (if (= (:type r) "story")
    [story-result r]
    [user-result r]))

(defn search-input []
  [:div
   [:> ui/Search {:class-name "top-menu-search"
                  :size "small"
                  :category true
                  :min-characters 3
                  :value @(rf/subscribe [:search/value])
                  :results @(rf/subscribe [:search/content])
                  :loading @(rf/subscribe [:search/loading?])
                  :on-search-change #(rf/dispatch [:search/set-value
                                                   (-> %2 .-value)])
                  :on-result-select #(rf/dispatch [:search/view-result
                                                   (-> %2 .-result)])
                  :result-renderer
                  (fn [props]
                    (let [p (js->clj props :keywordize-keys true)]
                      (r/as-element
                       [search-result p])))}]])

(defn top-menu-sidebar-btn []
  [:div
   [:> ui/Icon {:name "sidebar"
                :size "large"
                :link true
                :on-click #(rf/dispatch [:topbar/toggle-sidebar])}]])

(defn top-menu-logo []
  [:div
   [:h3 "O2SN"]])

(defn top-menu-search []
  [search-input])

(defn top-menu-feed []
  [:> ui/Popup {:hoverable true
                :position "bottom right"
                :style {:height "auto"
                        :min-width "260px"}
                :open false
                :on-open identity
                :on-close identity
                :trigger
                (r/as-element
                 [:span.menu-action
                  [:> ui/Icon
                   {:name "bell"
                    :size "large"
                    :link true
                    :color "yellow"
                    :inverted true
                    :circular true
                    :on-click identity}]])}
   #_[notifications/menu-notifications]])

(defn top-menu-messages []
  [:> ui/Popup {:hoverable true
                :position "bottom right"
                :style {:height "auto"}
                :trigger (r/as-element
                          [:span.menu-action
                           [:> ui/Icon {:name "envelope"
                                        :size "large"
                                        :link true}]])}
   [:> ui/Feed
    [:> ui/Feed.Event {:image "img/myAvatar.svg"
                       :content "my first message"}]
    [:> ui/Feed.Event {:image "img/myAvatar.svg"
                       :content "my second message"}]]])

(defn top-menu-add []
  [:> ui/Popup {:hoverable true
                :position "bottom right"
                :style {:height "auto"}
                :open @(rf/subscribe [:topbar/add-open?])
                :on-open #(rf/dispatch [:topbar/open-add])
                :on-close #(rf/dispatch [:topbar/close-add])
                :trigger (r/as-element
                          [:span.menu-action
                           [:> ui/Icon {:name "plus"
                                        :size "large"
                                        :link true}]])}
   [:> ui/Menu {:compact true
                :icon "labeled"}
    [:> ui/Menu.Item {:name "new-story"
                      :on-click (fn []
                                  (rf/dispatch [:navigate :new-story])
                                  (rf/dispatch [:topbar/close-add]))}
     [:> ui/Icon {:name "file"}]
     "New Story"]
    [:> ui/Menu.Item {:name "new-channel"
                      :on-click (fn []
                                  (rf/dispatch [:navigate :add-channel])
                                  (rf/dispatch [:topbar/close-add]))}
     [:> ui/Icon {:name "tv"}]
     "New Channel"]]])

(defn top-menu-actions []
  [:div.top-menu-actions
   [top-menu-add]
   [top-menu-feed]
   [top-menu-messages]
   [:span.menu-action
    [:> ui/Dropdown
     {:icon false
      :pointing "top right"
      :trigger
      (r/as-element
       [:> ui/Image {:src "img/user.svg"
                     :avatar true
                     :class-name "top-menu-avatar"}])}
     [:> ui/Dropdown.Menu
      [:> ui/Dropdown.Item {:icon "user"
                            :text "my profile"
                            :on-click #(rf/dispatch [:navigate :my-profile])}]
      [:> ui/Dropdown.Item {:icon "sign out"
                            :text "logout"
                            :on-click #(rf/dispatch [:logout/logout])}]]]]])

(defn main-menu []
  [:> ui/Segment {:id "top-menu"
                  :color "teal"
                  :inverted true
                  :style {:max-height "4rem"}}
   [:> ui/Grid {:columns 16
                :vertical-align "middle"
                :padded "horizontally"}
    [:> ui/Grid.Column {:width 2}
     [top-menu-sidebar-btn]]

    [:> ui/Grid.Column {:width 2}
     [top-menu-logo]]

    [:> ui/Grid.Column {:width 9}
     [top-menu-search]]

    [:> ui/Grid.Column {:width 3}
     [top-menu-actions]]]])
