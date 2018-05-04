(ns o2sn.events
  (:require [o2sn.db :as db]
            [re-frame.core :refer [dispatch reg-event-fx reg-event-db reg-sub]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

;;dispatchers

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-db
 :set-active-page
 (fn [db [_ page]]
   (assoc db :page page)))

(reg-event-db
 :set-docs
 (fn [db [_ docs]]
   (assoc db :docs docs)))

(reg-event-fx
 :check-authenticated
 (fn [{db :db} [_]]
   {:http-xhrio {:method :get
                 :uri "/authenticated"
                 :format (ajax/text-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:login-success]
                 :on-failure [:login-fail false]}}))


(reg-event-fx
 :login
 (fn [{db :db} [_ username password]]
   {:http-xhrio {:method :post
                 :uri "/api/login"
                 :params {:username username :password password}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:login-success]
                 :on-failure [:login-fail true]}
    :db (assoc-in db [:login-form :processing?] true)}))

(reg-event-db
 :login-success
 (fn [db [_ resp]]
   (-> (assoc-in db [:user :logged-in?] true)
       (assoc-in [:login-form :errors?] false)
       (assoc-in [:login-form :processing?] false))))

(reg-event-db
  :login-fail
  (fn [db [_ errors? resp]]
    (-> (assoc-in db [:user :logged-in?] false)
        (assoc-in [:login-form :errors?] errors?)
        (assoc-in [:login-form :processing?] false))))

(reg-event-fx
 :logout
 (fn [{db :db} [_]]
   {:http-xhrio {:method :post
                 :uri "/api/logout"
                 :format (ajax/text-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:logged-out true]
                 :on-failure [:logged-out false]}}))

(reg-event-db
 :logged-out
 (fn [db [_ logged-out? resp]]
   (assoc-in db [:user :logged-in?] (not logged-out?))))

(reg-event-fx
 :get-admin-content
 (fn [{db :db} [_]]
   {:http-xhrio {:method :get
                 :uri "/api/admin"
                 :format (ajax/text-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:set-admin-content]
                 :on-failure [:get-admin-content-failed]}}))

(reg-event-db
 :set-admin-content
 (fn [db [_ resp]]
   (println "RESP : " resp)
   (assoc db :admin-content resp)))

(reg-event-db
 :get-admin-content-failed
 (fn [db [_ resp]]
   (assoc db :admin-content "This page can viewed by admins only.")))

;; login form events
(reg-event-db
 :set-login-form-username
 (fn [db [_ new-val]]
   (assoc-in db [:login-form :username] new-val)))

(reg-event-db
 :set-login-form-password
 (fn [db [_ new-val]]
   (assoc-in db [:login-form :password] new-val)))

;; welcome events
(reg-event-db
 :set-welcome-form
 (fn [db [_ form]]
   (assoc db :welcome-form form)))

;;subscriptions

(reg-sub
 :page
 (fn [db _]
   (:page db)))

(reg-sub
 :docs
 (fn [db _]
   (:docs db)))

(reg-sub
 :admin-content
 (fn [db _]
   (:admin-content db)))

(reg-sub
 :user-logged-in
 (fn [db _]
   (get-in db [:user :logged-in?])))

;; welcome page subscriptions
(reg-sub
 :welcome-form
 (fn [db _]
   (:welcome-form db)))

;; login form subscriptions

(reg-sub
 :login-form-username
 (fn [db _]
   (get-in db [:login-form :username] "")))

(reg-sub
 :login-form-password
 (fn [db _]
   (get-in db [:login-form :password] "")))

(reg-sub
 :login-form-errors?
 (fn [db _]
   (get-in db [:login-form :errors?])))

(reg-sub
 :login-form-processing?
 (fn [db _]
   (get-in db [:login-form :processing?])))
