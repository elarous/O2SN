(ns o2sn.events.logout
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.validation :as v]))

(reg-event-fx
 :logout
 (fn [{db :db} [_]]
   {:http-xhrio {:method :post
                 :uri "/user/logout"
                 :format (ajax/text-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:logout-success]
                 :on-failure [:logout-fail]}}))

(reg-event-fx
 :logout-success
 (fn [{db :db} _]
   {:db (-> db
            (assoc-in [:user :logged-in?] false))
    :dispatch [:set-active-page :login]}))

(reg-event-db
 :logout-fail
 (fn [db _]
   (-> db
       (assoc-in [:user :logged-in?] true))))

