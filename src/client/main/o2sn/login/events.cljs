(ns o2sn.login.events
  (:require [kee-frame.core :refer [reg-event-fx reg-event-db]]
            [re-frame.core :refer [reg-fx debug dispatch path]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.common.interceptors :refer [server]]))

(reg-event-db
 :login/set-username
 [(path :login-form :username :value)]
 (fn [v [new-val]]
   new-val))

(reg-event-db
 :login/set-password
 [(path :login-form :password :value)]
 (fn [v [new-val]]
   new-val))

(reg-event-fx
 :login/validate-username
 (fn [_ [username]]
   {:timeout {:id :login-username
              :event [:login/validating-username username]
              :time 500}}))

(reg-event-fx
 :login/validating-username
 (fn [{db :db} [username]]
   {:validate {:target :username
               :value username
               :on-success #(dispatch [:login/username-valid])
               :on-failure #(dispatch [:login/username-not-valid %])}
    :db (assoc-in db [:login-form :username :validating] true)}))

(reg-event-fx
 :login/validate-password
 (fn [_ [password]]
   {:timeout {:id :login-password
              :event [:login/validating-password password]
              :time 500}}))

(reg-event-fx
 :login/validating-password
 (fn [{db :db} [password]]
   {:validate {:target :password
               :value password
               :on-success #(dispatch [:login/password-valid])
               :on-failure #(dispatch [:login/password-not-valid %])}
    :db (assoc-in db [:login-form :password :validating] true)}))


(reg-event-db
 :login/username-valid
 [(path :login-form :username)]
 (fn [username _]
   (-> username
       (assoc :valid? true)
       (assoc :validating? false))))

(reg-event-db
 :login/username-not-valid
 [(path :login-form :username)]
 (fn [username [err-map]]
   (-> username
       (assoc :valid? false)
       (assoc :error (:username err-map))
       (assoc :validating? false))))

(reg-event-db
 :login/password-valid
 [(path :login-form :password)]
 (fn [password _]
   (-> password
       (assoc :valid? true)
       (assoc :validating? false))))

(reg-event-db
 :login/password-not-valid
 [(path :login-form :password)]
 (fn [password [err-map]]
   (-> password
       (assoc :valid? false)
       (assoc :error (:password err-map))
       (assoc :validating? false))))


(reg-event-fx
 :login/login
 [server]
 (fn [{db :db} _]
   {:http-xhrio {:method :post
                 :uri "/user/login"
                 :params {:username (get-in db [:login-form :username :value])
                          :password (get-in db [:login-form :password :value])}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login/success]
                 :on-failure [:login/fail true]}
    :db (assoc-in db [:login-form :processing?] true)}))

(reg-event-fx
 :login/success
 (fn [{db :db} [resp]]
   (let [user (dissoc resp :token)
         token (:token resp)]
     {:db (-> db
              (assoc-in [:user :current] user)
              (assoc-in [:user :token] token)
              (assoc-in [:login-form :errors?] false)
              (assoc-in [:login-form :processing?] false)
              (assoc-in [:user :checking-auth?] false))
      :navigate-to [:home]
      :dispatch-n [[:notifs/connect-ws]
                   [:notifs/get-unreads]
                   [:channels/load]]})))

(reg-event-db
 :login/fail
 (fn [db [errors? resp]]
   (-> db
       (assoc-in [:login-form :errors?] errors?)
       (assoc-in [:login-form :processing?] false)
       (assoc-in [:user :checking-auth?] false))))
