(ns o2sn.home.events
  (:require [kee-frame.core :refer [reg-event-fx reg-event-db reg-chain]]
            [re-frame.core :refer [path]]
            [ajax.core :as ajax]
            [o2sn.common.interceptors :refer [server auth]]))

(reg-event-db
 :home/set-channel
 (fn [db [k]]
   (-> db
       (assoc-in [:home :channel] k)
       (assoc-in [:stories :all] []))))

(reg-event-db
 :home/set-sort-by
 (fn [db [v]]
   (-> db
       (assoc-in [:home :sort-by] v)
       (assoc-in [:stories :all] []))))

(reg-event-db
 :home/set-order
 (fn [db [v]]
   (-> db
       (assoc-in [:home :order]  v)
       (assoc-in [:stories :all] []))))

(reg-event-fx
 :home/first-load-stories
 (fn [{db :db}]
   {:db (-> db
            (assoc-in [:home :offset] 0)
            (assoc-in [:home :can-load?] true)
            (assoc-in [:stories :all] []))
    :dispatch [:home/load-stories true]}))

(reg-event-fx
 :home/load-stories
 [server auth]
 (fn [{db :db} [first?]]
   (let [chan (get-in db [:home :channel])
         sort-by (name (get-in db [:home :sort-by]))
         order (name (get-in db [:home :order]))
         offset (get-in db [:home :offset])
         count (get-in db [:home :count])
         can-load? (get-in db [:home :can-load?])]
     (when can-load?
       {:http-xhrio {:method :get
                     :uri (str "/stories/channel/" chan "/" sort-by "/" order "/" offset "/" count)
                     :format (ajax/text-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success [:home/load-stories-success]
                     :on-failure [:home/load-stories-fail]}
        :db (if first?
              (assoc-in db [:home :first-loading?] true)
              (assoc-in db [:home :more-loading?] true))}))))

(reg-event-db
 :home/load-stories-success
 (fn [db [resp]]
   (if (seq resp)
     (let [db1 (-> db
                (update-in [:stories :all] concat resp)
                (assoc-in [:home :first-loading?] false)
                (assoc-in [:home :more-loading?] false)
                (update-in [:home :offset] #(+ % (get-in db [:home :count]))))]
       (if (= (count resp) (get-in db [:home :count]))
         db1
         (-> db1 (assoc-in [:home :can-load?] false))))
     (-> (assoc-in db [:home :can-load?] false)
         (assoc-in [:stories :loading?] false)))))

(reg-event-db
 :home/load-stories-fail
 (fn [db _]
   (-> db
       (assoc-in [:home :can-load?] false)
       (assoc-in [:home :first-loading?] false)
       (assoc-in [:home :more-loading?] false)
       (assoc-in [:stories :all] []))))


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

