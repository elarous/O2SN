(ns o2sn.db.users
  (:require [o2sn.db.core :as db]
            [clojure.spec.alpha :as s]))

;; setting up the database connection
(db/set-arango!)
(db/set-db! :o2sn)

(s/def ::email string?)
(s/def ::username string?)
(s/def ::password string?)
(s/def ::_key string?)

(s/def ::user (s/keys :req-un [::_key ::username ::email ::password]))

(defn create-users-coll! []
  (db/create-coll! :users))

(defn drop-users-coll! []
  (db/drop-coll! :users))

(defn create-user! [user]
  (db/with-coll :users
    (-> (db/insert-doc!
         user
         {:return-new true}
         [:new])
        :new
        db/ednize)))

(defn get-user
  ([k]
   (db/with-coll :users
     (db/get-doc k)))
  ([username password]
   (let [q-str "for u in users
                filter u.username == @username
                and u.password == @password
                return u"]
     (-> (db/query! q-str {:username username :password password})
         first))))

(defn username-exists? [username]
  (let [q-str "for u in users
               filter u.username == @username
               return u != null"]
    (-> (db/query! q-str {:username username})
        first
        boolean)))

(defn email-exists? [email]
  (let [q-str "for u in users
               filter u.email == @email
               return u != null"]
    (-> (db/query! q-str {:email email})
        first
        boolean)))

(defn delete-user! [user]
  (if-let [k (cond (map? user) (:_key user)
                   (keyword? user) user)]
    (db/with-coll :users
      (db/delete-doc! k))))

(defn update-user! [user new-val]
  (if-let [k (cond (map? user) (:_key user)
                   (keyword? user) user)]
    (db/with-coll :users
      (db/update-doc! k new-val))))

(defn activate-accnt! [hash]
  (let [q-str "for u in users
               filter u.hash == @hash
               update u with {activated : true} in users
               return NEW"]
    (-> (db/query! q-str {:hash hash})
        first)))
