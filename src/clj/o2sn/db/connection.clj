(ns o2sn.db.connection
  (:require [mount.core :as m]
            [o2sn.config :as config])
  (:import [com.arangodb ArangoDB$Builder]))

