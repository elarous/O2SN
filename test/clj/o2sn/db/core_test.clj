(ns o2sn.db.core-test (:require [o2sn.db.core :as db]
            [clojure.test :refer :all]
            [clojure.spec.test.alpha :as test]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))


(defn- remove-colls [colls]
  (doseq [c colls]
    (when (db/coll-exists? c)
      (db/drop-coll! c))))

(defn- create-colls [colls]
  (doseq [c colls]
    (when-not (db/coll-exists? c)
      (db/create-coll! c))))

(defn- reset-colls [colls]
  (remove-colls colls)
  (create-colls colls))

(def data [{:_key "1" :name "nur"}
           {:_key "2" :name "nadal"}
           {:_key "3" :name "sami"}])

(def graph-map {:graph-name :mygraph
                :graph-edges [{:edge-coll :ecoll1
                               :edge-from [:coll2 :coll3]
                               :edge-to [:coll4 :coll5]}]
                :graph-options {:orphan-collections (into-array ["coll6"])}})

(declare setup-db)
(declare setup-colls)


(use-fixtures :once setup-db)
(use-fixtures :each setup-colls)

(defn setup-db [f]
  (db/set-arango!)
  (when (db/db-exists? :testdb)
    (db/drop-db! :testdb))
  (db/create-db! :testdb)
  (db/set-db! :testdb)
  (f)
  (db/drop-db! :testdb))

(defn setup-colls [f]
  (reset-colls [:mycoll])
  (when (db/graph-exists? :mygraph)
    (db/delete-graph! :mygraph)) 
  (f)
  (when (db/graph-exists? :mygraph)
    (db/delete-graph! :mygraph))
  (remove-colls [:mycoll :ecoll1 :coll2 :coll3 :coll4 :coll5 :coll6]))

(deftest coll-doesnt-exists
  (testing "check if a non existing collection exists returns false"
    (is (not
         (db/coll-exists? :randomcollname))
        "not created collection should not exists")))

(deftest coll-deleted
  (testing "checking the deletion of a collection"
    (db/drop-coll! :mycoll)
    (is (not (db/coll-exists? :mycoll))
        "collection should not exists after dropping it")))

(deftest coll-exists
  (testing "checking the creation of a collection and it's existence"
    (is (db/coll-exists? :mycoll)
        "already created collection should exists")))

(deftest doc-insert-get
  (testing "inserting and getting a doc"
    (db/with-coll :mycoll
      (let [result (-> (db/insert-doc! {:_key "1" :name "karim"}
                                       {:return-new true}
                                       [:new])
                       :new
                       db/ednize)]
        (is (and (= (:_key result) "1")
                 (= (:name result) "karim"))
            "inserted data should match retrieved data")))))

(deftest doc-get-non-existing
  (testing "return a non existing doc should return nil"
    (db/with-coll :mycoll
      (is (nil? (db/get-doc :random-key))
          "the document should be nil"))))

(deftest doc-exists
  (testing "check if an inserted doc exists"
    (db/with-coll :mycoll
      (db/insert-doc! {:_key "1" :name "something"})
      (is (db/doc-exists? :1)))))

(deftest doc-delete
  (testing "inserting then deleting a doc should delete it from db"
    (db/with-coll :mycoll
      (db/insert-doc! {:_key "1" :name "nada"})
      (db/delete-doc! :1)
      (is (nil? (db/get-doc :1))
          "getting a deleted documents returns nil"))))

(deftest doc-update
  (testing "updating values withing a document"
    (db/with-coll :mycoll
      (db/insert-doc! {:_key "1" :name "ayman"})
      (db/update-doc! :1 {:name "nabil"})
      (is (= (:name (db/get-doc :1)) "nabil")
          "the doc should have the modified name value"))))

(deftest doc-replace
  (testing "replacing a document"
    (db/with-coll :mycoll
      (db/insert-doc! {:_key "1" :name "nadim"})
      (db/replace-doc! :1 {:age 33})
      (let [r (db/get-doc :1)]
        (is (and (not (contains? r :name))
                 (= (:age r) 33))
            "should return the new doc with old keys-values pairs replaced by new ones")))))

(deftest docs-insert
  (testing "inserting and getting multiple documents"
    (db/with-coll :mycoll
      (db/insert-docs! data)
      (let [results (db/get-docs [:1 :2])]
        (are [result doc-map] (and (= (:_key result)
                                      (:_key doc-map))
                                   (= (:name result)
                                      (:name doc-map)))
          (-> results first) (-> data first)
          (-> results second) (-> data second))))))

(deftest docs-delete
  (testing "deleting multiple documents"
    (db/with-coll :mycoll
      (db/insert-docs! data)
      (db/delete-docs! [:1 :3])
      (let [results (db/get-docs [:1 :2 :3])]
        (is (= 1 (count results)) "after deleting 2 docs 1 doc is left")
        (is (and (= (-> results first :_key) "2")
                 (= (-> results first :name) "nadal"))
            "the docs left should stay untouched")))))

(deftest docs-update
  (testing "updating multiple documents"
    (db/with-coll :mycoll
      (let [name->uppercase (fn [m] (update m :name #(string/upper-case %)))
            modified-data (map name->uppercase data)]
        (db/insert-docs! data)
        (db/update-docs! modified-data)
        (let [results (db/get-docs [:1 :2 :3])]
          (are [result doc-map] (and (= (:_key result)
                                        (:_key doc-map))
                                     (= (:name result)
                                        (:name doc-map)))
            (-> results first) (-> modified-data first)
            (-> results second) (-> modified-data second)))))))

(deftest docs-replace
  (testing "replacing multiple documents"
    (db/with-coll :mycoll
      (let [name->age (fn [m] (-> (assoc m :age (* (count (:name m)) 10))
                                  (dissoc :name)))
            modified-data (map name->age data)]
        (db/insert-docs! data)
        (db/replace-docs! modified-data)
        (let [results (db/get-docs [:1 :2 :3])]
          (are [result doc-map] (and (= (:_key result)
                                        (:_key doc-map))
                                     (= (:age result)
                                        (:age doc-map)))
            (-> results first) (-> modified-data first)
            (-> results second) (-> modified-data second)))))))

;; TODO : add test for queries with bindings
(deftest query
  (testing "reading query without bindings"
    (db/with-coll :mycoll
      (db/insert-docs! data))
    (let [query-str "for d in mycoll return d"
          results (db/query! query-str)]
      (is (= (set data)
             (set (map #(select-keys % [:_key :name]) results)))
          "query result should equal all the inserted docs"))))


(deftest graph-creation
  (testing "creating a graph and then verifying it's existence."
    (db/create-graph! graph-map)
    (is (db/graph-exists? :mygraph) "the graph exists?")
    (is (db/coll-exists? :ecoll1) "the edge collection exists?")
    (is (db/coll-exists? :coll2) "from collection exists?")
    (is (db/coll-exists? :coll3) "from collection exists?")
    (is (db/coll-exists? :coll4) "to collection exists?")
    (is (db/coll-exists? :coll5) "to collection exists?")
    (is (db/coll-exists? :coll6) "orphan collection exists?")))

(deftest graph-deletion
  (testing "deletion of a graph"
    (db/create-graph! graph-map)
    (db/delete-graph! :mygraph)
    (is (not (db/graph-exists? :mygraph)) "the graph doesn't exists")
    (is (db/coll-exists? :ecoll1) "the edge collection still exists")
    (is (db/coll-exists? :coll2) "from collection still exists")
    (is (db/coll-exists? :coll3) "from collection still exists")
    (is (db/coll-exists? :coll4) "to collection still exists")
    (is (db/coll-exists? :coll5) "to collection still exists")
    (is (db/coll-exists? :coll6) "orphan collection still exists")))

(deftest vertex-insertion
  (testing "insertion of a new vertex into a vertex collection of the graph"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-vertex! :coll2 {:_key "1" :name "nadim"})
      (db/with-coll :coll2
        (is (db/doc-exists? :1))))))

(deftest vertex-deletion
  (testing "deletion of an already inserted vertex"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-vertex! :coll2 {:_key "1" :name "naoufal"})
      (db/delete-vertex! :coll2 :1)
      (db/with-coll :coll2
        (is (not (db/doc-exists? :1)))))))

(deftest vertex-retrieval
  (testing "retrieving a vertex by key from a vertex collection"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-vertex! :coll2 {:_key "1" :name "camilia"})
      (let [v (db/get-vertex :coll2 :1)]
        (is (= {:_key "1" :name "camilia"}
               (select-keys v [:_key :name])))))))

(deftest vertex-replacing
  (testing "replacing (overriding) a vertex's value with a new value"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-vertex! :coll2 {:_key "1" :name "jason"})
      (let [v {:age 23}
            _ (db/replace-vertex! :coll2 :1 v)
            rv (db/get-vertex :coll2 :1)]
        (is (and (= (:age v) (:age rv))
                 (not (contains? rv :name))))))))

(deftest vertex-updating
  (testing "updating a vertex values"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-vertex! :coll2 {:_key "1" :name "kawtar"})
      (let [v {:name "karawan"}
            _ (db/update-vertex! :coll2 :1 v)
            rv (db/get-vertex :coll2 :1)]
        (is (= (:name rv) (:name v)))))))

(deftest edge-insertion
  (testing "inserting a new edge to the edge collection"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-edge! :ecoll1 {:_key "1" :_from "coll2/1" :_to "coll4/1"})
      (is (db/with-coll :ecoll1
            (db/doc-exists? :1))))))

(deftest edge-deletion
  (testing "deleting an existing edge from an edge collection"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-edge! :ecoll1 {:_key "1" :_from "coll2/1" :_to "coll4/1"})
      (db/delete-edge! :ecoll1 :1)
      (is (not (db/with-coll :ecoll1
                 (db/doc-exists? :1)))))))

(deftest edge-retrieval
  (testing "retrieving an already inserted edge from an edge collection"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (db/insert-edge! :ecoll1 {:_key "1" :_from "coll2/1" :_to "coll4/1"})
      (let [e (db/get-edge :ecoll1 :1)]
        (is (= {:_key "1" :_from "coll2/1" :_to "coll4/1"}
               (select-keys e [:_key :_from :_to])))))))

(deftest edge-replacing
  (testing "replacing an edge value with a another one"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (let [original {:_key "1" :_from "coll2/1" :_to "coll4/1"}
            _ (db/insert-edge! :ecoll1 original)
            new-edge {:_from "coll2/3" :_to "coll4/2" :name "kamal"}
            _ (db/replace-edge! :ecoll1 :1 new-edge)
            r (db/get-edge :ecoll1 :1)]
        (is (= new-edge
               (select-keys r [:_from :_to :name])))))))

(deftest edge-updating
  (testing "updating an edge with new values"
    (db/create-graph! graph-map)
    (db/with-graph :mygraph
      (let [original {:_key "1" :_from "coll2/1" :_to "coll4/1" :name "sami"}
            _ (db/insert-edge! :ecoll1 original)
            updated-vals {:name "samir"}
            _ (db/update-edge! :ecoll1 :1 updated-vals)
            r (db/get-edge :ecoll1 :1)]
        (is (= (merge original updated-vals)
               (select-keys r [:_key :_from :_to :name])))))))
