(ns o2sn.story.subs
  (:require [re-frame.core :refer [reg-sub]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]))

(reg-sub
 :story/current
 (fn [db _]
   (get-in db [:story :current])))

(reg-sub
 :story/img
 (fn [db _]
   (let [story (get-in db [:story :current])]
     (->> (get-in db [:story :images :current])
          (nth (:images story))
          :body
          (str "data:image/*;base64,")))))

(reg-sub
 :story/marked-truth?
 (fn [db _]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (get-in db [:story :current :truth]))))

(reg-sub
 :story/marked-lie?
 (fn [db _]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (get-in db [:story :current :lie]))))

(reg-sub
 :story/map-visible?
 (fn [db _]
   (get-in db [:story :map-visible?])))

(reg-sub
 :story/loading?
 (fn [db _]
   (get-in db [:story :loading?])))
