(ns o2sn.events.notifications
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.ui :as ui]
            [o2sn.views.notifications :as view]
            [o2sn.websockets :as ws]
            [re-frame.core :as rf]))

(reg-fx
 :toastr
 (fn [[type msg]]
   (case type
     :info (.info ui/toastr msg)
     :error (.error ui/toastr msg)
     :success (.success ui/toastr msg)
     :warning (.warning ui/toastr msg)
     (.info ui/toastr msg))))

(reg-fx
 :notifs/create-ws
 (fn [_]
   (ws/make-websocket! (str "ws://" (.-host js/location) "/ws/notifs")
                       :notifs
                       #(rf/dispatch [:notifs/receive %]))))

(reg-fx
 :notifs/send
 (fn [data]
   (ws/send! :notifs data)))

(reg-event-fx
 :notifs/receive
 (fn [{db :db} [_ data]]
   #_(js/console.log data)
   {:dispatch [:notifs/add-notif data]}))

(reg-event-fx
 :notifs/send-test
 (fn [{db :db} [_ data]]
   {:notifs/send data}))

(reg-event-db
 :notifs/open-notifs
 (fn [db _]
   (assoc-in db [:notifications :opened?] true)))

(reg-event-db
 :notifs/close-notifs
 (fn [db _]
   (assoc-in db [:notifications :opened?] false)))

(reg-event-fx
 :notifs/add-notif
 (fn [{db :db} [_ notif]]
   (let [already-notified? (some #(= (:_key notif) (:_key %))
                                 (get-in db [:notifications :unreads]))]
     (when-not already-notified?
       {:db (update-in db [:notifications :unreads] conj notif)
        :toastr [:info (view/make-msg-toast notif)]}))))

(reg-event-db
 :notifs/clear-notifs
 (fn [db _]
   (assoc-in db [:notifications :unreads] [])))

(reg-event-db
 :notifs/remove
 (fn [db [_ notif-k]]
   (update-in db [:notifications :unreads]
              (fn [notifs] (remove #(= (:_key %) notif-k) notifs)))))

(reg-event-fx
 :notifs/get-notifs
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/activities/unreads"
                 :request (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:notifs/notifs-loaded]
                 :on-failure [:notifs/notifs-not-loaded]}}))

(reg-event-db
 :notifs/notifs-loaded
 (fn [db [_ resp]]
   (assoc-in db [:notifications :unreads] resp)))

(reg-event-db
 :notifs/notifs-not-loaded
 (fn [db [_ resp]]
   (js/alert resp)
   db))

(reg-event-fx
 :notifs/mark-read
 (fn [{db :db} [_ activity-k]]
   {:http-xhrio {:method :get
                 :uri (str "/activities/mark/read/" activity-k)
                 :request (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:notifs/notif-marked activity-k]
                 :on-failure [:notifs/notifs-not-marked]}}))

(reg-event-fx
 :notifs/notif-marked
 (fn [{db :db} [_ notif-k _]]
   {:dispatch [:notifs/remove notif-k]}))

(reg-event-db
 :notifs/notifs-not-marked
 (fn [db [_ resp]]
   (js/alert resp)
   db))

(reg-event-fx
 :notifs/notif-click
 (fn [{db :db} [_ notif]]
   {:dispatch-n [[:notifs/close-notifs]
                 [:notifs/mark-read (:_key notif)]
                 [:notifs/handle-notif-click notif]]}))

(reg-event-fx
 :notifs/handle-notif-click
 (fn [{db :db} [_ notif]]
   (js/console.log (str (:_key notif) " clicked!"))))

(reg-event-fx
 :notifs/mark-read-all
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
 (fn [db [_ resp]] (js/console.log resp) db))

