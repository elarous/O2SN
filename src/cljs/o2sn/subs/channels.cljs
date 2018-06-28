(ns o2sn.subs.channels
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

(reg-sub
 :channels/tab
 (fn [db _]
   (get-in db [:channels :active-tab])))

(reg-sub
 :channels/saving-visible?
 (fn [db _]
   (get-in db [:channels :saving :visible])))

(reg-sub
 :channels/progress
 (fn [db _]
   (get-in db [:channels :saving :progress])))

(reg-sub
 :channels/saving-state
 (fn [db _]
   (get-in db [:channels :saving :state])))

(reg-sub
 :channels/error-msg
 (fn [db _]
   (get-in db [:channels :saving :error-msg])))
