(ns o2sn.app
  (:require [o2sn.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
