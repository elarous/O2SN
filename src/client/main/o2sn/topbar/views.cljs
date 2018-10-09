(ns o2sn.topbar.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [o2sn.notifications.views :refer [menu-notifications]]))

;; search components

#_(defn story-result [s]
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

(defn story-result [s]
  ^{:key (:_id s)}
  [:div.story-result
   [:div.story-title (:title s)]
   [:div.story-channel (:channel s)]])

(defn user-result [u]
  ^{:key (:_id u)}
  [:div.user-result
   [:div.user-img
    [:img {:src "img/user.svg"}]]
   [:div.user-names
    [:div.user-fullname (:fullname u)]
    [:div.user-username (str "@" (:username u))]]])

(defn search-result [r]
  (if (= (:type r) "story")
    [story-result r]
    [user-result r]))

(defn top-menu-search []
  [:> ui/Search {:id "search"
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
                      [search-result p])))}])

(defn top-menu-sidebar-btn []
  [:> ui/Icon {:name "sidebar"
               :size "large"
               :link true
               :id "btn"
               :on-click #(rf/dispatch [:topbar/toggle-sidebar])}])

(defn top-menu-logo []
  [:h1 "O2SN"])

(defn top-menu-feed []
  [:> ui/Popup
   {:id "notifs-popup"
    :hoverable true
    :position "bottom right"
    :on "click"
    :style {:height "auto" :min-width "260px"}
                ;; :open @(rf/subscribe [:topbar/notifs-open?])
                ;; :on-open #(rf/dispatch [:notifs/get-unreads])
                ;; :on-close identity
    :trigger
    (r/as-element
     [:div {:id "notifs-container"}
      [:div {:id "notifs-count"}
       @(rf/subscribe [:notifs/unreads-count])]
      [:> ui/Icon
       {:name "bell"
        :size "large"
        :link true}]])}
   [menu-notifications]])

(defn top-menu-messages []
  [:> ui/Popup
   {:id "messages-popup"
    :hoverable true
    :position "bottom right"
    :style {:height "auto"}
    :on "click"
    :trigger (r/as-element
              [:span
               [:> ui/Icon {:name "envelope"
                            :size "large"
                            :link true
                            :on-click #(rf/dispatch [:notifs/connect-ws])}]])}
   [:> ui/Feed
    [:> ui/Feed.Event {:image "img/user.svg"
                       :content "my first message"}]
    [:> ui/Feed.Event {:image "img/user.svg"
                       :content "my second message"}]]])

(defn top-menu-add []
  [:> ui/Popup
   {:id "add-popup"
    :hoverable true
    :position "bottom right"
    :style {:height "auto"}
    :on "click"
    :open @(rf/subscribe [:topbar/add-open?])
    :on-open #(rf/dispatch [:topbar/open-add])
    :on-close #(rf/dispatch [:topbar/close-add])
    :trigger (r/as-element
              [:span
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
  [:div#actions
   [top-menu-add]
   [top-menu-feed]
   [top-menu-messages]
   [:div#user-dropdown
    [:> ui/Dropdown
     {:icon false
      :pointing "top right"
      :trigger
      (r/as-element
       [:img#user-img {:src "img/user.svg"}])}
     [:> ui/Dropdown.Menu
      [:> ui/Dropdown.Item {:icon "user"
                            :text "my profile"
                            :on-click #(rf/dispatch [:navigate :my-profile])}]
      [:> ui/Dropdown.Item {:icon "sign out"
                            :text "logout"
                            :on-click #(rf/dispatch [:logout/logout])}]]]]])

(defn main-menu []
  [:div#top-menu
   [:div#toggle-btn
    [top-menu-sidebar-btn]]
   [:div#logo
    [top-menu-logo]]
   [:div#search-container
    [top-menu-search]]
   [top-menu-actions]])
