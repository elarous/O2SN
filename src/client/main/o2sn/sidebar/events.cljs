(ns o2sn.sidebar.events
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]))

(reg-event-fx
 :sidebar/activate
 (fn [{db :db} [_ page-k]]
   {:navigate-to [page-k]
    :db (assoc-in db [:sidebar :active?] page-k)}))


