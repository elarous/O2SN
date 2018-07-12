(ns o2sn.services.search
  (:require [ring.util.http-response :refer :all]
            [o2sn.db.search :as db]))

(defn stories [user-id v]
  (->> (db/stories user-id v)
      (map #(assoc % :type "story"))
      ok))

(defn users [v]
  (->> (db/users v)
       (map #(assoc % :type "user"))
       ok))

