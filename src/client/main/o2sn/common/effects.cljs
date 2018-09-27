(ns o2sn.common.effects
  (:require [reagent.core :as r]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   dispatch
                                   debug]]
            [o2sn.validation.validation :as v]))

(defonce timeouts (r/atom {}))

(reg-fx
 :timeout
 (fn [{:keys [id event time]}]
   (when-some [to (get @timeouts id)]
     (js/clearTimeout to)
     (swap! timeouts dissoc id))
   (when (some? event)
     (swap! timeouts assoc id
            (js/setTimeout
             (fn []
               (dispatch event))
             time)))))

(reg-fx
 :timeout-n
 (fn [{:keys [id events time]}]
   (when-some [to (get @timeouts id)]
     (js/clearTimeout to)
     (swap! timeouts dissoc id))
   (when (seq events)
     (swap! timeouts assoc id
            (js/setTimeout
             (fn []
               (doseq [event events]
                 (dispatch event)))
             time)))))

(reg-fx
 :validate
 (fn [validation-map]
   (v/validate validation-map)))
