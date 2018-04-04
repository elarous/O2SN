(ns o2sn.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [o2sn.layout :refer [error-page]]
            [o2sn.routes.home :refer [home-routes]]
            [o2sn.routes.services :refer [service-routes]]
            [o2sn.routes.oauth :refer [oauth-routes]]
            [compojure.route :as route]
            [o2sn.env :refer [defaults]]
            [mount.core :as mount]
            [o2sn.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes
      (-> #'home-routes
          (wrap-routes middleware/wrap-csrf)
          (wrap-routes middleware/wrap-formats))
          #'oauth-routes
          #'service-routes
      (route/not-found
        (:body
          (error-page {:status 404
                       :title "page not found"}))))))
