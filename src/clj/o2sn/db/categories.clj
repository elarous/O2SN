(ns o2sn.db.categories
  (:require [o2sn.db.core :as db]))

(defn all []
  (let [q-str "for c in categories return c"]
    (db/query! q-str)))
