(ns o2sn.home.events
  (:require [kee-frame.core :refer [reg-event-fx reg-event-db reg-chain]]
            [ajax.core :as ajax]
            [o2sn.common.interceptors :refer [server auth]]))

(reg-event-fx
 :home/set-channel
 (fn [{db :db} [k]]
   {:db (assoc db :selected-channel k)
    :dispatch [:home/load-stories k]}))

(reg-event-fx
 :home/load-stories
 [server auth]
 (fn [{db :db} [k]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/channel/" k)
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:home/load-stories-success]
                 :on-failure [:home/load-stories-fail]}
    :db (assoc-in db [:stories :loading?] true)}))

(reg-event-db
 :home/load-stories-success
 (fn [db [resp]]
   (-> db
       (assoc-in [:stories :all] resp)
       (assoc-in [:stories :loading?] false))))

(reg-event-db
 :home/load-stories-fail
 (fn [db _]
   (-> db
       (assoc-in [:stories :all] [])
       (assoc-in [:stories :loading?] false))))

(reg-event-db
 :home/show-likes-modal
 (fn [db [users]]
   (-> db
       (assoc-in [:story-like-modal :users] users)
       (assoc-in [:story-like-modal :visible] true))))

(reg-event-db
 :home/hide-likes-modal
 (fn [db _]
   (assoc-in db [:story-like-modal :visible] false)))

(reg-event-fx
 :home/toggle-like
 [server auth]
 (fn [{db :db} [story-k]]
   {:http-xhrio
    {:method :get
     :uri (str "/stories/story/" story-k "/toggle/like")
     :on-success [:home/toggle-like-dislike-success story-k]
     :format (ajax/text-request-format)
     :response-format (ajax/json-response-format {:keywords? true})}}))

(reg-event-fx
 :home/toggle-dislike
 [server auth]
 (fn [{db :db} [story-k]]
   {:http-xhrio
    {:method :get
     :uri (str "/stories/story/" story-k "/toggle/dislike")
     :on-success [:home/toggle-like-dislike-success story-k]
     :format (ajax/text-request-format)
     :response-format (ajax/json-response-format {:keywords? true})}}))

(reg-event-db
 :home/toggle-like-dislike-success
 (fn [db [story-k resp]]
   (let [user (get-in db [:user :current])
         stories (get-in db [:stories :all])
         story (some #(and (= (:_key %) story-k) %) stories)
         stories-without-target (remove #(= (:_key %) story-k) stories)
         remove-fn (fn [coll]
                     (remove #(= (:_key %) (:_key user)) coll))

         s1 (when (:liked resp)
              (-> story
                  (update :likes conj user)
                  (update :dislikes #(remove-fn %))))
         s2 (when (:disliked resp)
              (-> story
                  (update :dislikes conj user)
                  (update :likes #(remove-fn %))))
         s3 (when-not (or s1 s2)
              (-> story
                  (update :likes #(remove-fn %))
                  (update :dislikes #(remove-fn %))))]

     (assoc-in db [:stories :all]
               (conj stories-without-target
                     (or s1 s2 s3 story))))))

