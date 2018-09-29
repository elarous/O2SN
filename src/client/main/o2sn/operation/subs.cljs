(ns o2sn.operation.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :operation/state
 (fn [db _]
   (get-in db [:operation :state])))

(reg-sub
 :operation/title
 (fn [db _]
   (get-in db [:operation :title])))

(reg-sub
 :operation/progress
 (fn [db _]
   (get-in db [:operation :progress])))

(reg-sub
 :operation/sub-title
 (fn [db _]
   (get-in db [:operation :sub-title])))

(reg-sub
 :operation/success-text
 (fn [db _]
   (get-in db [:operation :success :text])))

(reg-sub
 :operation/success-sub-text
 (fn [db _]
   (get-in db [:operation :success :sub-text])))

(reg-sub
 :operation/success-btn-text
 (fn [db _]
   (get-in db [:operation :success :btn-text])))

(reg-sub
 :operation/success-route
 (fn [db _]
   (get-in db [:operation :success :route])))

(reg-sub
 :operation/error-text
 (fn [db _]
   (get-in db [:operation :error :text])))

(reg-sub
 :operation/error-sub-text
 (fn [db _]
   (get-in db [:operation :error :sub-text])))

(reg-sub
 :operation/error-btn-text
 (fn [db _]
   (get-in db [:operation :error :btn-text])))

(reg-sub
 :operation/error-route
 (fn [db _]
   (get-in db [:operation :error :route])))
