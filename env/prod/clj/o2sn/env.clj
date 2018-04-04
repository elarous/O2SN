(ns o2sn.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[o2sn started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[o2sn has shut down successfully]=-"))
   :middleware identity})
