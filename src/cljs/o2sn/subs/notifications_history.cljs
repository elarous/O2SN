(ns o2sn.subs.notifications-history
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :notifs-history/all
 (fn [db _]
   (->> (get-in db [:notifications :all])
        (sort-by :_key)
        reverse)))


