(ns o2sn.services.users
  (:require [buddy.auth.accessrules :refer [restrict success error]]
            [buddy.auth :refer [authenticated?]]
            [ring.util.http-response :refer :all]
            [o2sn.db.users :as db]))

(defn set-user! [user {session :session} resp]
  (->> (assoc session :identity (:_key user))
       (assoc resp :session)))

(defn remove-user! [session resp]
  (-> resp
      (assoc :session (dissoc session :identity))))

(defn clear-session! [resp]
  (-> resp
      (dissoc :session)))

(defn login [username password req]
  (if-let [user (db/get-user username password)]
    (do
      (->> (ok "user logged in successfully")
           (set-user! user req)))
    (unauthorized "bad credentials.")))

(defn logout [req]
  (remove-user! (:session req) (ok "user logged out")))

;; access rules

(defn admin? [req]
  true)

#_(defn admin? [req]
  (let [user (get-in req [:session :identity])]
    (if (= user "admin")
      true
      (error "you should be an admin in order to access this."))))

(def not-authenticated? (complement authenticated?))
