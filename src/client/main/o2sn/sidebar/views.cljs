(ns o2sn.sidebar.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]))

(defn menu-item [{:keys [page label icon]}]
  (let [active? @(rf/subscribe [:sidebar/active? page])]
    [:> ui/Menu.Item {:name label
                      :link true
                      :active active?
                      :disabled active?
                      :on-click #(rf/dispatch [:sidebar/activate page])}
     [:> ui/Icon {:name icon}] label]))

(defn side-bar [panel]
  [:> ui/Sidebar.Pushable {:as (r/as-element ui/Segment)}
   [:> ui/Sidebar {:as (r/as-element ui/Menu)
                   :animation "push"
                   :width "thin"
                   :visible @(rf/subscribe [:sidebar/visible?])
                   :icon "labeled"
                   :vertical true
                   :color "teal"
                   :inverted true}
    [menu-item {:page  :home :label "Home" :icon  "home"}]
    [menu-item {:page :list-channels :label "Channels" :icon "podcast"}]
    [menu-item {:page :my-profile :label "Profile" :icon "user"}]
    [menu-item {:page :messages :label "Messages" :icon "envelope"}]
    [menu-item {:page :friends :label "Friends" :icon "users"}]
    [menu-item {:page :settings :label "Settings" :icon "settings"}]]
   [:> ui/Sidebar.Pusher
    [:> ui/Segment {:basic true}
     panel]]])
