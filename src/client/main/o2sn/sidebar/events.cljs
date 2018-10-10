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

(reg-event-fx
 :sidebar/start-loading
 (fn [{db :db} _]
   {:db (assoc-in db [:sidebar :loading?] true)
    :timeout {:id :sidebar/loading
              :time 600
              :event [:sidebar/stop-loading]}}))

(reg-event-db
 :sidebar/stop-loading
 (fn [db _]
   (assoc-in db [:sidebar :loading?] false)))

