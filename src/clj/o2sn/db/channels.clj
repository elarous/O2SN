(ns o2sn.db.channels
  (:require [o2sn.db.core :as db]))



(defn by-user [user-key]
  (let [id (str "users/" user-key)
        q-str "for c in 1..1 outbound @id subscribe
                let subscribers =
                  (for u in 1..1 inbound c._id subscribe
                    return u)
                for l in locations
                  filter c.location == l._id
                    return {_key : c._key,
                            name : l.name,
                            type : l.type,
                            subscribers : count(subscribers)}"]
    (db/query! q-str {:id id})))

(defn find-by-loc [loc-id]
  (let [q-str "for c in channels
                filter c.location == @locid
                  let subscribers =
                      (for u in 1..1 inbound c._id subscribe
                        return u)
                  let loc = document(@locid)
                  return {_key : c._key,
                         name : loc.name,
                         type : loc.type,
                         subscribers : count(subscribers)}"]
    (-> (db/query! q-str {:locid loc-id}) first)))

(defn create [loc-key]
  (if-let [chan (find-by-loc (str "locations/" loc-key))]
    chan
    (let [id (str "locations/" loc-key)]
      (db/with-coll :channels
        (-> (db/insert-doc! {:location id}
                            {:return-new true}
                            [:new])
            :new
            db/ednize
            :location
            find-by-loc)))))

(defn subscribed? [user-k chan-k]
  (let [user-id (str "users/" user-k)
        chan-id (str "channels/" chan-k)
        q-str "for s in subscribe
                 filter s._from == @userid
                 and s._to == @chanid
                   return s"]
    (-> (db/query! q-str {:userid user-id
                          :chanid chan-id})
        first)))

(defn subscribe [user-k chan-k]
  (if-not (subscribed? user-k chan-k)
    (let [user-id (str "users/" user-k)
          chan-id (str "channels/" chan-k)]
      (db/with-coll :subscribe
        (db/insert-doc! {:_from user-id :_to chan-id}))
      true)
    false))

(defn unsubscribe [user-k chan-k]
  (let [user-id (str "users/" user-k)
        chan-id (str "channels/" chan-k)
        q-str "for s in subscribe
                 filter s._from == @userid
                 and s._to == @chanid
                 remove s in subscribe"]
    (db/query! q-str {:userid user-id :chanid chan-id})))

#_(subscribed? "1" "1836319")
#_(create "1763553")
#_(find-by-loc "1747504")
#_(by-user 1)
#_(subscribe "2" "2")
