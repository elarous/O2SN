(ns o2sn.story.events
  (:require [kee-frame.core :refer [reg-event-fx reg-event-db]]
            [re-frame.core :refer [path]]
            [ajax.core :as ajax]
            [o2sn.common.interceptors :refer [server auth]]))

(reg-event-fx
 :story/set-current
 (fn [{db :db} [story-k load?]]
   (let [story (some #(and (= (:_key %) story-k) %)
                     (:stories db))]
     (if-not load?
       {:db (-> db
                (assoc-in [:story :current] story)
                (assoc-in [:story :images :current] 0))
        :navigate-to [:view-story :story story-k]}
       {:db (assoc-in db [:story :images :current] 0)
        :dispatch [:story/load-by-key story-k]}))))

(reg-event-fx
 :story/load-by-key
 [server auth]
 (fn [{db :db} [story-k]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/story/" story-k "/get")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:story/load-success]
                 :on-failure [:story/load-fail]}
    :db (assoc-in db [:story :loading?] true)}))

(reg-event-db
 :story/load-success
 (fn [db [resp]]
   (-> db
       (assoc-in [:story :current] resp)
       (assoc-in [:story :loading?] false))))

(reg-event-db
 :story/load-fail
 (fn [db [resp]]
   (assoc-in db [:story :loading?] false)))

(reg-event-fx
 :story/load-img
 [server auth]
 (fn [db [img-k index]]
   {:http-xhrio {:method :get
                 :uri (str "/images/get/" img-k)
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:story/img-loaded index]}}))

(reg-event-db
 :story/img-loaded
 [(path :story :current :images)]
 (fn [images [index resp]]
   (->> images
        (map-indexed
         (fn [i img] (if (= i index) resp img)))
        vec)))

(reg-event-fx
 :story/next-img
 (fn [{db :db} _]
   (let [cnt (-> (get-in db [:story :current :images]) count)
         curnt (get-in db [:story :images :current])
         nxt (if (= curnt (dec cnt))
               0
               (inc curnt))
         target (-> (get-in db [:story :current :images])
                    (get nxt))
         loaded? (contains? target :body)
         ret-map {:db (assoc-in db [:story :images :current] nxt)}]
     (if-not loaded?
       (assoc ret-map :dispatch [:story/load-img target nxt])
       ret-map))))

(reg-event-fx
 :story/previous-img
 (fn [{db :db} _]
   (let [cnt (-> (get-in db [:story :current :images]) count)
         curnt (get-in db [:story :images :current])
         prv (if (= curnt 0)
               (dec cnt)
               (dec curnt))
         target (-> (get-in db [:story :current :images])
                    (get prv))
         loaded? (contains? target :body)
         ret-map {:db (assoc-in db [:story :images :current] prv)}]
     (if-not loaded?
       (assoc ret-map :dispatch [:story/load-img target prv])
       ret-map))))

(reg-event-fx
 :story/toggle-map
 (fn [{db :db} _]
   (let [map-visible? (get-in db [:story :map-visible?])
         images (get-in db [:story :current :images])
         db-map {:db (update-in db [:story :map-visible?] not)}
         dispatch-map {:dispatch [:story/load-img (first images) 0]}]
     (if (and map-visible?
              (seq images)
              (not (contains? (first images) :body)))
       (merge db-map dispatch-map)
       db-map))))

(reg-event-fx
 :story/toggle-truth
 [server auth]
 (fn [{db :db} [story-k]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/story/" story-k "/toggle/truth")
                 :on-success [:story/toggle-truth-lie-success story-k]
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})}}))

(reg-event-fx
 :story/toggle-lie
 [server auth]
 (fn [{db :db} [story-k]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/story/" story-k "/toggle/lie")
                 :on-success [:story/toggle-truth-lie-success story-k]
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})}}))

(reg-event-db
 :story/toggle-truth-lie-success
 (fn [db [story-k resp]]
   (let [user (get-in db [:user :current])
         stories (get-in db [:stories :all])
         story (some #(and (= (:_key %) story-k) %) stories)
         stories-without-target (remove #(= (:_key %) story-k) stories)
         remove-fn (fn [coll]
                     (remove #(= (:_key %) (:_key user)) coll))

         s1 (when (:truth resp)
              (-> story
                  (update :truth conj user)
                  (update :lie #(remove-fn %))))
         s2 (when (:lie resp)
              (-> story
                  (update :lie conj user)
                  (update :truth #(remove-fn %))))
         s3 (when-not (or s1 s2)
              (-> story
                  (update :truth #(remove-fn %))
                  (update :lie #(remove-fn %))))
         result (merge (or s1 s2 s3 story)
                       {:images (get-in db [:story :current :images])})]

     (-> db
         (assoc-in [:stories :all] (conj stories-without-target result))
         (assoc-in [:story :current] result)))))
