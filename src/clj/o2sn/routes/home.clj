(ns o2sn.routes.home
  (:require [o2sn.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [o2sn.services.notifications :as notifs]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" []
       (-> (home-page)
           (response/header "Content-Type" "text/html")))
  (GET "/ws/notifs" req
       (notifs/notifs-handler req))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))
