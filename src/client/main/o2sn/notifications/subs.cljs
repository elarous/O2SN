(ns o2sn.notifications.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :notifs/all
 (fn [db _]
   (->> (get-in db [:notifications :all])
        (sort-by :_key)
        reverse)))

(reg-sub
 :notifs/opened?
 (fn [db _]
   (get-in db [:notifications :opened?])))

(reg-sub
 :notifs/unreads
 (fn [db _]
   (->> (get-in db [:notifications :unreads])
        (sort-by :_key)
        reverse)))

(reg-sub
 :notifs/unreads-count
 (fn [db _]
   (count (get-in db [:notifications :unreads]))))

(reg-sub
 :notifs/alerts
 (fn [db _]
   (get-in db [:notifications :alerts])))
