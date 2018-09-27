(ns o2sn.logout.events
  (:require [kee-frame.core :refer [reg-event-db
                                    reg-event-fx]]
            [re-frame.core :refer [path]]
            [ajax.core :as ajax]))

(reg-event-db
 :logout/logout
 [(path :user)]
 (fn [user _]
   (-> user
       (assoc :current nil)
       (assoc :token nil))))
