(ns o2sn.subs.profile
  (:require [re-frame.core :refer [reg-sub]]
            [clojure.string :as s]))

(reg-sub
 :profile/avatar
 (fn [db _]
   (get-in db [:profile :infos :avatar])))
(reg-sub
 :profile/fullname
 (fn [db _]
   (get-in db [:profile :infos :fullname])))

(reg-sub
 :profile/username
 (fn [db _]
   (get-in db [:profile :infos :username])))

(reg-sub
 :profile/email
 (fn [db _]
   (get-in db [:profile :infos :email])))

(reg-sub
 :profile/age
 (fn [db _]
   (get-in db [:profile :infos :age])))

(reg-sub
 :profile/gender
 (fn [db _]
   (get-in db [:profile :infos :gender])))

(reg-sub
 :profile/country
 (fn [db _]
   (get-in db [:profile :infos :country])))

(reg-sub
 :profile/stats
 (fn [db _]
   (get-in db [:profile :stats])))

(reg-sub
 :profile/activities
 (fn [db _]
   (get-in db [:profile :activities])))

(reg-sub
 :profile/rating
 (fn [db _]
   (get-in db [:profile :rating])))

(reg-sub
 :profile/loading?
 (fn [db [_ segment]]
   (get-in db [:profile :loading? segment])))
