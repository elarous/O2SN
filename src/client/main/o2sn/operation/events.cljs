(ns o2sn.operation.events
  (:require [kee-frame.core :refer [reg-event-db reg-event-fx]]
            [re-frame.core :refer [path]]))

(reg-event-fx
 :operation/start
 (fn [{db :db} [{:keys [title sub-title success error]}]]
   {:db  (-> db
             (assoc-in [:operation :title] title)
             (assoc-in [:operation :sub-title] sub-title)
             (assoc-in [:operation :success] success)
             (assoc-in [:operation :error] error))
    :dispatch [:operation/reset]
    :navigate-to [:operation]}))

(reg-event-db
 :operation/reset
 [(path :operation)]
 (fn [op _]
   (-> op
       (assoc :state :progress)
       (assoc :progress 0))))

(reg-event-db
 :operation/set-state
 [(path :operation :state)]
 (fn [_ [state]]
   state))

(reg-event-db
 :operation/set-progress
 [(path :operation :progress)]
 (fn [_ [progress]]
   progress))

(reg-event-db
 :operation/set-error-sub-text
 [(path :operation :error)]
 (fn [o-error [txt]]
   (-> o-error
       (assoc :sub-text txt))))
