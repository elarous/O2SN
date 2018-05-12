(ns o2sn.services.users
  (:require [buddy.auth.accessrules :refer [restrict success error]]
            [buddy.auth :refer [authenticated?]]
            [buddy.hashers :as hashers]
            [ring.util.http-response :refer :all]
            [o2sn.db.users :as db]
            [o2sn.validation :as v]))

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

(defn username-exists? [username]
  (ok (db/username-exists? username)))

(defn email-exists? [email]
  (ok (db/email-exists? email)))

;; in case of an error (bad-request {:title "Bad Name"
;; :content "nadal is a very bad name please change it"})

(defn signup-user [user]
  (let [v (v/validate-signup user)]
    (if (nil? (first v)) ;; no validation errors
      (cond
        (db/username-exists? (:username user))
        (bad-request {:title "username already exists"
                      :content "a user with the given username already exists"})
        (db/email-exists? (:email user))
        (bad-request {:title "email already exists"
                      :content "a user with the given email already exists"})
        :else
        (do (db/create-user!
             (update user :password hashers/derive))
            (ok {:results "signed up successfully"})))
      (let [first-entry (-> v first first) ;; in case of validation errors
            k (first first-entry)
            v (second first-entry)]
        (bad-request (hash-map :title (str "Invalid " (name k))
                               :content v))))))
