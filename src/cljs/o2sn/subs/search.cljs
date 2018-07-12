(ns o2sn.subs.search
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :search/value
 (fn [db _]
   (get-in db [:search :value])))

(reg-sub
 :search/content
 (fn [db _]
   (-> (get-in db [:search :content])
       clj->js)))

(reg-sub
 :search/loading?
 (fn [db _]
   (get-in db [:search :loading?])))
