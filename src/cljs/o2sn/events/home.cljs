(ns o2sn.events.home
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [o2sn.helpers.stories :refer [get-story
                                          get-current-story]]))

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
                 :on-failure [:load-stories-fail]}
    :db (assoc db :loading-stories true)}))

(reg-event-db
 :load-stories-success
 (fn [db [_ resp]]
   (-> db
       (assoc :stories resp)
       (assoc :loading-stories false))))

(reg-event-db
 :load-stories-fail
 (fn [db _]
   (-> db
       (assoc :stories [])
       (assoc :loading-stories false))))

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

(reg-event-fx
 :toggle-like-story-success
 (fn [{db :db} [_ story-k now-liked?]]
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
     (merge
      {:db (assoc db :stories updated-stories)}
      (when now-liked?
        {:notifs/send {:type :like
                       :target (str "stories/" story-k)}})))))

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

(reg-event-fx
 :toggle-dislike-story-success
 (fn [{db :db} [_ story-k now-disliked?]]
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
     (merge
      {:db (assoc db :stories updated-stories)}
      (when now-disliked?
        {:notifs/send {:type :dislike
                       :target (str "stories/" story-k)}})))))

(reg-event-db
 :toggle-dislike-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'dislike' of a story" resp)
   db))
