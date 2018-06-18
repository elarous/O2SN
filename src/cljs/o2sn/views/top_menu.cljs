(ns o2sn.views.top-menu
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]))

(defn top-menu-sidebar-btn []
  [:div
   [ui/icon {:name "sidebar"
             :size "large"
             :link true
             :on-click #(rf/dispatch [:toggle-sidebar])}]])

(defn top-menu-logo []
  [:div
   [:h4 "O2SN LOGO"]])

(defn top-menu-search []
  [:div
   [ui/search {:class-name "top-menu-search"
               :size "small"}]])

(defn top-menu-feed []
  [ui/popup {:hoverable true
             :position "bottom right"
             :style {:height "auto"}
             :trigger (r/as-element
                       [:span.menu-action
                        [ui/icon {:name "feed"
                                  :size "large"
                                  :link true}]])}
   [ui/feed
    [ui/feed-event {:icon "pencil"
                    :date "Today"
                    :summary "my first event ever"}]
    [ui/feed-event {:icon "pencil"
                    :date "Today"
                    :summary "my second event ever"}]]])


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
  [ui/dropdown
   {:icon false
    :pointing "top right"
    :trigger (r/as-element
              [ui/icon {:name  "plus"
                        :size "large"}])}
   [ui/dropdown-menu
    [ui/dropdown-item {:icon "file"
                       :text "New Story"
                       :on-click #(secretary/dispatch! "#/story/new")}]]])


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
                [ui/image {:src "img/myAvatar.svg"
                           :avatar true
                           :class-name "top-menu-avatar"}])}
     [ui/dropdown-menu
      [ui/dropdown-item {:icon "user"
                         :text "my profile"
                         :on-click #(js/alert "to my profile ... ")}]
      [ui/dropdown-item {:icon "sign out"
                         :text "logout"
                         :on-click #(js/alert "logging out ...")}]]]]])

(defn main-menu []
  [ui/segment {:class-name "top-menu"}
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
