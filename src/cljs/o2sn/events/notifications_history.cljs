(ns o2sn.events.notifications-history
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


(reg-event-fx
 :notifs-history/get-notifs
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/activities/all"
                 :request (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:notifs-history/notifs-loaded]
                 :on-failure [:notifs-history/notifs-not-loaded]}}))

(reg-event-db
 :notifs-history/notifs-loaded
 (fn [db [_ notifs]]
   (assoc-in db [:notifications :all] notifs)))

(reg-event-db
 :notifs-history/notifs-not-loaded
 (fn [db [_ resp]]
   (js/console.log resp)
   db))
