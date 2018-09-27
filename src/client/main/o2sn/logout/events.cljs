(ns o2sn.logout.events
  (:require [kee-frame.core :refer [reg-event-db
                                    reg-event-fx]]
            [re-frame.core :refer [path]]
            [ajax.core :as ajax]))

(reg-event-fx
 :logout/logout
 (fn [_ _]
   {:dispatch [:reset]}))
