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

(defmacro with-db  [db & body]
  `(try (binding [*db* (.db *arango* ~(name db))]
          ~@body)
        (catch ArangoDBException e#
          (println "Exception using db " ~db " : " (.getMessage e#)))))

(defmacro with-coll  [coll & body]
  `(binding [*coll* (.collection *db* ~(name coll))]
     ~@body))

(defmacro with-graph  [graph & body]
  `(binding [*graph* (.graph *db* ~(name graph))]
     ~@body))

(defn- ednize [data]
  (if (or (s/starts-with? (s/triml data) "{")
          (s/starts-with? (s/triml data) "["))
    (json/read-str data :key-fn keyword)
    data))

(defn- jsonize [data]
  (json/write-str data))

(defn- keys->names [m]
  (->> m
       (mapcat (fn [pair] [(name (first pair)) (second pair)]))
       (apply hash-map)))

;; TODO : change this so it's return the arangodb object, and create
;; a macro like with-db named with-arango or something like that
(defn set-arango!
  ([]
   (set-arango! (.build (ArangoDB$Builder.))))
  ([new-val]
   (alter-var-root #'*arango* (fn [_] new-val))))

(set-arango!)

;; database functions
(defn create-db! [db]
  (.createDatabase *arango* (name db)))

(defn drop-db! [db]
  (-> (.db *arango* (name db))
      .drop))

;; collection functions
(defn create-coll! [coll]
  (-> *db*
      (.createCollection (name coll))))

(defn drop-coll! [coll]
  (-> *db*
      (.collection (name coll))
      .drop))

(defn truncate-coll! [coll]
  (-> *db*
      (.collection (name coll))
      .truncate))

;; document functions

(defn get-doc
  ([doc-key]
   (-> *coll*
       (.getDocument (name doc-key) String)
       ednize))
  ([doc-coll doc-key]
   (-> *db*
       (.getDocument (str (name doc-coll) "/" (name doc-key))
                     String)
       ednize)))

(defn insert-doc! [doc-map]
  (-> *coll*
      (.insertDocument (jsonize doc-map))))

(defn delete-doc! [doc-key]
  (-> *coll*
      (.deleteDocument (name doc-key))))

(defn update-doc! [doc-key updated-map]
  (-> *coll*
      (.updateDocument (name doc-key)
                       (jsonize updated-map))))

(defn replace-doc! [doc-key new-map]
  (-> *coll*
      (.replaceDocument (name doc-key)
                        (jsonize new-map))))

;; multiple documents functions
(defn insert-docs! [docs]
  (-> *coll*
      (.insertDocuments (map jsonize docs))))

(defn delete-docs! [docs-keys]
  (-> *coll*
      (.deleteDocuments (map name docs-keys))))

(defn update-docs! [docs]
  (-> *coll*
      (.updateDocuments (map jsonize docs))))

(defn replace-docs! [docs]
  (-> *coll*
      (.replaceDocuments (map jsonize docs))))

;; general aql

(defn- create-seq [cur]
  (when (.hasNext cur)
    (lazy-seq (cons (ednize (.next cur))
                    (create-seq cur)))))


(defn query
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

(defn- edge-def [m]
  (-> (EdgeDefinition.)
      (.collection (name (:collection m)))
      (.from (into-array (map name (:from m))))
      (.to (into-array (map name (:to m))))))

(defn- graph-options [m]
  (-> (GraphCreateOptions.)
      (.orphanCollections (into-array (map name (:orphan-colls m))))))

(defn create-graph! [{:keys [name edge-defs options]}]
  (.createGraph *db*
                name
                (map edge-def edge-defs)
                (graph-options options)))

(defn delete-graph! [graph]
  (-> *db*
      (.graph (name graph))
      .drop))

;; vertex and vertex collection functions
(defn- get-vcoll [vcoll]
  (-> *graph*
      (.vertexCollection (name vcoll))))

(defn drop-vcoll! [vcoll]
  (-> (get-vcoll vcoll)
      .drop))

(defn insert-vertex! [vcoll vertex]
  (-> (get-vcoll vcoll)
      (.insertVertex (jsonize vertex))))

(defn delete-vertex! [vcoll k]
  (-> (get-vcoll vcoll)
      (.deleteVertex (name k))))

(defn get-vertex [vcoll k]
  (-> (get-vcoll vcoll)
      (.getVertex (name k) String)
      ednize))

(defn replace-vertex! [vcoll k vertex]
  (-> (get-vcoll vcoll)
      (.replaceVertex (name k) (jsonize vertex))))

(defn update-vertex! [vcoll k vertex]
  (-> (get-vcoll vcoll)
      (.updateVertex (name k) (jsonize vertex))))

;; edge and edge collection functions
(defn- get-ecoll [ecoll]
  (-> *graph*
      (.edgeCollection (name ecoll))))

(defn insert-edge! [ecoll edge]
  (-> (get-ecoll ecoll)
      (.insertEdge (jsonize edge))))

(defn delete-edge! [ecoll k]
  (-> (get-ecoll ecoll)
      (.deleteEdge (name k))))

(defn get-edge [ecoll k]
  (-> (get-ecoll ecoll)
      (.getEdge (name k) String)
      ednize))

(defn replace-edge! [ecoll k edge]
  (-> (get-ecoll ecoll)
      (.replaceEdge (name k) (jsonize edge))))

(defn update-edge! [ecoll k edge]
  (-> (get-ecoll ecoll)
      (.updateEdge (name k) (jsonize edge))))

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
