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
  `(try (binding [*db* (.db *arango* (name db))]
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

(defn- ednize
  "convert the given data from json to edn, and if the data is just a string return it untouched."
  [data]
  (if (or (starts-with? (triml data) "{")
          (starts-with? (triml data) "["))
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

(defn query
  "execute the given AQL query, it takes an optional bindings map. it should be called after set-db! or inside with-db.
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

(sp/fdef query
         :args (sp/cat :query-str string?
                       :bindings (sp/? map?))
         :ret (sp/nilable (sp/coll-of map?)))

;; Graph Functions

(defn- edge-def
  "create and EdgeDefinition object using the given map."
  [m]
  (-> (EdgeDefinition.)
      (.collection (name (:collection m)))
      (.from (into-array (map name (:from m))))
      (.to (into-array (map name (:to m))))))

(defn- create-graph-options
  "create the GraphCreateOptions object using the given map."
  [m]
  (-> (GraphCreateOptions.)
      (.orphanCollections (into-array (map name (:orphan-colls m))))))

(defn create-graph!
  "create a graph using the given map. it should be called after set-db! or inside with-db."
  [{:keys [graph-name graph-edges graph-options]}]
  (.createGraph *db*
                graph-name
                (map edge-def graph-edges)
                (create-graph-options graph-options)))

(sp/def ::graph-name string?)
(sp/def ::graph-edges (sp/coll-of simple-keyword?))
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

;; (defn testing []
;;   (with-db :o2sn
;;     (truncate-coll! :players))

;;   (with-db :o2sn
;;     (with-coll :players
;;       (get-doc :2)))

;;   (with-db :o2sn
;;     (with-coll :players
;;       (insert-doc! {:_key "3" :name "youssef" :age 55})
;;       (insert-doc! {:_key "4" :name "ahmed" :age 78})))

;;   (with-db :o2sn
;;     (with-coll :players
;;       (delete-doc! :1)))

;;   (with-db :o2sn
;;     (with-coll :players
;;       (get-doc :1)))

;;   (with-db :o2sn
;;     (get-doc :players :1))


;;   (with-db :o2sn
;;     (with-coll :players
;;       (update-doc! :3 {:age 17})
;;       (replace-doc! :4 {:goals 15 :team "real madrid"})))

;;   (let [v [{:_key "1" :name "nadal" :age 50}
;;            {:_key "2" :name "katrina" :age 25}
;;            {:_key "3" :name "sara" :age 44}
;;            {:_key "4" :name "katrina" :age 13}
;;            {:_key "5" :name "sara" :age 18}]]
;;     (with-db :o2sn
;;       (with-coll :players
;;         (insert-docs! v))))

;;   (with-db :o2sn
;;     (with-coll :players
;;       (delete-docs! [:7 :8 :9])))

;;   (with-db :o2sn
;;     (with-coll :players
;;       (update-docs! [{:_key "7" :name "samiha"}
;;                      {:_key "8" :name "samia"}])))

;;   (with-db :o2sn
;;     (with-coll :players
;;       (replace-docs! [{:_key "9" :goals 15}
;;                       {:_key "7" :team "fcb"}])))


;;   (with-db :o2sn
;;     (query "for p in players filter p.age >= @age return p.name"
;;            {:age 40}))

;;   (with-db :o2sn
;;     (query "for p in players return {name : p.name, age : p.age}"))

;;   (def ed (edge-def {:collection :coll1
;;                      :from [:coll2 :coll3]
;;                      :to [:coll4 :coll5]}))

;;   (.getCollection ed)
;;   (.getFrom ed)
;;   (.getTo ed)


;;   (def opts {:options {:orphan-colls [:coll6]}})

;;   (def go (graph-options (:options opts)))
;;   (.getOrphanCollections go)

;;   (def m {:name "myGraph"
;;           :edge-defs [{:collection :coll1
;;                        :from [:coll2 :coll3]
;;                        :to [:coll4 :coll5]}]
;;           :options {:orphan-colls [:coll6]}})

;;   (with-db :o2sn
;;     (create-graph! m))

;;   (with-db :o2sn
;;     (delete-graph! "myGraph"))


;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (insert-vertex! :coll2 {:_key "1" :name "soufiane" :age 51})
;;       ))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (delete-vertex! :coll2 :613251)))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (drop-vcoll! :coll3)))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (get-vertex :coll2 :1)))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (replace-vertex! :coll2 :615055
;;                        {:title "le rouge et le noir" :author "stendhal"})))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (update-vertex! :coll2 :1 {:age 20})))


;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (update-edge! :coll1 :1 {:_from "coll2/1" :_to "coll4/3"})))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (replace-edge! :coll1 :1 {:_from "coll2/2" :_to "coll4/3"})))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (get-edge :coll1 :1)))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (insert-vertex! :coll2 {:_key "4" :name "Rabat"})
;;       (insert-vertex! :coll4 {:_key "1" :name "Safi"})
;;       (insert-edge! :coll1
;;                     {:_key "1" :_from "coll2/4" :_to "coll4/1"})
;;       ))

;;   (with-db :o2sn
;;     (with-graph :myGraph
;;       (delete-edge! :coll1 :1)))


;;   (with-db :o2sn
;;     (truncate-coll! :coll2)
;;     (truncate-coll! :coll4)
;;     (truncate-coll! :coll1))

;;   )
