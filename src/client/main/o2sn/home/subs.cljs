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
   (let [stories (get-in db [:stories :all])
         sorting-k (case (get-in db [:home :sort-by])
                      :title :title
                      :truth :truth_count
                      :lie :lie_count
                      :_key)
         key-fn #(hash-map :_key (:_key %)
                           :sorting-k (get % sorting-k))
         normal-comp (fn [s1 s2]
                       (compare (:sorting-k s1) (:sorting-k s2)))
         fallback-comp (fn [s1 s2]
                         (compare (:_key s1) (:_key s2)))
         order (fn [coll] (if (= :asc (get-in db [:home :order]))
                            coll (reverse coll)))]
     (->> stories
          (sort-by key-fn (fn [s1 s2]
                            (if-not (zero? (normal-comp s1 s2))
                              (normal-comp s1 s2)
                              (fallback-comp s1 s2))))
          order))))

(reg-sub
 :home/first-loading?
 (fn [db _]
   (get-in db [:home :first-loading?])))

(reg-sub
 :home/more-loading?
 (fn [db _]
   (get-in db [:home :more-loading?])))

(reg-sub
 :home/can-load?
 (fn [db _]
   (get-in db [:home :can-load?])))

(reg-sub
 :home/channel
 (fn [db _]
   (get-in db [:home :channel])))

(reg-sub
 :home/sort-by
 (fn [db _]
   (get-in db [:home :sort-by])))

(reg-sub
 :home/order
 (fn [db _]
   (get-in db [:home :order])))

(reg-sub
 :home/stories-count
 (fn [db _]
   (count (get-in db [:stories :all]))))

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
 :home/likes
 (fn [query-vec dynamic-vec]
   [(subscribe [:home/story (second query-vec)])])
 (fn [[story] _]
   (:likes story)))

(reg-sub
 :home/dislikes
 (fn [query-vec dynamic-vec]
   [(subscribe [:home/story (second query-vec)])])
 (fn [[story] _]
   (:dislikes story)))

(reg-sub
 :home/likes-modal-visible?
 (fn [db _]
   (get-in db [:story-like-modal :visible])))

(reg-sub
 :home/likes-modal-users
 (fn [db _]
   (get-in db [:story-like-modal :users])))
