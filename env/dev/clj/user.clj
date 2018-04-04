(ns user
  (:require 
            [mount.core :as mount]
            [o2sn.figwheel :refer [start-fw stop-fw cljs]]
            [o2sn.core :refer [start-app]]))

(defn start []
  (mount/start-without #'o2sn.core/repl-server))

(defn stop []
  (mount/stop-except #'o2sn.core/repl-server))

(defn restart []
  (stop)
  (start))


