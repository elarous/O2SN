(ns o2sn.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict success error]]
            [buddy.auth :refer [authenticated?]]
            [o2sn.services.users :as users]))

(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}
  (GET "/authenticated" []
    :auth-rules authenticated?
    :current-user user
    (ok {:user user}))
  (context "/api" []
    :tags ["thingie"]

    (GET "/user/exists/:username" []
      :auth-rules users/not-authenticated?
      :path-params [username :- String]
      :return Boolean
      :summary "check whether a user with the given username exists"
      (users/username-exists? username))

    (GET "/user/email/exists/:email" []
      :auth-rules users/not-authenticated?
      :path-params [email :- String]
      :return Boolean
      :summary "check whether an email with the given username exists"
      (users/email-exists? email))

    (POST "/user/signup" []
      :auth-rules users/not-authenticated?
      :body-params [email :- String
                    username :- String
                    password :- String]
      :summary "sign up a new user"
      (users/signup-user (hash-map :email email
                                   :username username
                                   :password password)))

    (POST "/login" req
      :auth-rules users/not-authenticated?
      :body-params [username :- String, password :- String]
      :summary "login the user"
      (users/login username password req))

    (POST "/logout" req
      :auth-rules authenticated?
      :summary "logout the current user."
      (users/logout req))

    (GET "/admin" []
      :auth-rules users/admin?
      :summary "the admin page"
      (ok "Admin Data"))))
