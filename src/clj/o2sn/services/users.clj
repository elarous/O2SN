(ns o2sn.services.users
  (:require [buddy.auth.accessrules :refer [restrict success error]]
            [buddy.auth :refer [authenticated?]]
            [buddy.hashers :as hashers]
            [buddy.core.hash :as h]
            [buddy.core.codecs :as c]
            [ring.util.http-response :refer :all]
            [o2sn.db.users :as db]
            [o2sn.validation :as v]
            [postal.core :as email]
            [o2sn.layout :as layout]
            [o2sn.config :as config]))

(def email-config (:email-config config/env))
(def host (str (:addr (:server config/env))
               ":"
               (:port (:server config/env))))

(defn by-key [k]
  (db/get-user :key k))

;; TODO : the hard coded link should be changed later
(defn- send-confirm! [email]
  (let [confirm-hash (-> (h/sha256 (str (rand 10000)))
                         (c/bytes->hex))
        email-body (str "click this link to confirm your account "
                        "<a href=\"http://"
                        host
                        "/user/confirm/"
                        confirm-hash
                        "\">Confirm</a>")]
    (email/send-message (select-keys email-config [:host :user :pass :ssl])
                        {:from (:email email-config)
                         :to [email]
                         :cc ""
                         :subject "Confirm your o2sn account"
                         :body [{:type "text/html"
                                 :content email-body}]})
    confirm-hash))

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
  (if-let [user (db/get-user :username username)]
    (if (hashers/check password (:password user))
      (->> (ok user)
           (set-user! user req))
      (unauthorized "bad credentials."))
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
        (error "you should be ayoun admin in order to access this."))))

(def not-authenticated? (complement authenticated?))

(defn username-exists? [username]
  (ok (db/username-exists? username)))

(defn email-exists? [email]
  (ok (db/email-exists? email)))

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
             (-> (update user :password hashers/derive)
                 (assoc :hash (send-confirm! (:email user)))
                 (assoc :activated false)))
            (ok {:results "signed up successfully"})))
      (let [first-entry (-> v first first) ;; in case of validation errors
            k (first first-entry)
            v (second first-entry)]
        (bad-request (hash-map :title (str "Invalid " (name k))
                               :content v))))))

(defn confirm-accnt [hash]
  (if (db/activate-accnt! hash)
    (-> (layout/render "account_confirmed.html")
        (header "Content-Type" "text/html"))
    (ok "Error ! cannot activate account")))
