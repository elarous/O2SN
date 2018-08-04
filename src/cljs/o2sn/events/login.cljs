(ns o2sn.events.login
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug
                                   dispatch]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.validation :as v]))



;;effect handlers
(reg-fx
 :validate
 (fn [[target value & args]]
   (case target
     :email (let [f (fn []
                      (v/email-exists?
                       value
                       #(dispatch [:email-valid])
                       #(dispatch [:email-not-valid %])))]
              (v/validate-email
               value
               f
               #(dispatch [:email-not-valid %])))
     :email-ajax (v/email-deliverable?
                  value
                  #(dispatch [:email-valid])
                  #(dispatch [:email-not-valid %]))
     :username (v/validate-username
                value
                #(dispatch [:username-valid (first args)])
                #(dispatch [:username-not-valid % (first args)]))
     :username-ajax (v/username-exists?
                     value
                     #(dispatch [:username-valid (first args)])
                     #(dispatch [:username-not-valid % (first args)]))
     :password (v/validate-password
                value
                #(dispatch [:password-valid (first args)])
                #(dispatch [:password-not-valid % (first args)]))

     :repassword (v/validate-repassword
                  value
                  (first args) ;; the password
                  #(dispatch [:repassword-valid])
                  #(dispatch [:repassword-not-valid %]))
     :new-story (v/validate-new-story
                 value
                 #(dispatch [:new-story/data-valid])
                 #(dispatch [:new-story/data-not-valid %])))))

(reg-event-fx
 :login
 (fn [{db :db} [_]]
   {:http-xhrio {:method :post
                 :uri "/user/login"
                 :params {:username (get-in db [:login-form :username :value])
                          :password (get-in db [:login-form :password :value])}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-success]
                 :on-failure [:login-fail true]}
    :db (assoc-in db [:login-form :processing?] true)}))

(reg-event-fx
 :login-success
 (fn [{db :db} [_ resp]]
   {:db (-> (assoc-in db [:user :logged-in?] true)
            (assoc-in [:user :current] resp)
            (assoc-in [:login-form :errors?] false)
            (assoc-in [:login-form :processing?] false)
            (assoc :checking-auth? false))
    :dispatch-n [[:set-active-page :home]
                 [:profile/load-profile (:_key resp)]
                 [:notifs/get-notifs]
                 [:get-categories]]
    :notifs/create-ws nil}))

(reg-event-db
 :login-fail
 (fn [db [_ errors? resp]]
   (-> (assoc-in db [:user :logged-in?] false)
       (assoc-in [:login-form :errors?] errors?)
       (assoc-in [:login-form :processing?] false)
       (assoc :checking-auth? false))))

;; login form events
(reg-event-db
 :set-login-username
 (fn [db [_ new-val]]
   (assoc-in db [:login-form :username :value] new-val)))

(reg-event-db
 :set-login-password
 (fn [db [_ new-val]]
   (assoc-in db [:login-form :password :value] new-val)))

(reg-event-fx
 :validate-login-username
 (fn [{db :db} [_ username]]
   {:timeout {:id :login-username
              :event [:validating-login-username username]
              :time 500}}))

(reg-event-fx
 :validating-login-username
 (fn [{db :db} [_ username]]
   {:validate [:username username :login-form]
    :db (assoc-in db [:login-form :username :validating] true)}))

(reg-event-fx
 :validate-login-password
 (fn [{db :db} [_ password]]
   {:timeout {:id :login-password
              :event [:validating-login-password password]
              :time 500}}))

(reg-event-fx
 :validating-login-password
 (fn [{db :db} [_ password]]
   {:validate [:password password :login-form]
    :db (assoc-in db [:login-form :password :validating] true)}))
