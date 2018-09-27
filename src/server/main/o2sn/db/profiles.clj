(ns o2sn.db.profiles
  (:require [o2sn.db.core :as db]))

(defn get-profile [user-k]
  (let [user-id (str "users/" user-k)
        q-str "let u = document(@userid)
               let p = document(u.profile)
              return  merge(p,
              {\"username\" : u.username,
               \"email\" : u.email,
               \"avatar\" : u.avatar})"]
    (-> (db/query! q-str {:userid user-id})
        first)))

(defn get-activities [user-k]
  (let [user-id (str "users/" user-k)
        q-str "let u = document(@userid)
               for a in activities
                filter a.user == u._id
                return a"]
    (db/query! q-str {:userid user-id})))

(defn get-stats [user-k]
  (let [user-id (str "users/" user-k)
        q-str "let stories = (for s in 1..1 outbound @userid own return s)
               let truths = (for t in 1..1 outbound @userid truth return t)
               let lies = (for l in 1..1 outbound @userid lie return l)
               let likes =  (for l in 1..1 outbound @userid liking return l)
               let dislikes = (for l in 1..1 outbound @userid disliking return l)

               return {stories : count(stories),
                       truths : count(truths),
                       lies : count(lies),
                       likes : count(likes),
                       dislikes : count(dislikes)}"]
    (-> (db/query! q-str {:userid user-id})
        first)))

(defn get-rating [user-k]
  (let [user-id (str "users/" user-k)
        q-str "let t_arr =
                (for s in 1..1 outbound @userid own
                  return count(for t in 1..1 inbound s._id truth
                            return t))
               let l_arr =
                (for s in 1..1 outbound @userid own
                  return count(for l in 1..1 inbound s._id lie
                            return l))
               return {truths: sum(t_arr), lies: sum(l_arr)}"]
    (-> (db/query! q-str {:userid user-id})
        first
        (update :truths #(java.lang.Math/round %))
        (update :lies #(java.lang.Math/round %)))))

(defn create-profile [{:keys [fullname country age gender]}]
  (db/with-coll :profiles
    (-> (db/insert-doc! {:fullname fullname
                         :country country
                         :age age
                         :gender gender}
                        {:return-new true}
                        [:new])
        :new
        db/ednize)))
