(ns o2sn.db.core
  (:require [clojure.data.json :as json])
  (:import [com.arangodb
            ArangoDB
            ArangoDB$Builder
            ArangoDBException]))

(def ^:dynamic *arango* nil)
(def ^:dynamic *db* nil)
(def ^:dynamic *coll* nil)

(defmacro with-db  [db & body]
  `(try (binding [*db* (.db *arango* ~(name db))]
          ~@body)
        (catch ArangoDBException e#
          (println "Exception using db " ~db " : " (.getMessage e#)))))

(defmacro with-coll  [coll & body]
  `(binding [*coll* (.collection *db* ~(name coll))]
     ~@body))

(defn- ednize [data]
  (json/read-str data :key-fn keyword))

(defn- jsonize [data]
  (json/write-str data))

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
  )
