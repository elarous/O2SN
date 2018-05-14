(ns o2sn.validation
  #?(:clj (:require [struct.core :as st]
                    [ajax.core :as ajax :refer [ajax-request]]
                    [clojure.string :as str])
     :cljs (:require [struct.core :as st]
                     [ajax.core :as ajax :refer [ajax-request]]
                     [clojure.string :as str]
                     [cljs.core.async :as a]))
  #?(:cljs (:require-macros [cljs.core.async :refer [go]])))

;; custom validators

(def contains-uppercase
  {:message "must contain an uppercase letter"
   :optional false
   :validate #(re-matches #".*[A-Z]+.*" (str %))})

(def contains-number
  {:message "must contain a number"
   :optional false
   :validate #(re-matches #".*[0-9]+.*" (str %))})

(def no-space
  {:message "should not contain space"
   :optional false
   :validate #(not (re-matches #".*\s+.*" %))})

(def alpha-nums
  {:message "must only contain characters 'A-z', '0-9', '-' and '_'"
   :optional false
   :validate #(re-matches #"^[A-z0-9-_]+$" %)})

;; end of custom validators

(def email-schema
  {:email [st/required st/email]})

(def username-schema
  {:username [st/required
              st/string
              no-space
              alpha-nums
              [st/min-count 3 :message "must be at least 3 characters"]
              [st/max-count 20 :message "must be at most 20 characters"]]})

(def password-schema
  {:password [st/required
              st/string
              contains-uppercase
              contains-number
              no-space
              [st/min-count 8 :message "must be at least 8 characters"]
              [st/max-count 20 :message "must be at most 20 characters"]]})

(def repassword-schema
  {:repassword [st/required [st/identical-to :password]]})

(def passwords-schema (merge password-schema repassword-schema))

(def signup-form-schema (merge email-schema username-schema password-schema repassword-schema))

;; validations that requires some ajax requests

(defn email-deliverable? [email success-fn failure-fn]
  (if (= email "")
    (failure-fn {:email "email is empty"})
    (let [err-ret {:email "email is not deliverable"}]
      (ajax-request
       {:uri (str "https://api.trumail.io/v1/json/" email)
        :method :get
        :format (ajax/json-request-format)
        :response-format (ajax/json-response-format {:keywords? true})
        :handler (fn [[ok resp]]
                   (if ok
                     (if (:deliverable resp)
                       (success-fn)
                       (failure-fn err-ret))
                     (if (= (:status resp) 429)
                       (success-fn)
                       (failure-fn err-ret))))}))))

(defn email-exists? [email success-fn failure-fn]
  (if (= email "")
    (failure-fn {:email "email is empty"})
    (let [err-ret {:email "email already exists"}]
      (ajax-request
       {:uri (str "/user/email/exists/" email)
        :method :get
        :format (ajax/text-request-format)
        :response-format (ajax/json-response-format {:keywords? true})
        :handler (fn [[ok resp]]
                   (if ok
                     (if-not (boolean resp)
                       (success-fn)
                       (failure-fn err-ret))))}))))

(defn validate-email [email success-fn failure-fn]
  (let [v (st/validate (hash-map :email email) email-schema)]
    (if (nil? (first v))
      (success-fn)
      (failure-fn (first v)))))

(defn username-exists? [username success-fn failure-fn]
  (if (= username "")
    (failure-fn {:username "username is empty"})
    (let [err-ret {:username "username already exists"}]
      (ajax-request
       {:uri (str "/user/exists/" username)
        :method :get
        :format (ajax/text-request-format)
        :response-format (ajax/json-response-format {:keywords? true})
        :handler (fn [[ok resp]]
                   (if ok
                     (if-not (boolean resp)
                       (success-fn)
                       (failure-fn err-ret))))}))))

(defn validate-username [username success-fn failure-fn]
  (let [v (st/validate (hash-map :username username) username-schema)]
    (if (nil? (first v))
      (success-fn)
      (failure-fn (first v)))))

(defn validate-password [password success-fn failure-fn]
  (let [v (st/validate (hash-map :password password) password-schema)]
    (if (nil? (first v))
      (success-fn)
      (failure-fn (first v)))))

(defn validate-repassword [repassword password success-fn failure-fn]
  (let [v (st/validate (hash-map :password password
                                 :repassword repassword)
                       passwords-schema)]
    (if (or (nil? (first v))
            (not (contains? (first v) :repassword)))
      (success-fn)
      (failure-fn (select-keys (first v) [:repassword])))))

(defn validate-signup [data]
  (st/validate data (select-keys signup-form-schema
                                 [:username :password :email])))
