(ns o2sn.subs.core
  (:require [o2sn.db :as db]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 :page
 (fn [db _]
   (get-in db [:page :active])))

(reg-sub
 :page-hiding?
 (fn [db _]
   (get-in db [:page :hiding?])))

(reg-sub
 :docs
 (fn [db _]
   (:docs db)))

(reg-sub
 :user-logged-in
 (fn [db _]
   (get-in db [:user :logged-in?])))

(reg-sub
 :current-user
 (fn [db _]
   (get-in db [:user :current])))

(reg-sub
 :sidebar-visible
 (fn [db _]
   (get-in db [:sidebar :visible])))

