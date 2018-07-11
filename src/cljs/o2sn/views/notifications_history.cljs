(ns o2sn.views.notifications-history
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [o2sn.ui :as ui]
            [o2sn.views.notifications :as view]))


(defn notifs-history-panel []
  [ui/segment
   [ui/container {:text-align "center"}
    [ui/header {:as "h1"
                :color "teal"
                :style {:display "inline"}}
     [ui/icon {:name "bell"}]
     [ui/header-content "All Notifications"]]]
   [ui/divider]
   [:div {:style {:display "flex"
                  :flex-flow "row wrap"
                  :justify-content "center"}}
    (let [all-notifs @(rf/subscribe [:notifs/all])]
      (if (seq all-notifs)
        (for [notif all-notifs]
          ^{:key (:_key notif)}
          [:div {:style {:margin "15px"
                         :max-width "15%"}}
           (view/notif-segment notif)])
        [ui/header {:as "h3"
                    :color "grey"
                    :content "No Notifications Available Yet"}]))]])
