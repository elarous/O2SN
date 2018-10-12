(ns o2sn.sidebar.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]))

(def menu-items [{:page :home
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
                  :desc "Customize Your Settings"}])

(defn sidebar-overlay [loading?]
  (let [{:keys [label icon desc]} @(rf/subscribe [:sidebar/hovered-page])
        has-hover? @(rf/subscribe [:sidebar/has-hover?])]
    (js/console.log "label : " label)
    (when (or has-hover? loading?)
      [:div#sidebar-overlay
       (if loading?
         [:div#overlay-loading
          [:i.fas.fa-cog]]
         [:div#overlay-infos
          [:i {:class-name (str "icon fas fa-" icon)}]
          [:div.label label]
          [:div.desc desc]])])))

(defn menu-item [{:keys [page label icon desc] :as m}]
  (let [active? @(rf/subscribe [:sidebar/active? page])
        visible? @(rf/subscribe [:sidebar/visible?])
        loading? @(rf/subscribe [:sidebar/loading?])]
    [:div
     {:class-name (str "menu-item "
                       (when active? "menu-item-active"))
      :on-click (fn [e]
                  (when-not active?
                    (rf/dispatch [:sidebar/start-loading page])
                    (rf/dispatch [:sidebar/reset-hover])))
      :on-mouse-enter #(when-not loading?
                         (rf/dispatch [:sidebar/show-hover m]))
      :on-mouse-leave #(when @(rf/subscribe [:sidebar/has-hover?])
                         (rf/dispatch [:sidebar/reset-hover]))}
     [:i {:class-name (str "icon fas fa-" icon)}]
     [:div.label label]]))

(defn side-bar [panel]
  (let [visible? @(rf/subscribe [:sidebar/visible?])
        loading? @(rf/subscribe [:sidebar/loading?])]
    [:div#sidebar-page
     (if visible?
       [:div#sidebar
        (for [m menu-items]
          ^{:key (:page m)}
          [menu-item m])])
     [:div
      (if visible?
        {:id "panel-container-menu"}
        {:id "panel-container-full"})
      [:div#panel-container
       panel
       [sidebar-overlay loading?]]]]))
