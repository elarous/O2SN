(ns o2sn.handler
  (:require [mount.core :as mount]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes wrap-routes]]
            [compojure.route :as route]
            [cheshire.generate :as cheshire]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [muuntaja.core :as muuntaja]
            [muuntaja.format.json :refer [json-format]]
            [muuntaja.format.transit :as transit-format]
            [muuntaja.middleware :refer [wrap-format wrap-params]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.backends :as backends]
            [buddy.auth.backends.session :refer [session-backend]]
            [cognitect.transit :as transit]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [ring.middleware.format-response :refer [wrap-restful-response]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring-ttl-session.core :refer [ttl-memory-store]]
            [o2sn.routes :refer [service-routes]])
  (:import [javax.servlet ServletContext]
            [org.joda.time ReadableInstant]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        {:status 500
         :body "Something very bad has happened!"}))))

(def joda-time-writer
  (transit/write-handler
    (constantly "m")
    (fn [v] (-> ^ReadableInstant v .getMillis))
    (fn [v] (-> ^ReadableInstant v .getMillis .toString))))

(cheshire/add-encoder
  org.joda.time.DateTime
  (fn [c jsonGenerator]
    (.writeString jsonGenerator (-> ^ReadableInstant c .getMillis .toString))))

(def restful-format-options
  (update
    muuntaja/default-options
    :formats
    merge
    {"application/json"
     json-format

     "application/transit+json"
     {:decoder [(partial transit-format/make-transit-decoder :json)]
      :encoder [#(transit-format/make-transit-encoder
                   :json
                   (merge
                     %
                     {:handlers {org.joda.time.DateTime joda-time-writer}}))]}}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format restful-format-options))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))


(def secret "mysecret")

(defn wrap-auth [handler]
  (let [backend (backends/jws {:secret secret})]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defn wrapping-cors [handler]
  (wrap-cors handler
             :access-control-allow-origin #"http://localhost:8080"
             :access-control-allow-methods [:get :put :post :delete]))

(defn debug [handler]
  (fn [resp]
    (log/info "---- INPUT ----")
    (log/info resp)
    (let [resp2 (handler resp)]
      (log/info "---- OUTPUT ----")
      (log/info resp2)
      resp2)))

(defn wrap-base [handler]
  (-> handler
      wrap-auth
      wrap-formats
      wrap-webjars
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)))))



(mount/defstate app
  :start (-> (routes #'service-routes
                     (route/resources "/")
                     (route/not-found "<h1>Not Found</h1>"))
             #_(wrap-resource "public")
             #_wrap-content-type
             #_wrap-not-modified
             wrapping-cors
             wrap-base
             wrap-internal-error
             debug))
