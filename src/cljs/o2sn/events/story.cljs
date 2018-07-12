(ns o2sn.events.story
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [o2sn.helpers.stories :refer [get-story
                                          get-current-story
                                          story-in-stories?
                                          update-in-stories]]))

(reg-event-fx
 :story/set-current
 (fn [{db :db} [_ story-k load?]]
   (if-not load?
     {:db (-> db
              (assoc-in [:story :current] (get-story db story-k))
              (assoc-in [:story :images :current] 0))
      :dispatch [:set-active-panel :story]}
     {:db (assoc-in db [:story :images :current] 0)
      :dispatch [:story/load-by-key story-k]})))

(reg-event-fx
 :story/load-by-key
 (fn [{db :db} [_ story-k]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/story/" story-k "/get")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:story/load-success]
                 :on-failure [:story/load-fail]}}))

(reg-event-fx
 :story/load-success
 (fn [{db :db} [_ resp]]
   {:db (assoc-in db [:story :current] resp)
    :dispatch [:set-active-panel :story]}))

(reg-event-db
 :story/load-fail
 (fn [db [_ resp]]
   (js/console.log resp)
   db))

(reg-event-db
 :story/next-img
 (fn [db _]
   (let [cnt (-> (get-in db [:story :current :images]) count)
         curnt (get-in db [:story :images :current])
         nxt (if (= curnt (dec cnt))
               0
               (inc curnt))]
     (assoc-in db [:story :images :current] nxt))))

(reg-event-db
 :story/previous-img
 (fn [db _]
   (let [cnt (-> (get-in db [:story :current :images]) count)
         curnt (get-in db [:story :images :current])
         prv (if (= curnt 0)
               (dec cnt)
               (dec curnt))]
     (assoc-in db [:story :images :current] prv))))

(reg-event-fx
 :story/toggle-truth
 (fn [{db :db} _]
   (let [story (get-in db [:story :current])
         truth? (some #(= (:_key %) (get-in db [:user :current :_key]))
                         (:truth story))
         truth-uri (str "/stories/story/" (:_key story) "/mark/truth")
         not-truth-uri (str "/stories/story/" (:_key story) "/unmark/truth")]
     {:http-xhrio {:method :get
                   :uri (if truth? not-truth-uri truth-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:story/toggle-truth-success (not truth?)]
                   :on-failure [:story/toggle-truth-fail]}
      :dispatch [:story/toggle-lie-success false]})))

(reg-event-fx
 :story/toggle-truth-success
 (fn [{db :db} [_ now-truth?]]
   (let [current-user (get-in db [:user :current])
         story (get-in db [:story :current])
         updated-story (if now-truth?
                          (update story :truth
                                  #(conj % current-user))
                          (update story :truth
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key current-user))
                                            o))))]
     (merge
      {:db (if (story-in-stories? db (:_key story))
             (-> db
                 (assoc-in [:story :current] updated-story)
                 (update-in-stories updated-story))
             (assoc-in db [:story :current] updated-story))}
      (when now-truth?
        {:notifs/send {:type :truth
                       :target (str "stories/" (:_key story))}})))))

(reg-event-db
 :story/toggle-truth-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'truth' of a story" resp)
   db))

(reg-event-fx
 :story/toggle-lie
 (fn [{db :db} _]
   (let [story (get-in db [:story :current])
         lie? (some #(= (:_key %) (get-in db [:user :current :_key]))
                      (:lie story))
         lie-uri (str "/stories/story/" (:_key story) "/mark/lie")
         not-lie-uri (str "/stories/story/" (:_key story) "/unmark/lie")]
     {:http-xhrio {:method :get
                   :uri (if lie? not-lie-uri lie-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:story/toggle-lie-success (not lie?)]
                   :on-failure [:story/toggle-lie-fail]}
      :dispatch [:story/toggle-truth-success false]})))

(reg-event-fx
 :story/toggle-lie-success
 (fn [{db :db} [_ now-lie?]]
   (let [current-user (get-in db [:user :current])
         story (get-in db [:story :current])
         updated-story (if now-lie?
                         (update story :lie
                                 #(conj % current-user))
                         (update story :lie
                                 (fn [o]
                                   (remove #(= (:_key %)
                                               (:_key current-user))
                                           o))))]
     (merge
      {:db (if (story-in-stories? db (:_key story))
             (-> db
                 (assoc-in [:story :current] updated-story)
                 (update-in-stories updated-story))
             (assoc-in db [:story :current] updated-story))}
      (when now-lie?
        {:notifs/send {:type :lie
                       :target (str "stories/" (:_key story))}})))))

(reg-event-db
 :story/toggle-lie-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'lie' of a story" resp)
   db))

(reg-event-db
 :story/toggle-map
 (fn [db _]
   (update-in db [:story :map-visible?] not)))
