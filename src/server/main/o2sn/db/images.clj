(ns o2sn.db.images
  (:require [o2sn.db.core :as db]))

(defn by-id [id]
  (-> (db/query! (str "return document(\"" id "\")")) first))
