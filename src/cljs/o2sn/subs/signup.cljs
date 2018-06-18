(ns o2sn.subs.signup
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 :signup-email
 (fn [db _]
   (get-in db [:signup-form :email :value])))

(reg-sub
 :signup-username
 (fn [db _]
   (get-in db [:signup-form :username :value])))

(reg-sub
 :signup-password
 (fn [db _]
   (get-in db [:signup-form :password :value])))

(reg-sub
 :signup-repassword
 (fn [db _]
   (get-in db [:signup-form :repassword :value])))

(reg-sub
 :signup-processing?
 (fn [db _]
   (get-in db [:signup-form :processing?])))

(reg-sub
 :signup-errors?
 (fn [db _]
   (get-in db [:signup-form :errors?])))

(reg-sub
 :signup-email-valid?
 (fn [db _]
   (get-in db [:signup-form :email :valid])))

(reg-sub
 :signup-username-valid?
 (fn [db _]
   (get-in db [:signup-form :username :valid])))

(reg-sub
 :signup-password-valid?
 (fn [db _]
   (get-in db [:signup-form :password :valid])))

(reg-sub
 :signup-repassword-valid?
 (fn [db _]
   (get-in db [:signup-form :repassword :valid])))

(reg-sub
 :signup-email-error
 (fn [db _]
   (get-in db [:signup-form :email :error])))

(reg-sub
 :signup-username-error
 (fn [db _]
   (get-in db [:signup-form :username :error])))

(reg-sub
 :signup-password-error
 (fn [db _]
   (get-in db [:signup-form :password :error])))

(reg-sub
 :signup-repassword-error
 (fn [db _]
   (get-in db [:signup-form :repassword :error])))

(reg-sub
 :signup-email-validating?
 (fn [db _]
   (get-in db [:signup-form :email :validating])))

(reg-sub
 :signup-username-validating?
 (fn [db _]
   (get-in db [:signup-form :username :validating])))

(reg-sub
 :signup-password-validating?
 (fn [db _]
   (get-in db [:signup-form :password :validating])))

(reg-sub
 :signup-repassword-validating?
 (fn [db _]
   (get-in db [:signup-form :repassword :validating])))

(reg-sub
 :signup-button-enabled
 (fn [db _]
   (and (get-in db [:signup-form :email :valid])
        (get-in db [:signup-form :username :valid])
        (get-in db [:signup-form :password :valid])
        (get-in db [:signup-form :repassword :valid]))))

(reg-sub
 :signup-error-header
 (fn [db _]
   (get-in db [:signup-form :error :header])))

(reg-sub
 :signup-error-msg
 (fn [db _]
   (get-in db [:signup-form :error :msg])))

(reg-sub
 :signed-up?
 (fn [db _]
   (get-in db [:signup-form :signed-up?])))

(reg-sub
 :form-control-activated?
 (fn [db [_ form ctrl]]
   (get-in db [form ctrl :activated?])))
