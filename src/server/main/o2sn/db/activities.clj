(ns o2sn.db.activities
  (:require [o2sn.db.core :as db]))

(defn all-activities []
  (let [q-str "for a in activities
                 return a"]
    (db/query! q-str)))

(defn get-last [user-id n]
  (let [q-str "for a in activities
                filter a.by == @userid
                sort a._key desc
                limit @n
                return merge(a,
                {target : document(a.target)})"]
    (db/query! q-str {:userid user-id
                      :n n})))

(defn create-activity [activity]
  (db/with-coll :activities
    (-> (db/insert-doc! activity
                        {:return-new true}
                        [:new])
        :new
        db/ednize)))

(defn relevant-to [activity-k user-k]
  (let [activity-id (str "activities/" activity-k)
        user-id (str "users/" user-k)]
    (db/with-coll :relevant_to
      (db/insert-doc! {:_from activity-id :_to user-id :read false}))))

(defn relevant-to-all [activity-k users-ks]
  (doseq [user-k users-ks]
    (relevant-to activity-k user-k)))

(defn get-unreads [user-id]
  (let [q-str "for v,e,p in 1..1 inbound @userid relevant_to
                filter e.read == false
                  let target = document(v.target)
                  let by = document(v.by)
                  return merge(v,{target : target, by : by})"]
    (db/query! q-str {:userid user-id})))

(defn get-all [user-id]
  (let [q-str "for v,e,p in 1..1 inbound @userid relevant_to
                  let target = document(v.target)
                  let by = document(v.by)
                  return merge(v,{target : target, by : by})"]
    (db/query! q-str {:userid user-id})))

(defn mark-read [user-id activity-id]
  (let [q-str "for v,e,p in 1..1 outbound @activityid relevant_to
                filter v._id == @userid
                  update merge(e,{read : true}) in relevant_to
                  return OLD"]
    (-> (db/query! q-str {:activityid activity-id
                          :userid user-id})
        boolean)))

(defn mark-read-all [user-id]
  (let [q-str "for v,e,p in 1..1 inbound @userid relevant_to
                update merge(e,{read : true}) in relevant_to
                return NEW"]
    (-> (db/query! q-str {:userid user-id})
        boolean)))
