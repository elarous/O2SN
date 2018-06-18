(ns o2sn.db.locations-test
  (:require [o2sn.db.locations :as l]
            [clojure.test :refer :all]
            [o2sn.db.core :as db]))

(declare setup-db)
(declare clear)


(use-fixtures :once setup-db)
(use-fixtures :each clear)

(defn- remove-colls [colls]
  (doseq [c colls]
    (when (db/coll-exists? c)
      (db/drop-coll! c))))

(defn setup-db [f]
  (db/set-arango!)
  (when (db/db-exists? :testdb)
    (db/drop-db! :testdb))
  (db/create-db! :testdb)
  (db/set-db! :testdb)
  (f)
  (db/drop-db! :testdb))

(defn clear [f]
  (if (db/graph-exists? :locations-graph)
    (do
      (db/delete-graph! :locations-graph)
      (remove-colls [:locations :contains :coll3]))
    (l/create-locs-graph!))
  (f)
  (when (db/graph-exists? :locations-graph)
    (db/delete-graph! :locations-graph)
    (remove-colls [:locations :contains :coll3])))


(def locations-def-ex
  {:name "Morocco"
   :type :country
   :contains [{:name "Marrakesh-Safi"
               :type :admin-lvl-1
               :contains [{:name "El Kelaa Des Sraghna"
                           :type :admin-lvl-2
                           :contains [{:name "El Kelaa Des Sraghna"
                                       :type :locality}]}
                          {:name "Errhamna"
                           :type :admin-lvl-2
                           :contains [{:name "Ben Guerir"
                                       :type :locality}]}]}
              {:name "Rabat-Sale"
               :type :admin-lvl-1
               :contains [{:name "Rabat"
                           :type :admin-lvl-2
                           :contains [{:name "Rabat"
                                       :type :locality}]}
                          {:name "Sale"
                           :type :admin-lvl-2}]}]})


(deftest locs-insertion
  (testing "inserting locations to an empty graph"
    (l/insert-locs! locations-def-ex)
    (is true))
  (testing "inserting locations to a non empty graph"
    (l/insert-locs! locations-def-ex)
    (l/insert-locs! {:name "Morocco"
                   :type :country
                   :contains [{:name "Grande-Casa-Blanca"
                               :type :admin-lvl-1
                               :contains [{:name "Casa Blanca"
                                           :type :admin-lvl-2
                                           :contains [{:name "Settat"
                                                       :type :locality}
                                                      {:name "Casa Blanca"
                                                       :type :locality}]}]}]})
    (is true))
  (testing "insert a location multiple times should not duplicate it"
    (let [p (l/insert-loc-v! {:name "Figuig" :type :admin-lvl-2})
          c (l/insert-loc-v! {:name "Beni Tajjit" :type :locality})]
      (l/insert-loc-e! p c))))
