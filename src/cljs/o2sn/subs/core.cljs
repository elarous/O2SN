(ns o2sn.subs.core
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 :checking-auth?
 (fn [db _]
   (:checking-auth? db)))

(reg-sub
 :active-page
 (fn [db _]
   (let [active-page (get-in db [:page :active])]
     (get-in db [:pages active-page :page]))))

(reg-sub
 :with-menu?
 (fn [db _]
   (let [active-page (get-in db [:page :active])]
     (get-in db [:pages active-page :with-menu?]))))

(reg-sub
 :require-login?
 (fn [db [_ page]]
   (get-in db [:pages page :require-login?])))

(reg-sub
 :page-hiding?
 (fn [db _]
   (get-in db [:page :hiding?])))

;; panel subs

(reg-sub
 :active-panel
 (fn [db _]
   (let [active-panel (get-in db [:panel :active])]
     (get-in db [:panels active-panel]))))

(reg-sub
 :active-panel?
 (fn [db [_ panel]]
   (= (get-in db [:panel :active]) panel)))

(reg-sub
 :panel-hiding?
 (fn [db _]
   (get-in db [:panel :hiding?])))

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

