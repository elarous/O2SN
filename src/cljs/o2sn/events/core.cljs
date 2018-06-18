(ns o2sn.events.core
  (:require [o2sn.db :as db]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :set-active-page
 (fn [{db :db} [_ page]]
   {:db (assoc-in db [:page :hiding?] true)
    :dispatch-later [{:ms (get-in db [:page :duration])
                      :dispatch [:set-page page]}]}))

(reg-event-db
 :set-page
 (fn [db [_ page]]
   (-> db
       (assoc-in [:page :active] page)
       (assoc-in [:page :hiding?] false))))

;;;;

(reg-event-db
 :set-docs
 (fn [db [_ docs]]
   (assoc db :docs docs)))

(reg-event-fx
 :check-authenticated
 (fn [{db :db} [_]]
   {:http-xhrio {:method :get
                 :uri "/user/authenticated"
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-success]
                 :on-failure [:login-fail false]}}))
