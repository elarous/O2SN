(ns o2sn.db.channels
  (:require [o2sn.db.core :as db]))

(defn by-user [user-key]
  (let [id (str "users/" user-key)
        q-str "for c in 1..1 outbound @id subscribe
                 return c"]
    (db/query! q-str {:id id})))

#_(by-user 1)
