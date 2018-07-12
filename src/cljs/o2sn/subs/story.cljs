(ns o2sn.subs.story
  (:require [re-frame.core :refer [reg-sub]]
            [o2sn.helpers.stories :refer [get-story
                                          get-current-story]]))

(reg-sub
 :story/current
 (fn [db _]
   (get-in db [:story :current])))

(reg-sub
 :story/img
 (fn [db _]
   (let [story (get-in db [:story :current])]
     (nth (:images story)
          (get-in db [:story :images :current])))))

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
