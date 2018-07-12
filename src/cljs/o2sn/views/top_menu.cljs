(ns o2sn.views.top-menu
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]
            [o2sn.views.notifications :as notifications]
            [o2sn.views.search :as search]))

(defn top-menu-sidebar-btn []
  [:div
   [ui/icon {:name "sidebar"
             :size "large"
             :link true
             :on-click #(rf/dispatch [:toggle-sidebar])}]])

(defn top-menu-logo []
  [:div
   [:h3 "O2SN"]])

(defn top-menu-search []
  [search/search-input])

(defn top-menu-feed []
  [ui/popup {:hoverable true
             :position "bottom right"
             :style {:height "auto"
                     :min-width "260px"}
             :open @(rf/subscribe [:notifs/opened?])
             :on-open #(rf/dispatch [:notifs/open-notifs])
             :on-close #(rf/dispatch [:notifs/close-notifs])
             :trigger
             (r/as-element
              [:span.menu-action
               [ui/icon
                {:name "bell"
                 :size "large"
                 :link true
                 :color (when (pos? @(rf/subscribe [:notifs/unreads-count]))
                          "yellow")
                 :inverted true
                 :circular (pos? @(rf/subscribe [:notifs/unreads-count]))
                 :on-click
                 #(do
                    (rf/dispatch [:set-active-panel :notifs-history])
                    (rf/dispatch [:notifs-history/get-notifs]))}]])}
   [notifications/menu-notifications]])

(defn top-menu-messages []
  [ui/popup {:hoverable true
             :position "bottom right"
             :style {:height "auto"}
             :trigger (r/as-element
                       [:span.menu-action
                        [ui/icon {:name "envelope"
                                  :size "large"
                                  :link true}]])}
   [ui/feed
    [ui/feed-event {:image "img/myAvatar.svg"
                    :content "my first message"}]
    [ui/feed-event {:image "img/myAvatar.svg"
                    :content "my second message"}]]])

(defn top-menu-add []
  [ui/popup {:hoverable true
             :position "bottom right"
             :style {:height "auto"}
             :trigger (r/as-element
                       [:span.menu-action
                        [ui/icon {:name "plus"
                                  :size "large"
                                  :link true}]])}
   [ui/menu {:compact true
             :icon "labeled"}
    [ui/menu-item {:name "new-story"
                   :on-click #(secretary/dispatch! "/story/new")}
     [ui/icon {:name "file"}]
     "New Story"]
    [ui/menu-item {:name "new-channel"
                   :on-click #(secretary/dispatch! "/channel/add")}
     [ui/icon {:name "tv"}]
     "New Channel"]]])

(defn top-menu-actions []
  [:div.top-menu-actions
   [top-menu-add]
   [top-menu-feed]
   [top-menu-messages]
   [:span.menu-action
    [ui/dropdown
     {:icon false
      :pointing "top right"
      :trigger (r/as-element
                [ui/image {:src @(rf/subscribe [:user-avatar])
                           :avatar true
                           :class-name "top-menu-avatar"}])}
     [ui/dropdown-menu
      [ui/dropdown-item {:icon "user"
                         :text "my profile"
                         :on-click #(do (secretary/dispatch! "/profile")
                                        (rf/dispatch [:profile/load]))}]
      [ui/dropdown-item {:icon "sign out"
                         :text "logout"
                         :on-click #(secretary/dispatch! "/logout")}]]]]])

(defn main-menu []
  [ui/segment {:id "top-menu"
               :color "teal"
               :inverted true}
   [ui/grid {:columns 16
             :vertical-align "middle"
             :padded "horizontally"}
    [ui/grid-column {:width 2}
     [top-menu-sidebar-btn]]

    [ui/grid-column {:width 2}
     [top-menu-logo]]

    [ui/grid-column {:width 9}
     [top-menu-search]]

    [ui/grid-column {:width 3}
     [top-menu-actions]]]])
