(ns o2sn.subs.home
  (:require [re-frame.core :refer [reg-sub]]
            [o2sn.helpers.stories :refer [get-story
                                          get-current-story]]))

(reg-sub
 :user-channels
 (fn [db _]
   (map #(hash-map :key (:_key %)
                   :value (:_key %)
                   :text (:name %))
        (get-in db [:user-channels :all] db))))

(reg-sub
 :selected-user-channel
 (fn [db _]
   (get-in db [:user-channels :selected])))

(reg-sub
 :stories
 (fn [db _]
   (sort-by :_key (:stories db))))

(reg-sub
 :category-color
 (fn [db [_ cat]]
   (get-in db [:categories cat])))

(reg-sub
 :current-story
 (fn [db _]
   (get-current-story db)))

(reg-sub
 :story-modal-visible?
 (fn [db _]
   (get-in db [:story-modal :visible])))

(reg-sub
 :story-modal-img
 (fn [db _]
   (let [story (get-current-story db)]
     (nth (:images story)
          (get-in db [:story-modal :images :current])))))

(reg-sub
 :story-like-modal-visible
 (fn [db _]
   (get-in db [:story-like-modal :visible])))

(reg-sub
 :story-like-modal-users
 (fn [db _]
   (get-in db [:story-like-modal :users])))

(reg-sub
 :like-story?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:likes (get-story db story-k)))))

(reg-sub
 :dislike-story?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:dislikes (get-story db story-k)))))

(reg-sub
 :marked-story-truth?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:truth (get-story db story-k)))))

(reg-sub
 :marked-story-lie?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:lie (get-story db story-k)))))

(reg-sub
 :story-map-visible?
 (fn [db _]
   (get-in db [:story-modal :map-visible?])))
