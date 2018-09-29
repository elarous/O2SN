(ns o2sn.app
  (:require [o2sn.core :as core]))

(defn start! []
  (core/start!))

(defn stop! []
  (core/stop!))

;; starting the app
(start!)
