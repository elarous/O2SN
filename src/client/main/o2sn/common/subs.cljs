(ns o2sn.common.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :common/db
 (fn [db _]
   db))

(reg-sub
 :common/user
 (fn [db _]
   (let [user (get-in db [:user :current])]
     (if-not (nil? (:avatar user))
       (-> user
           (assoc :avatar "img/user.svg"))
       user))))

(reg-sub
 :common/token
 (fn [db _]
   (get-in db [:user :token])))

(reg-sub
 :common/server
 (fn [db _]
   (:server db)))

(reg-sub
 :common/checking-auth?
 (fn [db _]
   (get-in db [:user :checking-auth?])))

(reg-sub
 :forms/control-activated?
 (fn [db [_ path-to-ctrl]]
   (get-in db (vec (concat path-to-ctrl [:activated?])))))



