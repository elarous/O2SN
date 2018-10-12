(ns o2sn.sidebar.events
  (:require [kee-frame.core :refer [reg-event-fx
                                   reg-event-db]]))

(reg-event-fx
 :sidebar/activate
 (fn [{db :db} [page-k]]
   {:navigate-to [page-k]}))

(reg-event-fx
 :sidebar/start-loading
 (fn [{db :db} [page]]
   {:db (assoc-in db [:sidebar :overlay :loading?] true)
    :dispatch [:sidebar/activate page]}))

(reg-event-db
 :sidebar/stop-loading
 (fn [db _]
   (assoc-in db [:sidebar :overlay :loading?] false)))

(reg-event-db
 :sidebar/show-hover
 (fn [db [m]]
   (assoc-in db [:sidebar :overlay :hovered-page] m)))

(reg-event-db
 :sidebar/reset-hover
 (fn [db _]
   (assoc-in db [:sidebar :overlay :hovered-page] nil)))

