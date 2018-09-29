(ns o2sn.services.images
  (:require [o2sn.db.images :as db]
            [ring.util.http-response :refer :all]))

(defn get [id]
  (ok (db/by-id id)))
