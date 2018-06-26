(ns o2sn.subs.new-story
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :new-story/title
 (fn [db _]
   (get-in db [:new-story :title])))

(reg-sub
 :new-story/marker-lat
 (fn [db _]
   (get-in db [:new-story :map :lat])))

(reg-sub
 :new-story/marker-lng
 (fn [db _]
   (get-in db [:new-story :map :lng])))

(reg-sub
 :new-story/desc
 (fn [db _]
   (get-in db [:new-story :description])))

(reg-sub
 :new-story/images
 (fn [db _]
   (->> (get-in db [:new-story :images])
        (map #(hash-map :img (:img %)
                        :fname (.-name (:file %)))))))

(reg-sub
 :new-story/categories
 (fn [db _]
   (:categories db)))

(reg-sub
 :new-story/date
 (fn [db _]
   (get-in db [:new-story :datetime :date])))

(reg-sub
 :new-story/time
 (fn [db _]
   (get-in db [:new-story :datetime :time])))

(reg-sub
 :new-story/not-valid?
 (fn [db _]
   (seq (get-in db [:new-story :errors]))))

(reg-sub
 :new-story/errors
 (fn [db _]
   (map #(str (name (first (keys %))) " : " (first (vals %)))
        (get-in db [:new-story :errors]))))

(reg-sub
 :new-story/progress
 (fn [db _]
   (get-in db [:new-story :saving :progress])))

(reg-sub
 :new-story/saving-state
 (fn [db _]
   (get-in db [:new-story :saving :state])))

(reg-sub
 :new-story/saving-msg
 (fn [db _]
   (get-in db [:new-story :saving :message])))

(reg-sub
 :new-story/phase
 (fn [db _]
   (get-in db [:new-story :phase])))
