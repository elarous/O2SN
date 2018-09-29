(ns o2sn.channels.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :channels/all
 (fn [db _]
   (get-in db [:channels :all])))

(reg-sub
 :channels/color
 (fn [db [_ t]]
   (get-in db [:channels :color t])))

(reg-sub
 :channels/color-by-k
 (fn [db [_ k]]
   (if-let [c (some #(and (= (:_key %) k) %) (get-in db [:channels :all]))]
     (get-in db [:channels :color (:type c)])
     "grey")))

(reg-sub
 :channels/confirm-visible?
 (fn [db [_ chan-k]]
   (get-in db [:channels :confirm-visible? chan-k])))

(reg-sub
 :channels/locations
 (fn [db _]
   (get-in db [:channels :locations])))

(reg-sub
 :channels/location-selected?
 (fn [db _]
   (get-in db [:channels :location-selected?])))
