(ns o2sn.new-story.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :new-story/title
 (fn [db _]
   (get-in db [:new-story :title :value])))

(reg-sub
 :new-story/title-valid?
 (fn [db _]
   (get-in db [:new-story :title :valid?])))

(reg-sub
 :new-story/title-validating?
 (fn [db _]
   (get-in db [:new-story :title :validating?])))

(reg-sub
 :new-story/title-error
 (fn [db _]
   (get-in db [:new-story :title :error])))

(reg-sub
 :new-story/desc
 (fn [db _]
   (get-in db [:new-story :description :value])))

(reg-sub
 :new-story/desc-valid?
 (fn [db _]
   (get-in db [:new-story :description :valid?])))

(reg-sub
 :new-story/desc-validating?
 (fn [db _]
   (get-in db [:new-story :description :validating?])))

(reg-sub
 :new-story/desc-error
 (fn [db _]
   (get-in db [:new-story :description :error])))

(reg-sub
 :new-story/category
 (fn [db _]
   (get-in db [:new-story :category :value])))

(reg-sub
 :new-story/category-valid?
 (fn [db _]
   (get-in db [:new-story :category :valid?])))

(reg-sub
 :new-story/category-validating?
 (fn [db _]
   (get-in db [:new-story :category :validating?])))

(reg-sub
 :new-story/category-error
 (fn [db _]
   (get-in db [:new-story :category :error])))

(reg-sub
 :new-story/marker-lat
 (fn [db _]
   (get-in db [:new-story :map :lat])))

(reg-sub
 :new-story/marker-lng
 (fn [db _]
   (get-in db [:new-story :map :lng])))



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
 :new-story/valid?
 (fn [db _]
   (empty? (get-in db [:new-story :errors]))))

(reg-sub
 :new-story/errors
 (fn [db _]
   (get-in db [:new-story :errors])))
