(ns o2sn.sidebar.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]))

(defn sidebar-overlay [{:keys [label icon desc]}]
  [:div#sidebar-overlay
   [:i {:class-name (str "icon fas fa-" icon)}]
   [:div.label label]
   [:div.desc desc]])

(defn sidebar-loading []
  [:div#sidebar-loading
   [:i.fas.fa-cog]])

(defn side-bar [panel]
  (let [hovered-page (r/atom nil)
        set-hovered (fn [page-map]
                      (reset! hovered-page page-map))
        items [{:page :home
                :label "Home"
                :icon  "home"
                :desc "Get News By Channel"}
               {:page :list-channels
                :label "Channels"
                :icon "podcast"
                :desc "List, Subscribe And Unsubscribe To News Channels"}
               {:page :my-profile
                :label "Profile"
                :icon "user"
                :desc "View And Edit Your Profile"}
               {:page :messages
                :label "Messages"
                :icon "envelope"
                :desc "View And Send Messages To Other Users"}
               {:page :friends
                :label "Friends"
                :icon "users"
                :desc "View And Follow Friends"}
               {:page :settings
                :label "Settings"
                :icon "cogs"
                :desc "Customize Your Settings"}]]
    (fn [panel]
      (let [visible? @(rf/subscribe [:sidebar/visible?])]
        [:div#sidebar-page
         (if visible?
           [:div#sidebar
            (for [{:keys [page label icon desc] :as m} items]
              ^{:key page}
              [:div
               {:class-name (str "menu-item "
                                 (if @(rf/subscribe [:sidebar/active? page])
                                   "menu-item-active" ""))
                :on-click (fn [e]
                            (rf/dispatch [:sidebar/activate page])
                            (rf/dispatch [:sidebar/start-loading])
                            (reset! hovered-page nil))
                :on-mouse-enter #(when-not @(rf/subscribe [:sidebar/loading?])
                                  (reset! hovered-page m))
                :on-mouse-leave #(when (some? @hovered-page)
                                 (reset! hovered-page nil))}
               [:i {:class-name (str "icon fas fa-" icon)}]
               [:div.label label]])])
         [:div
          (if visible?
            {:id "panel-container-menu"}
            {:id "panel-container-full"})
          [:div#panel-container
           panel
           (cond @(rf/subscribe [:sidebar/loading?]) [sidebar-loading]
                 (some? @hovered-page) [sidebar-overlay @hovered-page])]]]))))
