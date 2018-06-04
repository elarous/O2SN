(ns o2sn.services.channels
  (:require [o2sn.db.channels :as db]
            [ring.util.http-response :refer :all]))

(defn get-channels [user-key]
  (ok (db/by-user user-key)))
