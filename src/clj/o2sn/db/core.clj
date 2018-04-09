(ns o2sn.db.core
  (:require [clojure.data.json :as json]
            [clojure.string :refer [starts-with? triml] :as string]
            [clojure.tools.trace :refer :all]
            [clojure.spec.alpha :as sp]
            [clojure.spec.test.alpha :as test]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check :as check]
            [clojure.string :as str])
  (:import [com.arangodb
            ArangoDB
            ArangoDatabase
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

(sp/fdef with-db
           :args (sp/cat :db-name keyword?
                           :body (sp/+ list?)))

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

(sp/fdef with-coll
           :args (sp/cat :coll-name keyword?
                           :body (sp/+ list?)))

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

(sp/fdef with-graph
           :args (sp/cat :graph-name keyword?
                           :body (sp/+ list?)))

(defn ednize
  "convert the given data from json to edn, and if the data is just a string return it untouched."
  [data]
  (if (and (some? data)
           (or (starts-with? (triml data) "{")
               (starts-with? (triml data) "[")))
    (json/read-str data :key-fn keyword)
    data))

(sp/fdef ednize
         :args (sp/cat :data (sp/alt :str string?
                                     :null nil?))
         :ret (sp/or :m map?
                     :v vector?
                     :s string?
                     ))

(defn jsonize
  "convert the given data from edn to json."
  [data]
  (json/write-str data))

(defn json-objs-join [coll]
  (str "["
       (string/join "," coll)
       "]"))

(defn- keys->names
  "convert the keys of the given map from keywords to strings."
  [m]
  (->> m
       (mapcat (fn [pair] [(name (first pair)) (second pair)]))
       (apply hash-map)))

(sp/fdef keys->names
         :args (sp/cat :m (sp/map-of simple-keyword?
                                     (sp/or :string-val string?
                                            :num-val number?)))
         :ret (sp/map-of string? (sp/or :string-val string?
                                        :num-val number?))
         :fn #(and (= (->> % :args :m keys (map name) set)
                      (->> % :ret keys set))
                   (= (->> % :args :m vals set)
                      (->> % :ret vals set))))

(defn set-arango!
  "set the arangodb instance to use, if no value given the needed configuration to create a new instance are loaded from the arangodb.properties file that should reside in the class path."
  ([]
   (set-arango! (.build (ArangoDB$Builder.))))
  ([new-val]
   (alter-var-root #'*arango* (fn [_] new-val))))

(sp/fdef set-arango!
         :args (sp/cat :arango (sp/? #(instance? ArangoDB %)))
         :ret #(instance? ArangoDB %))

(defn set-db!
  "set the database to use with database operations."
  [db]
  (alter-var-root #'*db* (fn [_] (.db *arango* (name db)))))

(sp/fdef set-db!
         :args (sp/cat :arangodb #(instance? ArangoDatabase %))
         :ret #(instance? ArangoDatabase %)
         :fn #(= (-> % :args :arangodb)
                 (-> % :ret)))

;; database functions

(defn create-db!
  "create a new database with the given name. should be called after set-arango!."
  [db]
  (.createDatabase *arango* (name db)))

(sp/fdef create-db!
         :args (sp/cat :db-name simple-keyword?))

(defn drop-db!
  "drop the database with the given name. should be called after set-arango!."
  [db]
  (-> (.db *arango* (name db))
      .drop))

(sp/fdef drop-db!
         :args (sp/cat :db-name simple-keyword?))

(defn db-exists?
  "check whether the database with the given name exists. should be called after set-arango!."
  [db]
  (-> (.db *arango* (name db))
      .exists))

(sp/fdef db-exists?
         :args (sp/cat :db-name simple-keyword?)
         :ret boolean?)

;; collection functions
(defn create-coll!
  "create a new collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> *db*
      (.createCollection (name coll))))

(sp/fdef create-coll!
        :args (sp/cat :coll-name simple-keyword?))

(defn drop-coll!
  "drop the collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> *db*
      (.collection (name coll))
      .drop))

(sp/fdef drop-coll!
         :args (sp/cat :coll-name simple-keyword?))

(defn truncate-coll!
  "truncate the collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> *db*
      (.collection (name coll))
      .truncate))

(sp/fdef truncate-coll!
         :args (sp/cat :coll-name simple-keyword?))

(defn coll-exists?
  "check if a collection exists using it's name. should be called after set-db! or inside with-db."
  [coll]
  (-> *db*
      (.collection (name coll))
      .exists))

(sp/fdef coll-exists!
         :args (sp/cat :coll simple-keyword?)
         :ret boolean?)

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

(sp/fdef get-doc
         :args (sp/cat :coll (sp/? simple-keyword?)
                       :doc simple-keyword?)
         :ret #(or (map? %)
                   (nil? %)))

(defn doc-exists?
  "check whether the doc exists or not using it's key. should be called inside with-coll"
  [doc]
  (-> *coll*
      (.documentExists (name doc))))

(sp/fdef doc-exists?
         :args (sp/cat :doc-name simple-keyword?)
         :ret boolean?)

(defn insert-doc!
  "insert document into the surrounding collection. should be called inside with-coll."
  [doc-map]
  (-> *coll*
      (.insertDocument (jsonize doc-map))))

(sp/fdef insert-doc!
         :args (sp/cat :doc-map map?))

(defn delete-doc!
  "delete document by it's key from the surrounding collection. should be called inside with-coll."
  [doc-key]
  (-> *coll*
      (.deleteDocument (name doc-key))))

(sp/fdef delete-doc!
         :args (sp/cat :doc-key simple-keyword?))

(defn update-doc!
  "update document by it's key inside the surrounding collection. should be called inside with-coll."
  [doc-key updated-map]
  (-> *coll*
      (.updateDocument (name doc-key)
                       (jsonize updated-map))))

(sp/fdef update-doc!
         :args (sp/cat :doc-key simple-keyword?
                       :updated-map map?))

(defn replace-doc!
  "replace document by it's key inside the surrounding collection. should be called inside with.coll."
  [doc-key new-map]
  (-> *coll*
      (.replaceDocument (name doc-key)
                        (jsonize new-map))))

(sp/fdef replace-doc!
         :args (sp/cat :doc-key simple-keyword?
                       :new-map map?))

;; multiple documents functions


(defn get-docs
  "get documents from the surrounding collection. should be called inside with-coll."
  [ks]
  (-> *coll*
      (.getDocuments (map name ks) String)
      .getDocuments
      json-objs-join
      ednize))

(sp/fdef get-docs
         :args (sp/cat :docs-keys (sp/coll-of simple-keyword?))
         :ret (sp/coll-of map?))

(defn insert-docs!
  "insert documents into the surrounding collection. should be called inside with-coll."
  [docs]
  (-> *coll*
      (.insertDocuments (map jsonize docs))))

(sp/fdef insert-docs!
         :args (sp/cat :docs (sp/coll-of map?)))

(defn delete-docs!
  "delete documents by their keys from the surrounding collection. should be called inside with-coll."
  [docs-keys]
  (-> *coll*
      (.deleteDocuments (map name docs-keys))))

(sp/fdef delete-docs!
         :args (sp/cat :docs-keys (sp/coll-of simple-keyword?)))

(defn update-docs!
  "update documents by their keys in the surrounding collection. should be called inside with-coll."
  [docs]
  (-> *coll*
      (.updateDocuments (map jsonize docs))))

(sp/fdef update-docs!
         :args (sp/cat :docs (sp/coll-of map?)))

(defn replace-docs!
  "replace documents by their keys in the surrounding "
  [docs]
  (-> *coll*
      (.replaceDocuments (map jsonize docs))))

(sp/fdef replace-docs!
         :args (sp/cat :docs (sp/coll-of map?)))

;; general aql

(defn- create-seq
  "a helper function with creates a lazy sequence from the cursor after executing a query."
  [cur]
  (when (.hasNext cur)
    (lazy-seq (cons (ednize (.next cur))
                    (create-seq cur)))))

(defn query!
  "execute the given AQL query, it takes an optional bindings map. it should be called after set-db! or inside with-db.
  Exemple :
  (with-db :mydb
    (query \"for p in persons filter p.age >= @age return p\"
            {:age 20}))"
  ([query-str]
   (query! query-str nil))
  ([query-str bindings]
   (let [bind-vars (and bindings
                        (-> bindings
                            keys->names
                            java.util.HashMap.))
         cursor (-> *db*
                    (.query query-str bind-vars nil String))]
     (create-seq cursor))))

(sp/fdef query
         :args (sp/cat :query-str string?
                       :bindings (sp/? map?))
         :ret (sp/nilable (sp/coll-of map?)))

;; Graph Functions
(defn graph-exists?
  "check whether the graph with the given name exists or not. should be called after set-db! or inside with-db."
  [graph]
  (-> *db*
      (.graph (name graph))
      .exists))

(sp/fdef graph-exists?
         :args (sp/cat :graph-name simple-keyword?)
         :ret boolean?)

(defn- edge-def
  "create and EdgeDefinition object using the given map."
  [m]
  (-> (EdgeDefinition.)
      (.collection (name (:edge-coll m)))
      (.from (into-array (map name (:edge-from m))))
      (.to (into-array (map name (:edge-to m))))))

(defn- create-graph-options
  "create the GraphCreateOptions object using the given map."
  [m]
  (-> (GraphCreateOptions.)
      (.orphanCollections (into-array (map name (:orphan-colls m))))))

(defn create-graph!
  "create a graph using the given map. it should be called after set-db! or inside with-db."
  [{:keys [graph-name graph-edges graph-options]}]
  (.createGraph *db*
                (name graph-name)
                (map edge-def graph-edges)
                (create-graph-options graph-options)))

(sp/def ::edge-coll string?)
(sp/def ::edge-from (sp/coll-of keyword?))
(sp/def ::edge-to (sp/coll-of keyword?))

(sp/def ::graph-name string?)
(sp/def ::graph-edges (sp/coll-of
                       (sp/keys :req-un [::edge-coll ::edge-from ::edge-to])))
(sp/def ::graph-options map?)

(sp/def ::create-graph-map
  (sp/keys :req-un [::graph-name ::graph-edges ::graph-options]))

(sp/fdef create-graph!
         :args (sp/cat :m ::create-graph-map))

(defn delete-graph!
  "delete the graph by it's name. it should be called after set-db! or inside with-db."
  [graph]
  (-> *db*
      (.graph (name graph))
      .drop))

(sp/fdef delete-graph!
        :args (sp/cat :graph simple-keyword?))

;; vertex and vertex collection functions

(defn- get-vcoll
  "get vertex collection by name. it should be called inside with-graph."
  [vcoll]
  (-> *graph*
      (.vertexCollection (name vcoll))))

(defn insert-vertex!
  "insert vertex to the vertex collection. it should be called inside with-graph."
  [vcoll vertex]
  (-> (get-vcoll vcoll)
      (.insertVertex (jsonize vertex))))

(sp/fdef insert-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :vertex map?))

(defn delete-vertex!
  "delete vertex by key from vertex collection. it should be called inside with-graph."
  [vcoll k]
  (-> (get-vcoll vcoll)
      (.deleteVertex (name k))))

(sp/fdef delete-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?))

(defn get-vertex
  "get vertex by key from vertex collection. it should be called inside with-graph."
  [vcoll k]
  (-> (get-vcoll vcoll)
      (.getVertex (name k) String)
      ednize))

(sp/fdef get-vertex
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?)
         :ret map?)

(defn replace-vertex!
  "replace vertex by key in the vertex collection. it should be called inside with-graph."
  [vcoll k vertex]
  (-> (get-vcoll vcoll)
      (.replaceVertex (name k) (jsonize vertex))))

(sp/fdef replace-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?
                       :vertex map?))

(defn update-vertex!
  "update vertex by key in the vertex collection. it should be called inside with-graph."
  [vcoll k vertex]
  (-> (get-vcoll vcoll)
      (.updateVertex (name k) (jsonize vertex))))

(sp/fdef update-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?
                       :vertex map?))

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

(sp/fdef insert-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :edge map?))

(defn delete-edge!
  "delete edge by key from edge collection. it should be called inside with-graph."
  [ecoll k]
  (-> (get-ecoll ecoll)
      (.deleteEdge (name k))))

(sp/fdef delete-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?))

(defn get-edge
  "get edge by key from edge collection. it should be called inside with-graph."
  [ecoll k]
  (-> (get-ecoll ecoll)
      (.getEdge (name k) String)
      ednize))

(sp/fdef get-edge
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?)
         :ret map?)

(defn replace-edge!
  "replace edge by key in edge collection. it should be called inside with-graph."
  [ecoll k edge]
  (-> (get-ecoll ecoll)
      (.replaceEdge (name k) (jsonize edge))))

(sp/fdef replace-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?
                       :edge map?))

(defn update-edge!
  "update edge by key in edge collection. it should be called inside with-graph."
  [ecoll k edge]
  (-> (get-ecoll ecoll)
      (.updateEdge (name k) (jsonize edge))))

(sp/fdef update-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?
                       :edge map?))
