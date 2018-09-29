(ns o2sn.db.core
  (:require [clojure.data.json :as json]
            [clojure.string :refer [starts-with? triml] :as string]
            [clojure.spec.alpha :as sp]
            [clojure.spec.test.alpha :as test]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [mount.core :as m]
            [o2sn.config :as config])
  (:import [com.arangodb
            ArangoDB
            ArangoDatabase
            ArangoDB$Builder
            ArangoDBException]
           [com.arangodb.entity EdgeDefinition
            DocumentCreateEntity
            DocumentUpdateEntity]
           [com.arangodb.model
            GraphCreateOptions
            DocumentCreateOptions
            DocumentDeleteOptions
            DocumentUpdateOptions
            DocumentReplaceOptions
            VertexCreateOptions
            VertexUpdateOptions
            VertexDeleteOptions
            VertexReplaceOptions
            DocumentReadOptions
            EdgeCreateOptions
            EdgeUpdateOptions
            EdgeReplaceOptions
            EdgeDeleteOptions]
           [java.lang String]))


(defn create-arango!
  []
  (if-let [db-conf (:database config/conf)]
    (-> (ArangoDB$Builder.)
        (.host (:host db-conf) (:port db-conf))
        (.user (:user db-conf))
        (.password (:password db-conf))
        .build)
    (println "Cannot get database configuration !")))

(m/defstate arango
  :start (create-arango!)
  :stop (.shutdown arango))

(defn use-database!
  []
  (.db arango (get-in config/conf [:database :dbname])))

(m/defstate db
  :start (use-database!))

(def ^:dynamic *coll* nil)
(def ^:dynamic *graph* nil)

(sp/def ::opts-map (sp/map-of simple-keyword? any?))
(sp/def ::out-ks (sp/coll-of simple-keyword?))
(sp/def ::docs (sp/coll-of simple-keyword?))
(sp/def ::errors (sp/coll-of simple-keyword?))
(sp/def ::out-map (sp/keys :req-un [::docs ::errors]))


(defn- kebab->camel
  "transform a kebab case keyword to a camel case string."
  [k]
  (string/replace (name k) #"-(\w)" #(string/upper-case (last %))))

(defn- keyword->getter
  "transform a keyword to a java getter (with case transformation)."
  [k]
  (kebab->camel (keyword (str "get-" (name k)))))

(defn- resolve-method
  "resolve a java method given a class instance, it's name and args."
  [instance method-name args]
  (try (->  instance
            .getClass
            (.getMethod method-name args))
       (catch NoSuchMethodException e
         (println "No method found " method-name
                  "with args " args
                  "for instance " instance))))


(defn- instance-get
  "invoke the getter indicated by the keyword k (after case transformation) on the given java class instance."
  [instance k]
  (let [getter-name (keyword->getter k)
        getter (resolve-method instance getter-name (make-array java.lang.Class 0))]
    (when getter
      (.invoke getter instance nil))))

(defn- set-opt
  "invoke the function indicated by the keyword k with one argument v on the given java class instance."
  [instance [k v]]
  (let [method-name (kebab->camel k)
        method (resolve-method instance method-name (into-array [(type v)]))]
    (when method
      (.invoke method instance (into-array [v])))))

(defn map->opts
  "invoke methods of the given instance, the map m contains pairs of the method name as keyword (in kebab case) and the argument of that method as value. this currently works with methods with one argument only. (maybe this will be modified later)."
  [instance m]
  (when (and (some? instance) (some? m))
    (loop [result instance
           opts-pairs (seq m)]
      (if (or (empty? opts-pairs) (nil? result))
        result
        (recur (set-opt result (first opts-pairs))
               (rest opts-pairs))))))

(defn entity->map
  "retrieves data from the given instance as a map, ks is a sequence of keys that resulting map will contain, each key is mapped to a java getter method, for exemple the key :user-infos is mapped to getUserInfos."
  [instance ks]
  (when (and (some? instance) (some? ks))
    (loop [result (hash-map)
           left-ks ks]
      (if (empty? left-ks)
        result
        (recur (assoc result (first left-ks)
                      (instance-get instance (first left-ks)))
               (rest left-ks))))))

(defmacro with-db
  "use the given database as the target database for the contained database operations.
  Exemple :
  (with-db :mydb
    (do-this thing)
    (do-other thing))"
  [db & body] `(try (binding [db (.db arango ~(name db))]
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
  `(binding [*coll* (.collection db ~(name coll))]
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
  `(binding [*graph* (.graph db ~(name graph))]
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
                     :s string?))

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
;; database functions

(defn create-db!
  "create a new database with the given name. should be called after set-arango!."
  [db]
  (.createDatabase arango (name db)))

(sp/fdef create-db!
         :args (sp/cat :db-name simple-keyword?))

(defn drop-db!
  "drop the database with the given name. should be called after set-arango!."
  [db]
  (-> (.db arango (name db))
      .drop))

(sp/fdef drop-db!
         :args (sp/cat :db-name simple-keyword?))

(defn db-exists?
  "check whether the database with the given name exists. should be called after set-arango!."
  [db]
  (-> (.db arango (name db))
      .exists))

(sp/fdef db-exists?
         :args (sp/cat :db-name simple-keyword?)
         :ret boolean?)

;; collection functions
(defn create-coll!
  "create a new collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> db
      (.createCollection (name coll))))

(sp/fdef create-coll!
        :args (sp/cat :coll-name simple-keyword?))

(defn drop-coll!
  "drop the collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> db
      (.collection (name coll))
      .drop))

(sp/fdef drop-coll!
         :args (sp/cat :coll-name simple-keyword?))

(defn truncate-coll!
  "truncate the collection with the given name. it should be called after set-db! or inside with-db."
  [coll]
  (-> db
      (.collection (name coll))
      .truncate))

(sp/fdef truncate-coll!
         :args (sp/cat :coll-name simple-keyword?))

(defn coll-exists?
  "check if a collection exists using it's name. should be called after set-db! or inside with-db."
  [coll]
  (-> db
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
   (get-doc doc-key nil))
  ([doc-key opts-map]
   (-> *coll*
       (.getDocument (name doc-key)
                     String
                     (map->opts (DocumentReadOptions.) opts-map))
       ednize))
  ([doc-coll doc-key opts-map]
   (-> db
       (.getDocument (str (name doc-coll) "/" (name doc-key))
                     String
                     (map->opts (DocumentReadOptions.) opts-map))
       ednize)))

(sp/fdef get-doc
         :args (sp/cat :coll (sp/? simple-keyword?)
                       :doc simple-keyword?
                       :opts-map (sp/? ::opts-map))
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
  "insert document into the surrounding collection and return it. should be called inside with-coll."
  ([doc-map]
   (insert-doc! doc-map nil nil))
  ([doc-map opts-map]
   (insert-doc! doc-map opts-map nil))
  ([doc-map opts-map out-ks]
   (-> *coll*
       (.insertDocument (jsonize doc-map)
                        (map->opts (DocumentCreateOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef insert-doc!
         :args (sp/cat :doc-map map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn delete-doc!
  "delete document by it's key from the surrounding collection. should be called inside with-coll."
  ([doc-key]
   (delete-doc! doc-key nil nil))
  ([doc-key opts-map]
   (delete-doc! doc-key opts-map nil))
  ([doc-key opts-map out-ks]
   (-> *coll*
       (.deleteDocument (name doc-key)
                        String
                        (map->opts (DocumentDeleteOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef delete-doc!
         :args (sp/cat :doc-key simple-keyword?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn update-doc!
  "update document by it's key inside the surrounding collection. should be called inside with-coll."
  ([doc-key updated-map]
   (update-doc! doc-key updated-map nil nil))
  ([doc-key updated-map opts-map]
   (update-doc! doc-key updated-map opts-map nil))
  ([doc-key updated-map opts-map out-ks]
   (-> *coll*
       (.updateDocument (name doc-key)
                        (jsonize updated-map)
                        (map->opts (DocumentUpdateOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef update-doc!
         :args (sp/cat :doc-key simple-keyword?
                       :updated-map map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn replace-doc!
  "replace document by it's key inside the surrounding collection. should be called inside with-coll."
  ([doc-key new-map]
   (replace-doc! doc-key new-map nil nil))
  ([doc-key new-map opts-map]
   (replace-doc! doc-key new-map opts-map nil))
  ([doc-key new-map opts-map out-ks]
   (-> *coll*
       (.replaceDocument (name doc-key)
                         (jsonize new-map)
                         (map->opts (DocumentReplaceOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef replace-doc!
         :args (sp/cat :doc-key simple-keyword?
                       :new-map map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

;; multiple documents functions
(defn- multi-results [multi-ents docs errors]
  (when (and (some? docs) (some? errors))
    (hash-map :docs (map #(entity->map % docs) (.getDocuments multi-ents))
              :errors (map #(entity->map % errors) (.getErrors multi-ents)))))

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
  ([documents]
   (insert-docs! documents nil nil))
  ([documents opts-map]
   (insert-doc! documents opts-map nil))
  ([documents opts-map {:keys [docs errors]}]
   (-> *coll*
       (.insertDocuments (map jsonize documents)
                         (map->opts (DocumentCreateOptions.) opts-map))
       (multi-results docs errors))))

(sp/fdef insert-docs!
         :args (sp/cat :documents (sp/coll-of map?)
                       :opts-map (sp/? ::opts-map)
                       :out-map (sp/? ::out-map))
         :ret map?)

(defn delete-docs!
  "delete documents by their keys from the surrounding collection. should be called inside with-coll."
  ([docs-keys]
   (delete-docs! docs-keys nil nil))
  ([docs-keys opts-map]
   (delete-docs! docs-keys opts-map nil))
  ([docs-keys opts-map {:keys [docs errors]}]
   (-> *coll*
       (.deleteDocuments (map name docs-keys)
                         String
                         (map->opts (DocumentDeleteOptions.) opts-map))
       (multi-results docs errors))))

(sp/fdef delete-docs!
         :args (sp/cat :docs-keys (sp/coll-of simple-keyword?)
                       :opts-map (sp/? ::opts-map)
                       :out-map (sp/? ::out-map))
         :ret map?)

(defn update-docs!
  "update documents by their keys in the surrounding collection. should be called inside with-coll."
  ([documents]
   (update-docs! documents nil nil))
  ([documents opts-map]
   (update-docs! opts-map nil))
  ([documents opts-map {:keys [docs errors]}]
   (-> *coll*
       (.updateDocuments (map jsonize documents)
                         (map->opts (DocumentUpdateOptions.) opts-map))
       (multi-results docs errors))))


(sp/fdef update-docs!
         :args (sp/cat :documents (sp/coll-of map?)
                       :opts-map (sp/? ::opts-map)
                       :out-map (sp/? ::out-map))
         :ret map?)

(defn replace-docs!
  "replace documents by their keys in the surrounding "
  ([documents]
   (replace-docs! documents nil nil))
  ([documents opts-map]
   (replace-docs! documents opts-map nil))
  ([documents opts-map {:keys [docs errors]}]
   (-> *coll*
       (.replaceDocuments (map jsonize documents)
                          (map->opts (DocumentReplaceOptions.) opts-map))
       (multi-results docs errors))))

(sp/fdef replace-docs!
         :args (sp/cat :docs (sp/coll-of map?)
                       :opts-map (sp/? ::opts-map)
                       :out-map (sp/? ::out-map))
         :ret map?)

;; general aql

(defn- create-seq
  "a helper function which creates a lazy sequence from the cursor after executing a query."
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
         cursor (-> db
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
  (-> db
      (.graph (name graph))
      .exists))

(sp/fdef graph-exists?
         :args (sp/cat :graph-name simple-keyword?)
         :ret boolean?)

(defn- edge-def
  "create and EdgeDefinition object using the given map."
  [m]
  (map->opts (EdgeDefinition.) {:collection (name (:edge-coll m))
                                :from (into-array (map name (:edge-from m)))
                                :to (into-array (map name (:edge-to m)))}))

(defn create-graph!
  "create a graph using the given map. it should be called after set-db! or inside with-db."
  [{:keys [graph-name graph-edges graph-options]}]
  (.createGraph db
                (name graph-name)
                (map edge-def graph-edges)
                (map->opts (GraphCreateOptions.) graph-options)))

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
  (-> db
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
  ([vcoll vertex]
   (insert-vertex! vcoll vertex nil nil))
  ([vcoll vertex opts-map]
   (insert-vertex! vcoll vertex opts-map nil))
  ([vcoll vertex opts-map out-ks]
   (-> (get-vcoll vcoll)
       (.insertVertex (jsonize vertex)
                      (map->opts (VertexCreateOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef insert-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :vertex map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn delete-vertex!
  "delete vertex by key from vertex collection. it should be called inside with-graph."
  ([vcoll k]
   (delete-vertex! vcoll k nil nil))
  ([vcoll k opts-map]
   (delete-vertex! vcoll k opts-map nil))
  ([vcoll k opts-map out-ks]
   (-> (get-vcoll vcoll)
       (.deleteVertex (name k)
                      (map->opts (VertexDeleteOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef delete-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn get-vertex
  "get vertex by key from vertex collection. it should be called inside with-graph."
  ([vcoll k]
   (get-vertex vcoll k nil))
  ([vcoll k opts-map]
   (-> (get-vcoll vcoll)
       (.getVertex (name k)
                   String
                   (map->opts (DocumentReadOptions.) opts-map))
       ednize)))

(sp/fdef get-vertex
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?
                       :opts-map (sp/? ::opts-map))
         :ret map?)

(defn replace-vertex!
  "replace vertex by key in the vertex collection. it should be called inside with-graph."
  ([vcoll k vertex]
   (replace-vertex! vcoll k vertex nil nil))
  ([vcoll k vertex opts-map]
   (replace-vertex! vcoll k vertex opts-map nil))
  ([vcoll k vertex opts-map out-ks]
   (-> (get-vcoll vcoll)
       (.replaceVertex (name k)
                       (jsonize vertex)
                       (map->opts (VertexReplaceOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef replace-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?
                       :vertex map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn update-vertex!
  "update vertex by key in the vertex collection. it should be called inside with-graph."
  ([vcoll k vertex]
   (update-vertex! vcoll k vertex nil nil))
  ([vcoll k vertex opts-map]
   (update-vertex! vcoll k vertex opts-map nil))
  ([vcoll k vertex opts-map out-ks]
   (-> (get-vcoll vcoll)
       (.updateVertex (name k)
                      (jsonize vertex)
                      (map->opts (VertexUpdateOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef update-vertex!
         :args (sp/cat :vcoll simple-keyword?
                       :k simple-keyword?
                       :vertex map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks)))

;; edge and edge collection functions
(defn- get-ecoll
  "get edge collection by it's name. it should be called inside with-graph."
  [ecoll]
  (-> *graph*
      (.edgeCollection (name ecoll))))

(defn insert-edge!
  "insert edge to edge collection. it should be called inside with-graph."
  ([ecoll edge]
   (insert-edge! ecoll edge nil nil))
  ([ecoll edge opts-map]
   (insert-edge! ecoll edge opts-map nil))
  ([ecoll edge opts-map out-ks]
   (-> (get-ecoll ecoll)
       (.insertEdge (jsonize edge)
                    (map->opts (EdgeCreateOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef insert-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :edge map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn delete-edge!
  "delete edge by key from edge collection. it should be called inside with-graph."
  ([ecoll k]
   (delete-edge! ecoll k nil nil))
  ([ecoll k opts-map]
   (delete-edge! ecoll k opts-map nil))
  ([ecoll k opts-map out-ks]
   (-> (get-ecoll ecoll)
       (.deleteEdge (name k)
                    (map->opts (EdgeDeleteOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef delete-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?
                       :opts-map (sp/? ::opts-map)
                       :out-ka (sp/? ::out-ks))
         :ret map?)

(defn get-edge
  "get edge by key from edge collection. it should be called inside with-graph."
  ([ecoll k]
   (get-edge ecoll k nil))
  ([ecoll k opts-map]
   (-> (get-ecoll ecoll)
       (.getEdge (name k)
                 String
                 (map->opts (DocumentReadOptions.) opts-map))
       ednize)))

(sp/fdef get-edge
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?
                       :opts-map (sp/? ::opts-map))
         :ret map?)

(defn replace-edge!
  "replace edge by key in edge collection. it should be called inside with-graph."
  ([ecoll k edge]
   (replace-edge! ecoll k edge nil nil))
  ([ecoll k edge opts-map]
   (replace-edge! ecoll k edge opts-map nil))
  ([ecoll k edge opts-map out-ks]
   (-> (get-ecoll ecoll)
       (.replaceEdge (name k)
                     (jsonize edge)
                     (map->opts (EdgeReplaceOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef replace-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?
                       :edge map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

(defn update-edge!
  "update edge by key in edge collection. it should be called inside with-graph."
  ([ecoll k edge]
   (update-edge! ecoll k edge nil nil))
  ([ecoll k edge opts-map]
   (update-edge! ecoll k edge opts-map nil))
  ([ecoll k edge opts-map out-ks]
   (-> (get-ecoll ecoll)
       (.updateEdge (name k)
                    (jsonize edge)
                    (map->opts (EdgeUpdateOptions.) opts-map))
       (entity->map out-ks))))

(sp/fdef update-edge!
         :args (sp/cat :ecoll simple-keyword?
                       :k simple-keyword?
                       :edge map?
                       :opts-map (sp/? ::opts-map)
                       :out-ks (sp/? ::out-ks))
         :ret map?)

