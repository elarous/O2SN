(ns o2sn.db.core
  (:require [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure.tools.trace :refer :all])
  (:import [com.arangodb
            ArangoDB
            ArangoDB$Builder
            ArangoDBException]
           [com.arangodb.entity EdgeDefinition]
           [com.arangodb.model GraphCreateOptions]))

(def ^:dynamic *arango* nil)
(def ^:dynamic *db* nil)
(def ^:dynamic *coll* nil)
(def ^:dynamic *graph* nil)

(defmacro with-db
  "use the given database as the target database for the contained database operations.
  Exemple :
  (with-db :mydb
    (do-this thing)
    (do-other thing))"
  [db & body]
  `(try (binding [*db* (.db *arango* ~(name db))]
          ~@body)
        (catch ArangoDBException e#
          (println "Exception using db " ~db " : " (.getMessage e#)))))

(defmacro with-coll
  "use the given collection as the target for the contained collection operations. this macro should be used after calling set-db! or inside a with-db macro.
  Exemple :
  (with-db :mydb
    (with-coll :mycoll
      (do-this)
      (do-that)))"
  [coll & body]
  `(binding [*coll* (.collection *db* ~(name coll))]
     ~@body))

(defmacro with-graph
  "use the given graph as the target for the contained graph operations. this macro should be used after calling set-db! or inside a with-db macro.
  Exemple :
  (with-db :mydb
    (with-graph :mygraph
      (do-this)
      (do-that)))"
  [graph & body]
  `(binding [*graph* (.graph *db* ~(name graph))]
     ~@body))

(defn- ednize
  "convert the given data from json to edn, and if the data is just a string return it untouched."
  [data]
  (if (or (s/starts-with? (s/triml data) "{")
          (s/starts-with? (s/triml data) "["))
    (json/read-str data :key-fn keyword)
    data))

(defn- jsonize
  "convert the given data from edn to json."
  [data]
  (json/write-str data))

(defn- keys->names
  "convert the keys of the given map from keywords to strings."
  [m]
  (->> m
       (mapcat (fn [pair] [(name (first pair)) (second pair)]))
       (apply hash-map)))

(defn set-arango!
  "set the arangodb instance to use, if no value given the needed configuration to create a new instance are loaded from the arangodb.properties file that should reside in the class path."
  ([]
   (set-arango! (.build (ArangoDB$Builder.))))
  ([new-val]
   (alter-var-root #'*arango* (fn [_] new-val))))

(defn set-db!
  "set the database to use with database operations."
  [db]
  (alter-var-root #'*db* (fn [_] (.db *arango* (name db)))))


;; database functions
(defn create-db!
  "create a new database with the given name. should be called after set-arango!."
  [db]
  (.createDatabase *arango* (name db)))

(defn drop-db!
  "drop the database with the given name. should be called after set-arango!."
  [db]
  (-> (.db *arango* (name db))
      .drop))

;; collection functions
(defn create-coll!
  "create a new collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> *db*
      (.createCollection (name coll))))

(defn drop-coll!
  "drop the collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> *db*
      (.collection (name coll))
      .drop))

(defn truncate-coll!
  "truncate the collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> *db*
      (.collection (name coll))
      .truncate))

;; document functions

(defn get-doc
  "get the document with key from the surrounding collection, or any collection given it's name.
  Exemple :
  (with-db :mydb
    (with-coll :mycoll
      (get-doc :mykey)))
  OR :
  (with-db :mydb
   (get-doc :mycoll :mykey))"
  ([doc-key]
   (-> *coll*
       (.getDocument (name doc-key) String)
       ednize))
  ([doc-coll doc-key]
   (-> *db*
       (.getDocument (str (name doc-coll) "/" (name doc-key))
                     String)
       ednize)))

(defn insert-doc!
  "insert document into the surrounding collection. should be called inside with-coll."
  [doc-map]
  (-> *coll*
      (.insertDocument (jsonize doc-map))))

(defn delete-doc!
  "delete document by it's key from the surrounding collection. should be called inside with-coll."
  [doc-key]
  (-> *coll*
      (.deleteDocument (name doc-key))))

(defn update-doc!
  "update document by it's key inside the surrounding collection. should be called inside with-coll."
  [doc-key updated-map]
  (-> *coll*
      (.updateDocument (name doc-key)
                       (jsonize updated-map))))

(defn replace-doc!
  "replace document by it's key inside the surrounding collection. should be called inside with.coll."
  [doc-key new-map]
  (-> *coll*
      (.replaceDocument (name doc-key)
                        (jsonize new-map))))

;; multiple documents functions
(defn insert-docs!
  "insert documents into the surrounding collection. should be called inside with-coll."
  [docs]
  (-> *coll*
      (.insertDocuments (map jsonize docs))))

(defn delete-docs!
  "delete documents by their keys from the surrounding collection. should be called inside with-coll."
  [docs-keys]
  (-> *coll*
      (.deleteDocuments (map name docs-keys))))

(defn update-docs!
  "update documents by their keys in the surrounding collection. should be called inside with-coll."
  [docs]
  (-> *coll*
      (.updateDocuments (map jsonize docs))))

(defn replace-docs!
  "replace documents by their keys in the surrounding "
  [docs]
  (-> *coll*
      (.replaceDocuments (map jsonize docs))))

;; general aql

(defn- create-seq
  "a helper function with creates a lazy sequence from the cursor after executing a query."
  [cur]
  (when (.hasNext cur)
    (lazy-seq (cons (ednize (.next cur))
                    (create-seq cur)))))


(defn query
  "execute the given AQL query, it takes an optional bindings map. it should be calle after set-db! or inside with-db.
  Exemple :
  (with-db :mydb
    (query \"for p in persons filter p.age >= @age return p\"
            {:age 20}))"
  ([query-str]
   (query query-str nil))
  ([query-str bindings]
   (let [bind-vars (and bindings
                        (-> bindings
                            keys->names
                            java.util.HashMap.))
         cursor (-> *db*
                    (.query query-str bind-vars nil String))]
     (create-seq cursor))))

;; Graph Functions

(defn- edge-def
  "create and EdgeDefinition object using the given map."
  [m]
  (-> (EdgeDefinition.)
      (.collection (name (:collection m)))
      (.from (into-array (map name (:from m))))
      (.to (into-array (map name (:to m))))))

(defn- graph-options
  "create the GraphCreateOptions object using the given map."
  [m]
  (-> (GraphCreateOptions.)
      (.orphanCollections (into-array (map name (:orphan-colls m))))))

(defn create-graph!
  "create a graph using the given map. it should be called after set-db! or inside with-db."
  [{:keys [name edge-defs options]}]
  (.createGraph *db*
                name
                (map edge-def edge-defs)
                (graph-options options)))

(defn delete-graph!
  "delete the graph by it's name. it should be called after set-db! or inside with-db."
  [graph]
  (-> *db*
      (.graph (name graph))
      .drop))

;; vertex and vertex collection functions
(defn- get-vcoll
  "get vertex collection by name. it should be called inside with-graph."
  [vcoll]
  (-> *graph*
      (.vertexCollection (name vcoll))))

(defn drop-vcoll!
  "drop vertex collection by name. it should be called inside with-graph."
  [vcoll]
  (-> (get-vcoll vcoll)
      .drop))

(defn insert-vertex!
  "insert vertex to the vertex collection. it should be called inside with-graph."
  [vcoll vertex]
  (-> (get-vcoll vcoll)
      (.insertVertex (jsonize vertex))))

(defn delete-vertex!
  "delete vertex by key from vertex collection. it should be called inside with-graph."
  [vcoll k]
  (-> (get-vcoll vcoll)
      (.deleteVertex (name k))))

(defn get-vertex
  "get vertex by key from vertex collection. it should be called inside with-graph."
  [vcoll k]
  (-> (get-vcoll vcoll)
      (.getVertex (name k) String)
      ednize))

(defn replace-vertex!
  "replace vertex by key in the vertex collection. it should be called inside with-graph."
  [vcoll k vertex]
  (-> (get-vcoll vcoll)
      (.replaceVertex (name k) (jsonize vertex))))

(defn update-vertex!
  "update vertex by key in the vertex collection. it should be called inside with-graph."
  [vcoll k vertex]
  (-> (get-vcoll vcoll)
      (.updateVertex (name k) (jsonize vertex))))

;; edge and edge collection functions
(defn- get-ecoll
  "get edge collection by it's name. it should be called inside with-graph."
  [ecoll]
  (-> *graph*
      (.edgeCollection (name ecoll))))

(defn insert-edge!
  "insert edge to edge collection. it should be called inside with-graph."
  [ecoll edge]
  (-> (get-ecoll ecoll)
      (.insertEdge (jsonize edge))))

(defn delete-edge!
  "delete edge by key from edge collection. it should be called inside with-graph."
  [ecoll k]
  (-> (get-ecoll ecoll)
      (.deleteEdge (name k))))

(defn get-edge
  "get edge by key from edge collection. it should be called inside with-graph."
  [ecoll k]
  (-> (get-ecoll ecoll)
      (.getEdge (name k) String)
      ednize))

(defn replace-edge!
  "replace edge by key in edge collection. it should be called inside with-graph."
  [ecoll k edge]
  (-> (get-ecoll ecoll)
      (.replaceEdge (name k) (jsonize edge))))

(defn update-edge!
  "update edge by key in edge collection. it should be called inside with-graph."
  [ecoll k edge]
  (-> (get-ecoll ecoll)
      (.updateEdge (name k) (jsonize edge))))

(set-arango!)
(set-db! :o2sn)

;; testing

(defn testing []
  (with-db :o2sn
    (truncate-coll! :players))

  (with-db :o2sn
    (with-coll :players
      (get-doc :2)))

  (with-db :o2sn
    (with-coll :players
      (insert-doc! {:_key "3" :name "youssef" :age 55})
      (insert-doc! {:_key "4" :name "ahmed" :age 78})))

  (with-db :o2sn
    (with-coll :players
      (delete-doc! :1)))

  (with-db :o2sn
    (with-coll :players
      (get-doc :1)))

  (with-db :o2sn
    (get-doc :players :1))


  (with-db :o2sn
    (with-coll :players
      (update-doc! :3 {:age 17})
      (replace-doc! :4 {:goals 15 :team "real madrid"})))

  (let [v [{:_key "1" :name "nadal" :age 50}
           {:_key "2" :name "katrina" :age 25}
           {:_key "3" :name "sara" :age 44}
           {:_key "4" :name "katrina" :age 13}
           {:_key "5" :name "sara" :age 18}]]
    (with-db :o2sn
      (with-coll :players
        (insert-docs! v))))

  (with-db :o2sn
    (with-coll :players
      (delete-docs! [:7 :8 :9])))

  (with-db :o2sn
    (with-coll :players
      (update-docs! [{:_key "7" :name "samiha"}
                     {:_key "8" :name "samia"}])))

  (with-db :o2sn
    (with-coll :players
      (replace-docs! [{:_key "9" :goals 15}
                      {:_key "7" :team "fcb"}])))


  (with-db :o2sn
    (query "for p in players filter p.age >= @age return p.name"
           {:age 40}))

  (with-db :o2sn
    (query "for p in players return {name : p.name, age : p.age}"))

  (def ed (edge-def {:collection :coll1
                     :from [:coll2 :coll3]
                     :to [:coll4 :coll5]}))

  (.getCollection ed)
  (.getFrom ed)
  (.getTo ed)


  (def opts {:options {:orphan-colls [:coll6]}})

  (def go (graph-options (:options opts)))
  (.getOrphanCollections go)

  (def m {:name "myGraph"
          :edge-defs [{:collection :coll1
                       :from [:coll2 :coll3]
                       :to [:coll4 :coll5]}]
          :options {:orphan-colls [:coll6]}})

  (with-db :o2sn
    (create-graph! m))

  (with-db :o2sn
    (delete-graph! "myGraph"))


  (with-db :o2sn
    (with-graph :myGraph
      (insert-vertex! :coll2 {:_key "1" :name "soufiane" :age 51})
      ))

  (with-db :o2sn
    (with-graph :myGraph
      (delete-vertex! :coll2 :613251)))

  (with-db :o2sn
    (with-graph :myGraph
      (drop-vcoll! :coll3)))

  (with-db :o2sn
    (with-graph :myGraph
      (get-vertex :coll2 :1)))

  (with-db :o2sn
    (with-graph :myGraph
      (replace-vertex! :coll2 :615055
                       {:title "le rouge et le noir" :author "stendhal"})))

  (with-db :o2sn
    (with-graph :myGraph
      (update-vertex! :coll2 :1 {:age 20})))


  (with-db :o2sn
    (with-graph :myGraph
      (update-edge! :coll1 :1 {:_from "coll2/1" :_to "coll4/3"})))

  (with-db :o2sn
    (with-graph :myGraph
      (replace-edge! :coll1 :1 {:_from "coll2/2" :_to "coll4/3"})))

  (with-db :o2sn
    (with-graph :myGraph
      (get-edge :coll1 :1)))

  (with-db :o2sn
    (with-graph :myGraph
      (insert-vertex! :coll2 {:_key "4" :name "Rabat"})
      (insert-vertex! :coll4 {:_key "1" :name "Safi"})
      (insert-edge! :coll1
                    {:_key "1" :_from "coll2/4" :_to "coll4/1"})
      ))

  (with-db :o2sn
    (with-graph :myGraph
      (delete-edge! :coll1 :1)))


  (with-db :o2sn
    (truncate-coll! :coll2)
    (truncate-coll! :coll4)
    (truncate-coll! :coll1))

  )
