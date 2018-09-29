(ns o2sn.home.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :home/channels
 (fn [db _]
   (map #(hash-map :key (:_key %)
                   :value (:_key %)
                   :text (:name %)
                   :type (:type %))
        (get-in db [:channels :all]))))

(reg-sub
 :home/stories
 (fn [db _]
   (sort-by :_key (get-in db [:stories :all]))))

(reg-sub
 :home/stories-loading?
 (fn [db _]
   (get-in db [:stories :loading?])))

(reg-sub
 :home/selected-channel
 (fn [db _]
   (:selected-channel db)))

(reg-sub
 :home/story
 (fn [db [_ k]]
   (some #(and (= k (:_key %)) %)
         (get-in db [:stories :all]))))

(reg-sub
 :home/like?
 (fn [query-vec dynamic-vec]
   [(subscribe [:common/user])
    (subscribe [:home/story (second query-vec)])])
 (fn [[user story] _]
   (some #(= (:_key %) (:_key user)) (:likes story))))

(reg-sub
 :home/dislike?
 (fn [query-vec dynamic-vec]
   [(subscribe [:common/user])
    (subscribe [:home/story (second query-vec)])])
 (fn [[user story] _]
   (some #(= (:_key %) (:_key user)) (:dislikes story))))

(reg-sub
 :home/likes-modal-visible?
 (fn [db _]
   (get-in db [:story-like-modal :visible])))

(reg-sub
 :home/likes-modal-users
 (fn [db _]
   (get-in db [:story-like-modal :users])))
