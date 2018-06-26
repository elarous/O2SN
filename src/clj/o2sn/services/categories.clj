(ns o2sn.services.categories
  (:require [o2sn.db.categories :as db]
            [ring.util.http-response :refer :all]))

(defn get-all []
  (ok (db/all)))
