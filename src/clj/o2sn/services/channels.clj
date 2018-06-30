(ns o2sn.services.channels
  (:require [o2sn.db.channels :as db]
            [ring.util.http-response :refer :all]
            [o2sn.db.locations :as locations]))

(defn get-channels [user-key]
  (ok (db/by-user user-key)))

(defn add-channel [locs user-k]
  (let [loc (locations/insert-locs-seq! (reverse locs))
        chan (db/create (:_key loc))
        just-subscribed? (db/subscribe user-k (:_key chan))]
    (if just-subscribed?
      (ok (update chan :subscribers inc))
      (bad-request {:error "user already subscribed to this channel"}))))

(defn unsubscribe [user-k chan-k]
  (db/unsubscribe user-k chan-k)
  (ok {:chan-k chan-k}))
