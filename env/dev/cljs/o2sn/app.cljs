(ns ^:figwheel-no-load o2sn.app
  (:require [o2sn.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
