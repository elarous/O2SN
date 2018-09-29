(ns o2sn.notifications.events
  (:require [kee-frame.core :refer [reg-event-fx reg-event-db]]
            [re-frame.core :refer [path]]
            [o2sn.common.interceptors :refer [server auth]]
            [ajax.core :as ajax]))

(reg-event-fx
 :notifs/test
 (fn [_ data]
   {:notifs/send data}))

(reg-event-fx
 :notifs/connect-ws
 (fn [{db :db} _]
   {:notifs/create-ws (merge (:server db)
                             {:user-k (get-in db [:user :current :_key])})}))

(reg-event-fx
 :notifs/disconnect-ws
 (fn [_ _]
   {:notifs/close nil}))

(reg-event-fx
 :notifs/receive
 (fn [{db :db} [notif]]
   (let [already-notified? (some #(= (:_key notif) (:_key %))
                                 (get-in db [:notifications :unreads]))]
     (when-not already-notified?
       {:db (update-in db [:notifications :unreads] conj notif)
        :dispatch [:notifs/show-alert notif]}))))


(reg-event-fx
 :notifs/get-notifs
 [server auth]
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/activities/all"
                 :request (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:notifs/notifs-loaded]
                 :on-failure [:notifs/notifs-not-loaded]}}))

(reg-event-db
 :notifs/notifs-loaded
 (fn [db [notifs]]
   (assoc-in db [:notifications :all] notifs)))

(reg-event-db
 :notifs/notifs-not-loaded
 (fn [db [resp]]
   (js/console.log resp)
   db))

(reg-event-db
 :notifs/clear-notifs
 (fn [db _]
   (assoc-in db [:notifications :unreads] [])))

(reg-event-db
 :notifs/remove
 (fn [db [notif-k]]
   (update-in db [:notifications :unreads]
              (fn [notifs] (remove #(= (:_key %) notif-k) notifs)))))

(reg-event-fx
 :notifs/get-unreads
 [server auth]
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/activities/unreads"
                 :request (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:notifs/unreads-loaded]
                 :on-failure [:notifs/unreads-not-loaded]}}))

(reg-event-db
 :notifs/unreads-loaded
 (fn [db [resp]]
   (assoc-in db [:notifications :unreads] resp)))

(reg-event-db
 :notifs/unreads-not-loaded
 (fn [db [resp]]
   (js/alert resp)
   db))

(reg-event-fx
 :notifs/mark-read
 [server auth]
 (fn [{db :db} [activity-k]]
   {:http-xhrio {:method :get
                 :uri (str "/activities/mark/read/" activity-k)
                 :request (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:notifs/notif-marked activity-k]
                 :on-failure [:notifs/notifs-not-marked]}}))

(reg-event-fx
 :notifs/notif-marked
 (fn [{db :db} [notif-k _]]
   {:dispatch [:notifs/remove notif-k]}))

(reg-event-db
 :notifs/notifs-not-marked
 (fn [db [resp]]
   (js/alert resp)
   db))

(reg-event-fx
 :notifs/notif-click
 (fn [{db :db} [notif]]
   {:dispatch [:notifs/mark-read (:_key notif)]}))


(reg-event-fx
 :notifs/mark-read-all
 [server auth]
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/activities/mark/read-all"
                 :request (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:notifs/notifs-all-marked]
                 :on-failure [:notifs/notifs-all-not-marked]}}))

(reg-event-db
 :notifs/notifs-all-marked
 (fn [db _]
   (assoc-in db [:notifications :unreads] [])))

(reg-event-db
 :notifs/notifs-all-not-marked
 (fn [db [resp]] (js/console.log resp) db))

(reg-event-db
 :notifs/remove-alert
 [(path :notifications :alerts)]
 (fn [alerts [alert-k]]
   (remove #(= (:_key %) alert-k) alerts)))

(reg-event-fx
 :notifs/pause-alert
 (fn [{db :db} [alert-k]]
   {:timeout {:id (keyword (str "alert-" alert-k))
              :event [:notifs/remove-alert alert-k]
              :time 5000}}))

(reg-event-fx
 :notifs/show-alert
 (fn [{db :db} [notif]]
   (when-not (some #(= (:_key %) (:_key notif))
                   (get-in db [:notifications :alerts]))
     (let [[header action] (case (keyword (:type notif))
                             :like ["New Like" "Likes"]
                             :dislike ["New Dislike" "Dislikes"]
                             :lie ["New Lie" "Marked As Lie"]
                             :truth ["New Truth" "Marked As Truth"]
                             ["something" "something"])
           alert (-> notif
                     (assoc :header header)
                     (assoc :action action)
                     (dissoc :type))]
       {:db (update-in db [:notifications :alerts] conj alert)
        :timeout {:id (keyword (str "alert-" (:_key notif)))
                  :event [:notifs/remove-alert (:_key notif)]
                  :time 5000}}))))
