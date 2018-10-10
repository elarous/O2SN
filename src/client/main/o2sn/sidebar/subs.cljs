(ns o2sn.sidebar.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :sidebar/visible?
 (fn [db _]
   (get-in db [:sidebar :visible?])))

(reg-sub
 :sidebar/active?
 (fn [db [_ page-k]]
   (= (get-in db [:kee-frame/route :handler]) page-k)))

(reg-sub
 :sidebar/loading?
 (fn [db _]
   (get-in db [:sidebar :loading?])))
