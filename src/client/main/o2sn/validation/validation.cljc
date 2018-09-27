(ns o2sn.validation.validation
  #?(:clj (:require [struct.core :as st]
                    [ajax.core :as ajax :refer [ajax-request]]
                    [clojure.string :as str])
     :cljs (:require [struct.core :as st]
                     [ajax.core :as ajax :refer [ajax-request]]
                     [clojure.string :as str]
                     [re-frame.core :refer [subscribe]])))


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

;; new story schemas
(def title-schema
  {:title [st/required
           [st/min-count 20 :message "must be at least 20 characters"]
           [st/max-count 80 :message "must be at most 80 characters"]]})

(def map-lat-schema
  {:lat [{:message "a point should be selected"
          :optional false
          :validate (complement zero?)}]})

(def map-lng-schema
  {:lng [{:message "a point should be selected"
          :optional false
          :validate (complement zero?)}]})

(def description-schema
  {:description  [st/required
                  [st/min-count 80 :message "must be at least 80 characters"]
                  [st/max-count 400 :message "must be at most 400 characters"]]})

(def date-schema
  {:date [{:message "invalid date format"
           :optional false
           :validate #(re-matches #"^\d{4}-\d{2}-\d{2}$" %)}]})

(def time-schema
  {:time [{:message "invalid time format"
           :optional false
           :validate #(re-matches #"^\d{2}:\d{2}$" %)}]})

(def category-schema
  {:category [st/required]})


;; validations that requires some ajax requests


(defn validate-signup [data]
  (st/validate data (select-keys signup-form-schema
                                 [:username :password :email])))

(defn- do-validate [validation on-success on-failure]
  (if (nil? (first validation))
    (on-success)
    (on-failure (first validation))))

(defmulti validate (fn [m] (:target m)))

(defmethod validate :email [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:email value} email-schema)
               on-success
               on-failure))


(defmethod validate :email-deliverable? [{:keys [value on-success on-failure]}]
  (if (= value "")
    (on-failure {:email "email is empty"})
    (let [err-ret {:email "email is not deliverable"}]
      (ajax-request
       {:uri (str "https://api.trumail.io/v2/lookups/json?email=" value)
        :method :get
        :format (ajax/json-request-format)
        :response-format (ajax/json-response-format {:keywords? true})
        :handler (fn [[ok resp]]
                   (if ok
                     (if (:deliverable resp)
                       (on-success)
                       (on-failure err-ret))
                     (if (= (:status resp) 429)
                       (on-success)
                       (on-failure err-ret))))}))))

(defmethod validate :email-available? [{:keys [value on-success on-failure]}]
  (if (= value "")
    (on-failure {:email "email is empty"})
    (let [err-ret {:email "email already exists"}
          server #?(:cljs @(subscribe [:common/server])
                    :clj "")]
      (ajax-request
       {:uri (str server "/user/email/exists/" value)
        :method :get
        :format (ajax/text-request-format)
        :response-format (ajax/json-response-format {:keywords? true})
        :handler (fn [[ok resp]]
                   (if ok
                     (if-not (boolean resp)
                       (on-success)
                       (on-failure err-ret))))}))) )

(defmethod validate :username [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:username value} username-schema)
               on-success
               on-failure))

(defmethod validate :username-available? [{:keys [value on-success on-failure]}]
  (if (= value "")
    (on-failure {:username "username is empty"})
    (let [err-ret {:username "username already exists"}
          server #?(:cljs @(subscribe [:common/server])
                    :clj "")]
      (ajax-request
       {:uri (str server "/user/exists/" value)
        :method :get
        :format (ajax/text-request-format)
        :response-format (ajax/json-response-format {:keywords? true})
        :handler (fn [[ok resp]]
                   (if ok
                     (if-not (boolean resp)
                       (on-success)
                       (on-failure err-ret))))}))) )

(defmethod validate :password [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:password value} password-schema)
               on-success
               on-failure))

(defmethod validate :repassword [{:keys [value password on-success on-failure]}]
  (let [v (st/validate (hash-map :password password
                                 :repassword value)
                       passwords-schema)]
    (if (or (nil? (first v))
            (not (contains? (first v) :repassword)))
      (on-success)
      (on-failure (select-keys (first v) [:repassword])))) )

(defmethod validate :title [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:title value} title-schema)
               on-success
               on-failure))

(defmethod validate :map [{:keys [lat lng on-success on-failure]}]
  (let [lat-val (-> (st/validate {:lat lat} map-lat-schema)
                    second :lat)
        lng-val (-> (st/validate {:lng lng} map-lng-schema)
                    second :lng)]
    (if (and (some? lat-val)
             (some? lng-val))
      (on-success)
      (on-failure {:map "a point should be selected"}))))

(defmethod validate :description [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:description value} description-schema)
               on-success
               on-failure))

(defmethod validate :date [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:date value} date-schema)
               on-success
               on-failure))

(defmethod validate :time [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:time value} time-schema)
               on-success
               on-failure))

(defmethod validate :category [{:keys [value on-success on-failure]}]
  (do-validate (st/validate {:category value} category-schema)
               on-success
               on-failure))

(defmethod validate :new-story [{:keys [data on-success on-failure]}]
  (do-validate (st/validate data (merge title-schema
                                        map-lng-schema
                                        map-lat-schema
                                        description-schema
                                        date-schema
                                        time-schema
                                        category-schema))
               on-success
               on-failure))
