(ns o2sn.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload
                                          wrap-multipart-params]]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict success error]]
            [buddy.auth :refer [authenticated?]]
            [o2sn.services.users :as users]
            [o2sn.services.channels :as channels]
            [o2sn.services.stories :as stories]
            [o2sn.services.categories :as categories]
            [o2sn.services.profiles :as profiles]
            [o2sn.services.notifications :as notifs]
            [o2sn.services.activities :as activities]))

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

  (context "/user" []
    (GET "/authenticated" []
      :auth-rules authenticated?
      :current-user user-k
      (ok (users/by-key user-k)))

    (GET "/exists/:username" []
      :auth-rules users/not-authenticated?
      :path-params [username :- String]
      :return Boolean
      :summary "check whether a user with the given username exists"
      (users/username-exists? username))

    (GET "/email/exists/:email" []
      :auth-rules users/not-authenticated?
      :path-params [email :- String]
      :return Boolean
      :summary "check whether an email with the given username exists"
      (users/email-exists? email))

    (POST "/signup" []
      :auth-rules users/not-authenticated?
      :body-params [email :- String
                    username :- String
                    password :- String]
      :summary "sign up a new user"
      (users/signup-user (hash-map :email email
                                   :username username
                                   :password password)))

    (GET "/confirm/:hash-str" []
      :auth-rules users/not-authenticated?
      :path-params [hash-str :- String]
      :summary "confirm an account"
      (users/confirm-accnt hash-str))

    (POST "/login" req
      :auth-rules users/not-authenticated?
      :body-params [username :- String, password :- String]
      :summary "login the user"
      (users/login username password req))

    (POST "/logout" req
      :auth-rules authenticated?
      :summary "logout the current user."
      (users/logout req)))

  (context "/profiles" []
    (GET "/user/:user-k/profile" []
      :auth-rules authenticated?
      :path-params [user-k :- s/Str]
      :summary "get the profile of the given user"
      (profiles/get-profile user-k))

    (GET "/user/:user-k/stats" []
      :auth-rules authenticated?
      :path-params [user-k :- s/Str]
      :summary "get the stats of the given user"
      (profiles/get-stats user-k))

    (GET "/user/:user-k/activities" []
      :auth-rules authenticated?
      :path-params [user-k :- s/Str]
      :summary "get the activities of the given user"
      (profiles/get-activities user-k))

    (GET "/user/:user-k/rating" []
      :auth-rules authenticated?
      :path-params [user-k :- s/Str]
      :summary "get the rating of the given user"
      (profiles/get-rating user-k)))



  (context "/channels" []
    (GET "/user/current" req
      :auth-rules authenticated?
      :summary "get all the current's user channels"
      (channels/get-channels (:identity req)))

    (POST "/add" req
      :auth-rules authenticated?
      :body-params [locations :- [s/Any]]
      :summary "add a channel for the current user"
      (channels/add-channel locations (:identity req)))

    (POST "/unsubscribe" req
      :auth-rules authenticated?
      :body-params [chan-k :- s/Str]
      :summary "user channel unsubscribe "
      (channels/unsubscribe (:identity req) chan-k)))

  (context "/categories" []
    (GET "/all" req
      :summary "get all categories"
      (categories/get-all)))

  (context "/stories" []
    (GET "/user/:user-key" []
      :auth-rules authenticated?
      :path-params [user-key :- String]
      :summary "get all the stories for the given user"
      (stories/by-user user-key))

    (GET "/channel/:chan-key" []
      :auth-rules authenticated?
      :path-params [chan-key :- String]
      :summary "get all the stories for the given channel"
      (stories/by-channel chan-key))

    (GET "/story/:story-key/get" []
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "get the story by it's key"
      (stories/by-key story-key))

    (GET "/story/:story-key/truth" []
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "get all the users who marked the story as truth"
      (stories/claim-truth story-key))

    (GET "/story/:story-key/lie" []
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "get all the users who marked the story as lie"
      (stories/claim-lie story-key))

    (GET "/story/:story-key/like" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "add a like for the given story by the current user"
      (stories/like story-key (:identity req)))

    (GET "/story/:story-key/dislike" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "add a dislike for the given story by the current user"
      (stories/dislike story-key (:identity req)))

    (GET "/story/:story-key/unlike" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "remove a like for the given story by the current user"
      (stories/unlike story-key (:identity req)))

    (GET "/story/:story-key/undislike" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "remove a dislike for the given story by the current user"
      (stories/undislike story-key (:identity req)))

    (GET "/story/:story-key/mark/truth" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "mark a story as truth"
      (stories/mark-truth story-key (:identity req)))

    (GET "/story/:story-key/mark/lie" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "mark a story as lie"
      (stories/mark-lie story-key (:identity req)))

    (GET "/story/:story-key/unmark/truth" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "unmark a story if it's already marked as truth"
      (stories/unmark-truth story-key (:identity req)))

    (GET "/story/:story-key/unmark/lie" req
      :auth-rules authenticated?
      :path-params [story-key :- String]
      :summary "unmark a story if it's already marked as lie"
      (stories/unmark-lie story-key (:identity req)))

    (POST "/story/new" req
      :auth-rules authenticated?
      :multipart-params [title :- String
                         lat :- Double
                         lng :- Double
                         description :- String
                         images :- s/Any
                         category :- String
                         date :- String
                         time :- String]
      :middleware [wrap-multipart-params]
      :summary "create a new story by the current user"
      (stories/new-story {:title title
                          :mapcords {:lat lat :lng lng}
                          :description description
                          :images (cond (vector? images) images
                                        (= "null" images) nil
                                        :else (vector images))
                          :category category
                          :datetime {:date date :time time}
                          :user (:identity req)})))

  (context "/activities" []
    (GET "/unreads" req
      :auth-rules authenticated?
      (activities/unreads (str "users/" (:identity req))))

    (GET "/all" req
      :auth-rules authenticated?
      (activities/all (str "users/" (:identity req))))

    (GET "/mark/read/:activity-k" req
      :auth-rules authenticated?
      :path-params [activity-k :- String]
      (activities/mark-read (str "users/" (:identity req))
                            (str "activities/" activity-k)))
    (GET "/mark/read-all" req
      :auth-rules authenticated?
      :summary "mark all user's notifications as read"
      (activities/mark-read-all (str "users/" (:identity req))))

    (GET "/last/:user-k/:n" req
      :auth-rules authenticated?
      :path-params [user-k :- s/Str
                    n :- s/Int]
      :summary "get the n latest user activities"
      (activities/get-last (str "users/" user-k) n)))


  (context "/api" []
    :tags ["thingie"]
    (GET "/admin" []
      :auth-rules users/admin?
      :summary "the admin page"
      (ok "Admin Data"))))
