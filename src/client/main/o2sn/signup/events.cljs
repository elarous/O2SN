(ns o2sn.signup.events
  (:require [kee-frame.core :refer [reg-event-db reg-event-fx]]
            [re-frame.core :refer [path dispatch]]
            [ajax.core :as ajax]
            [o2sn.common.interceptors :refer [server]]
            #_[o2sn.validation :as v]))

;; sign up form events
(reg-event-db
 :signup/set-email
 [(path :signup-form :email :value)]
 (fn [_ [email]]
   email))

(reg-event-db
 :signup/set-username
 [(path :signup-form :username :value)]
 (fn [_ [username]]
   username))

(reg-event-db
 :signup/set-password
 [(path :signup-form :password :value)]
 (fn [_ [password]]
   password))

(reg-event-db
 :signup/set-repassword
 [(path :signup-form :repassword :value)]
 (fn [_ [repassword]]
   repassword))

(reg-event-db
 :set-form-control-activated?
 (fn [db [form ctrl]]
   (assoc-in db [form ctrl :activated?] true)))

(reg-event-fx
 :signup/validate-email
 (fn [_ [email]]
   {:timeout {:id :signup-email
              :event [:signup/validating-email email]
              :time 500}}))

(reg-event-fx
 :signup/validating-email
 (fn [{db :db} [email]]
   {:validate {:target :email
               :value email
               :on-success #(dispatch [:signup/email-valid])
               :on-failure #(dispatch [:signup/email-not-valid %])}
    :db (assoc-in db [:signup-form :email :validating] true)}))

(reg-event-fx
 :signup/validate-email-available?
 (fn [_ [email]]
   {:timeout {:id :signup-email-available
              :event [:signup/validating-email-available? email]
              :time 500}}))

(reg-event-fx
 :signup/validating-email-available?
 (fn [{db :db} [email]]
   {:validate {:target :email-available?
               :value email
               :on-success #(dispatch [:signup/validating-email-deliverable?
                                       email])
               :on-failure #(dispatch [:signup/email-not-valid %])}
    :db (assoc-in db [:signup-form :email :validating] true)}))

(reg-event-fx
 :signup/validating-email-deliverable?
 (fn [{db :db} [email]]
   {:validate {:target :email-deliverable?
               :value email
               :on-success #(dispatch [:signup/email-valid])
               :on-failure #(dispatch [:signup/email-not-valid %])}
    :db (assoc-in db [:signup-form :email :validating] true)}))

(reg-event-db
 :signup/email-valid
 [(path :signup-form :email)]
 (fn [email _]
   (-> email
       (assoc :valid true)
       (assoc :validating false))))

(reg-event-db
 :signup/email-not-valid
 [(path :signup-form :email)]
 (fn [email [err-map]]
   (-> email
       (assoc :valid false)
       (assoc :error (:email err-map))
       (assoc :validating false))))

(reg-event-fx
 :signup/validate-username
 (fn [_ [username]]
   {:timeout {:id :signup-username
              :event [:signup/validating-username username]
              :time 500}}))

(reg-event-fx
 :signup/validating-username
 (fn [{db :db} [username]]
   {:validate {:target :username
               :value username
               :on-success #(dispatch [:signup/username-valid])
               :on-failure #(dispatch [:signup/username-not-valid %])}
    :db (assoc-in db [:signup-form :username :validating] true)}))

(reg-event-fx
 :signup/validate-username-available?
 (fn [_ [username]]
   {:timeout {:id :signup-username-available?
              :event [:signup/validating-username-available? username]
              :time 500}}))

(reg-event-fx
 :signup/validating-username-available?
 (fn [{db :db} [username]]
   {:validate {:target :username-available?
               :value username
               :on-success #(dispatch [:signup/username-valid])
               :on-failure #(dispatch [:signup/username-not-valid %])}
    :db (assoc-in db [:signup-form :username :validating] true)}))

(reg-event-db
 :signup/username-valid
 [(path :signup-form :username)]
 (fn [username [form]]
   (-> username
       (assoc :valid true)
       (assoc :validating false))))

(reg-event-db
 :signup/username-not-valid
 [(path :signup-form :username)]
 (fn [username [err-map form]]
   (-> username
       (assoc :valid false)
       (assoc :error (:username err-map))
       (assoc :validating false))))

(reg-event-fx
 :signup/validate-password
 (fn [_ [password]]
   {:timeout {:id :signup-password
              :event [:signup/validating-password password]
              :time 500}}))

(reg-event-fx
 :signup/validating-password
 (fn [{db :db} [password]]
   {:validate {:target :password
               :value password
               :on-success #(dispatch [:signup/password-valid])
               :on-failure #(dispatch [:signup/password-not-valid %])}
    :db (assoc-in db [:signup-form :password :validating] true)}))

(reg-event-db
 :signup/password-valid
 [(path :signup-form :password)]
 (fn [password [form]]
   (-> password
       (assoc :valid true)
       (assoc :validating false))))

(reg-event-db
 :signup/password-not-valid
 [(path :signup-form :password)]
 (fn [password [err-map form]]
   (-> password
       (assoc :valid false)
       (assoc :error (:password err-map))
       (assoc :validating false))))

(reg-event-fx
 :signup/validate-repassword
 (fn [_ [repassword]]
   {:timeout {:id :signup-repassword
              :event [:signup/validating-repassword repassword]
              :time 500}}))

(reg-event-fx
 :signup/validating-repassword
 (fn [{db :db} [repassword]]
   {:validate {:target :repassword
               :value repassword
               :password (get-in db [:signup-form :password :value])
               :on-success #(dispatch [:signup/repassword-valid])
               :on-failure #(dispatch [:signup/repassword-not-valid %])}
    :db (assoc-in db [:signup-form :repassword :validating] true)}))

(reg-event-db
 :signup/repassword-valid
 [(path :signup-form :repassword)]
 (fn [repassword _]
   (-> repassword
       (assoc :valid true)
       (assoc :validating false))))

(reg-event-db
 :signup/repassword-not-valid
 [(path :signup-form :repassword)]
 (fn [repassword [err-map]]
   (-> repassword
       (assoc :valid false)
       (assoc :error (:repassword err-map))
       (assoc :validating false))))

(reg-event-fx
 :signup/signup
 [server]
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
                   :on-success [:signup/signup-success]
                   :on-failure [:signup/signup-failure]}})))

(reg-event-db
 :signup/signup-success
 [(path :signup-form)]
 (fn [form _]
   (-> form
       (assoc :processing? false)
       (assoc :errors? false)
       (assoc :signed-up? true))))

(reg-event-db
 :signup/signup-failure
 [(path :signup-form)]
 (fn [form [{resp :response}]]
   (-> form
       (assoc :processing? false)
       (assoc :errors? true)
       (assoc-in [:error :header] (:title resp))
       (assoc-in [:error :msg] (:content resp)))))
