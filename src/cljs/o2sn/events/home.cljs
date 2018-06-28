(ns o2sn.events.home
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [o2sn.helpers.stories :refer [get-story
                                          get-current-story]]))

;; helper functions

(reg-event-fx
 :set-selected-user-chan
 (fn [{db :db} [_ k]]
   {:db (assoc db :selected-channel k)
    :dispatch [:load-stories k]}))

(reg-event-fx
 :load-stories
 (fn [{db :db} [_ k]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/channel/" k)
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-stories-success]
                 :on-failure [:load-stories-fail]}}))

(reg-event-db
 :load-stories-success
 (fn [db [_ resp]]
   (assoc db :stories resp)))

(reg-event-db
 :load-stories-fail
 (fn [db _]
   (assoc db :stories [])))

(reg-event-db
 :show-story-modal
 (fn [db [_ k]]
   (-> db
       (assoc-in [:story-modal :story] k)
       (assoc-in [:story-modal :visible] true)
       (assoc-in [:story-modal :images :current] 0))))

(reg-event-db
 :hide-story-modal
 (fn [db _]
   (assoc-in db [:story-modal :visible] false)))

(reg-event-db
 :next-story-modal-img
 (fn [db _]
   (let [cnt (-> (get-current-story db) :images count)
         curnt (get-in db [:story-modal :images :current])
         nxt (if (= curnt (dec cnt))
               0
               (inc curnt))]
     (assoc-in db [:story-modal :images :current] nxt))))

(reg-event-db
 :previous-story-modal-img
 (fn [db _]
   (let [cnt (-> (get-current-story db) :images count)
         curnt (get-in db [:story-modal :images :current])
         prv (if (= curnt 0)
               (dec cnt)
               (dec curnt))]
     (assoc-in db [:story-modal :images :current] prv))))

(reg-event-db
 :show-story-users-like-modal
 (fn [db [_ users]]
   (-> db
       (assoc-in [:story-like-modal :users] users)
       (assoc-in [:story-like-modal :visible] true))))

(reg-event-db
 :hide-story-users-like-modal
 (fn [db _]
   (assoc-in db [:story-like-modal :visible] false)))

(reg-event-fx
 :toggle-like-story
 (fn [{db :db} [_ story-k]]
   (let [liked? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                      (:likes (get-story db story-k)))
         like-uri (str "/stories/story/" story-k "/like")
         unlike-uri (str "/stories/story/" story-k "/unlike")]
     {:http-xhrio {:method :get
                   :uri (if liked? unlike-uri like-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-like-story-success story-k (not liked?)]
                   :on-failure [:toggle-like-story-fail]}
      :dispatch [:toggle-dislike-story-success story-k false]})))

(reg-event-db
 :toggle-like-story-success
 (fn [db [_ story-k now-liked?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-liked?
                          (update target :likes
                                  #(conj % (get-in db [:user :current])))
                          (update target :likes
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-like-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'like' of a story" resp)
   db))

(reg-event-fx
 :toggle-dislike-story
 (fn [{db :db} [_ story-k]]
   (let [disliked? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                      (:dislikes (get-story db story-k)))
         dislike-uri (str "/stories/story/" story-k "/dislike")
         undislike-uri (str "/stories/story/" story-k "/undislike")]
     {:http-xhrio {:method :get
                   :uri (if disliked? undislike-uri dislike-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-dislike-story-success story-k (not disliked?)]
                   :on-failure [:toggle-dislike-story-fail]}
      :dispatch [:toggle-like-story-success story-k false]})))

(reg-event-db
 :toggle-dislike-story-success
 (fn [db [_ story-k now-disliked?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-disliked?
                          (update target :dislikes
                                  #(conj % (get-in db [:user :current])))
                          (update target :dislikes
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-dislike-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'dislike' of a story" resp)
   db))

(reg-event-fx
 :toggle-truth-story
 (fn [{db :db} [_ story-k]]
   (let [truth? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                         (:truth (get-story db story-k)))
         truth-uri (str "/stories/story/" story-k "/mark/truth")
         not-truth-uri (str "/stories/story/" story-k "/unmark/truth")]
     {:http-xhrio {:method :get
                   :uri (if truth? not-truth-uri truth-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-truth-story-success story-k (not truth?)]
                   :on-failure [:toggle-truth-story-fail]}
      :dispatch [:toggle-lie-story-success story-k false]})))

(reg-event-db
 :toggle-truth-story-success
 (fn [db [_ story-k now-truth?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-truth?
                          (update target :truth
                                  #(conj % (get-in db [:user :current])))
                          (update target :truth
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-truth-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'truth' of a story" resp)
   db))

(reg-event-fx
 :toggle-lie-story
 (fn [{db :db} [_ story-k]]
   (let [lie? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                      (:lie (get-story db story-k)))
         lie-uri (str "/stories/story/" story-k "/mark/lie")
         not-lie-uri (str "/stories/story/" story-k "/unmark/lie")]
     {:http-xhrio {:method :get
                   :uri (if lie? not-lie-uri lie-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-lie-story-success story-k (not lie?)]
                   :on-failure [:toggle-lie-story-fail]}
      :dispatch [:toggle-truth-story-success story-k false]})))

(reg-event-db
 :toggle-lie-story-success
 (fn [db [_ story-k now-lie?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-lie?
                          (update target :lie
                                  #(conj % (get-in db [:user :current])))
                          (update target :lie
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-lie-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'lie' of a story" resp)
   db))

(reg-event-db
 :toggle-map-visiblity
 (fn [db _]
   (update-in db [:story-modal :map-visible?] not)))
