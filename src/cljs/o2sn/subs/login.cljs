(ns o2sn.subs.login
  (:require [o2sn.db :as db]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 :login-username
 (fn [db _]
   (get-in db [:login-form :username :value])))

(reg-sub
 :login-password
 (fn [db _]
   (get-in db [:login-form :password :value])))

(reg-sub
 :login-errors?
 (fn [db _]
   (get-in db [:login-form :errors?])))

(reg-sub
 :login-processing?
 (fn [db _]
   (get-in db [:login-form :processing?])))

(reg-sub
 :login-username-valid?
 (fn [db _]
   (get-in db [:login-form :username :valid])))

(reg-sub
 :login-username-error
 (fn [db _]
   (get-in db [:login-form :username :error])))

(reg-sub
 :login-username-validating?
 (fn [db _]
   (get-in db [:login-form :username :validating])))

(reg-sub
 :login-password-valid?
 (fn [db _]
   (get-in db [:login-form :password :valid])))

(reg-sub
 :login-password-error
 (fn [db _]
   (get-in db [:login-form :password :error])))

(reg-sub
 :login-password-validating?
 (fn [db _]
   (get-in db [:login-form :password :validating])))

(reg-sub
 :login-button-enabled?
 (fn [db _]
   (and (get-in db [:login-form :username :valid])
        (get-in db [:login-form :password :valid]))))

