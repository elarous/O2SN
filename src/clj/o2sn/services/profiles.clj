(ns o2sn.services.profiles
  (:require [o2sn.db.profiles :as db]
            [ring.util.http-response :refer :all]))

(defn get-profile [user-k]
  (ok (db/get-profile user-k)))

(defn get-activities [user-k]
  (ok (db/get-activities user-k)))

(defn get-stats [user-k]
  (ok (db/get-stats user-k)))

(defn get-rating [user-k]
  (ok (db/get-rating user-k)))
