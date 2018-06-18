(ns o2sn.views.sidebar
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]))

(defn side-bar [panel]
  [ui/sidebar-pushable {:as (ui/component "Segment")}
   [ui/sidebar {:as (ui/component "Menu")
                :animation "push"
                :width "thin"
                :visible @(rf/subscribe [:sidebar-visible])
                :icon "labeled"
                :vertical true
                :color "teal"
                :inverted true}
    [ui/menu-item {:name "home"
                   :link true
                   :active @(rf/subscribe [:active-panel? :home])
                   :on-click #(secretary/dispatch! "/home")}
     [ui/icon {:name "home"}]
     "Home"]
    [ui/menu-item {:name "channels"
                   :link true
                   :active @(rf/subscribe [:active-panel? :channels])}
     [ui/icon {:name "podcast"}]
     "Channels"]
    [ui/menu-item {:name "messages"
                   :link true
                   :active @(rf/subscribe [:active-panel? :messages])
                   :on-click #(secretary/dispatch! "/messages")}
     [ui/icon {:name "envelope"}]
     "Messages"]

    [ui/menu-item {:name "friends"
                   :active @(rf/subscribe [:active-panel? :friends])
                   :link true}
     [ui/icon {:name "users"}]
     "Friends"]

    [ui/menu-item {:name "settings"
                   :active @(rf/subscribe [:active-panel? :settings])
                   :link true}
     [ui/icon {:name "settings"}]
     "Settings"]]
   [ui/sidebar-pusher
    [ui/segment {:basic true}
     panel]]])
