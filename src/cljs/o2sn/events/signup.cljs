(ns o2sn.events.signup
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.validation :as v]))

;; sign up form events
(reg-event-db
 :set-signup-email
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :email :value] new-val)))

(reg-event-db
 :set-signup-username
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :username :value] new-val)))

(reg-event-db
 :set-signup-password
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :password :value] new-val)))

(reg-event-db
 :set-signup-repassword
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :repassword :value] new-val)))

(reg-event-db
 :set-form-control-activated?
 (fn [db [_ form ctrl]]
   (assoc-in db [form ctrl :activated?] true)))

(reg-event-fx
 :validate-signup-email
 (fn [{db :db} [_ email]]
   {:timeout {:id :signup-email
              :event [:validating-signup-email email]
              :time 500}}))

(reg-event-fx
 :validating-signup-email
 (fn [{db :db} [_ email]]
   {:validate [:email email]
    :db (assoc-in db [:signup-form :email :validating] true)}))

(reg-event-fx
 :validate-signup-email-ajax
 (fn [{db :db} [_ email]]
   {:timeout {:id :signup-email-ajax
              :event [:validating-signup-email-ajax email]
              :time 500}}))

(reg-event-fx
 :validating-signup-email-ajax
 (fn [{db :db} [_ email]]
   {:validate [:email-ajax email]
    :db (assoc-in db [:signup-form :email :validating] true)}))

(reg-event-db
 :email-valid
 (fn [db _]
   (-> db
       (assoc-in [:signup-form :email :valid] true)
       (assoc-in [:signup-form :email :validating] false))))

(reg-event-db
 :email-not-valid
 (fn [db [_ err-map]]
   (-> db
       (assoc-in [:signup-form :email :valid] false)
       (assoc-in [:signup-form :email :error] (:email err-map))
       (assoc-in [:signup-form :email :validating] false))))

(reg-event-fx
 :validate-signup-username
 (fn [{db :db} [_ username]]
   {:timeout {:id :signup-username
              :event [:validating-signup-username username]
              :time 500}}))

(reg-event-fx
 :validating-signup-username
 (fn [{db :db} [_ username]]
   {:validate [:username username :signup-form]
    :db (assoc-in db [:signup-form :username :validating] true)}))

(reg-event-fx
 :validate-signup-username-ajax
 (fn [{db :db} [_ username]]
   {:timeout {:id :signup-username-ajax
              :event [:validating-signup-username-ajax username]
              :time 500}}))

(reg-event-fx
 :validating-signup-username-ajax
 (fn [{db :db} [_ username]]
   {:validate [:username-ajax username :signup-form]
    :db (assoc-in db [:signup-form :username :validating] true)}))

(reg-event-db
 :username-valid
 (fn [db [_ form]]
   (-> db
       (assoc-in [form :username :valid] true)
       (assoc-in [form :username :validating] false))))

(reg-event-db
 :username-not-valid
 (fn [db [_ err-map form]]
   (-> db
       (assoc-in [form :username :valid] false)
       (assoc-in [form :username :error] (:username err-map))
       (assoc-in [form :username :validating] false))))

(reg-event-fx
 :validate-signup-password
 (fn [{db :db} [_ password]]
   {:timeout {:id :signup-password
              :event [:validating-signup-password password]
              :time 500}}))

(reg-event-fx
 :validating-signup-password
 (fn [{db :db} [_ password]]
   {:validate [:password password :signup-form]
    :db (assoc-in db [:signup-form :password :validating] true)}))

(reg-event-db
 :password-valid
 (fn [db [_ form]]
   (-> db
       (assoc-in [form :password :valid] true)
       (assoc-in [form :password :validating] false))))

(reg-event-db
 :password-not-valid
 (fn [db [_ err-map form]]
   (-> db
       (assoc-in [form :password :valid] false)
       (assoc-in [form :password :error] (:password err-map))
       (assoc-in [form :password :validating] false))))

(reg-event-fx
 :validate-signup-repassword
 (fn [{db :db} [_ repassword]]
   {:timeout {:id :signup-repassword
              :event [:validating-signup-repassword repassword]
              :time 500}}))


(reg-event-fx
 :validating-signup-repassword
 (fn [{db :db} [_ repassword]]
   {:validate [:repassword repassword (get-in db [:signup-form :password :value])]
    :db (assoc-in db [:signup-form :repassword :validating] true)}))

(reg-event-db
 :repassword-valid
 (fn [db _]
   (-> db
       (assoc-in [:signup-form :repassword :valid] true)
       (assoc-in [:signup-form :repassword :validating] false))))

(reg-event-db
 :repassword-not-valid
 (fn [db [_ err-map]]
   (-> db
       (assoc-in [:signup-form :repassword :valid] false)
       (assoc-in [:signup-form :repassword :error] (:repassword err-map))
       (assoc-in [:signup-form :repassword :validating] false))))

(reg-event-fx
 :signup
 (fn [{db :db} _]
   (let [data (hash-map :email (get-in db [:signup-form :email :value])
                        :username (get-in db [:signup-form :username :value])
                        :password (get-in db [:signup-form :password :value]))]
     {:db (assoc-in db [:signup-form :processing?] true)
      :http-xhrio {:uri "/user/signup"
                   :method :post
                   :params data
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:signup-success]
                   :on-failure [:signup-failure]}})))

(reg-event-db
 :signup-success
 (fn [db _]
   (-> db
       (assoc-in [:signup-form :processing?] false)
       (assoc-in [:signup-form :errors?] false)
       (assoc-in [:signup-form :signed-up?] true))))

(reg-event-db
 :signup-failure
 (fn [db [_ {resp :response}]]
   (-> db
       (assoc-in [:signup-form :processing?] false)
       (assoc-in [:signup-form :errors?] true)
       (assoc-in [:signup-form :error :header] (:title resp))
       (assoc-in [:signup-form :error :msg] (:content resp)))))
