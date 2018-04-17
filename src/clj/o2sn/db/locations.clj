(ns o2sn.db.locations
  (:require [o2sn.db.core :as db]
            [clojure.spec.alpha :as s]
            [clojure.walk :as w]
            [clojure.tools.trace :as t]))

(def graph-def {:graph-name "locations-graph"
                :graph-edges [{:edge-coll :contains
                               :edge-from [:locations]
                               :edge-to [:locations]}]
                :graph-options {:orphan-colls [:coll3]}})

(s/def ::name string?)
(s/def ::type #{:country :admin-lvl-1 :admin-lvl-2 :locality})
(s/def ::location (s/keys :req-un [::name ::type]
                          :opt-un [::contains]))
(s/def ::contains (s/coll-of ::location))

(db/set-arango!)
(db/set-db! :o2sn)

(defn create-locs-graph! []
  (db/create-graph! graph-def))

(defn get-loc [m]
  (let [q-str "for l in locations
               filter l.name == @name
               and l.type == @type
               return l"
        loc-map (-> (update m :type name)
                    (select-keys [:name :type]))]
    (db/with-coll :locations
      (-> (db/query! q-str loc-map)
          first))))

(defn get-contains-rel [parent child]
  (let [q-str "for r in contains
               filter r._from == @parentId
               and r._to == @childId
               return r"
        r-map {:parentId (:_id parent) :childId (:_id child)}]
    (db/with-coll :contains
      (-> (db/query! q-str r-map)
          first))))

(defn loc-exists? [m]
  (some? (get-loc m)))

(defn insert-loc-v! [loc]
  #_(println "inserting ... " (:name loc))
  (if-let [existing-loc (get-loc loc)]
    existing-loc
    (db/with-coll :locations
      (-> (db/insert-doc! (-> (select-keys loc [:name :type])
                              (update :type name))
                          {:return-new true}
                          [:new])
          :new
          db/ednize))))

(defn insert-loc-e! [parent child]
  #_(println "connecting " (:name parent) " and " (:name child))
  (when-not (get-contains-rel parent child)
    (db/with-graph :locations-graph
      (db/insert-edge! :contains
                       {:_from (:_id parent) :_to (:_id child)}))))

(defn insert-locs!
  ([locs]
   (insert-locs! [locs] nil))
  ([locs parent]
   (doseq [l locs]
     (let [inserted-loc (insert-loc-v! l)]
       (when (some? parent)
         (insert-loc-e! parent inserted-loc))
       (when (some? (:contains l))
         (insert-locs! (:contains l) inserted-loc))))))
