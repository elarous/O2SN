(ns o2sn.db.users-test
  (:require [o2sn.db.users :as u]
            [o2sn.db.core :as db]
            [clojure.test :refer :all]
            [clojure.set :as st]))

(def user {:username "karim"
           :password "1234"
           :email "karim@karimo.com"})

(defn set-connection [f]
  (db/set-arango!)
  (db/set-db! :testdb)
  (when-not (db/db-exists? :testdb)
    (db/create-db! :testdb))
  (f)
  (when (db/db-exists? :testdb)
    (db/drop-db! :testdb)))

(defn clean-coll [f]
  (when-not (db/coll-exists? :users)
    (u/create-users-coll!))
  (f)
  (when (db/coll-exists? :users)
    (u/drop-users-coll!)))

;; tests

(deftest creating-user
  (testing "creating a new user"
    (let [inserted-user (u/create-user! user)]
      (is (st/subset? (set (keys user))
                      (set (keys inserted-user))))
      (is (= (select-keys inserted-user [:username :password :email])
             user)))))

(deftest getting-user-by-key
  (testing "get an already inserted user by key"
    (let [inserted-user (u/create-user! user)
          retrieved-user (u/get-user (keyword (:_key inserted-user)))]
      (is (= inserted-user retrieved-user)))))

(deftest getting-user-by-credentials
  (testing "get an already inserted user by username and password"
    (let [inserted-user (u/create-user! user)
          retrieved-user (u/get-user "karim" "1234")]
      (is (= inserted-user retrieved-user)))))

(deftest deleting-user
  (testing "deleting an already inserted user"
    (let [inserted-user (u/create-user! user)]
      (u/delete-user! inserted-user)
      (is (nil? (u/get-user (:_key inserted-user)))))))

(deftest updating-user
  (testing "updating an already inserted user"
    (let [inserted-user (u/create-user! user)
          new-val {:username "nada" :password "abc"}]
      (u/update-user! inserted-user new-val)
      (is (= (-> (merge inserted-user new-val)
                 (dissoc :_rev))
             (-> (u/get-user (:_key inserted-user))
                 (dissoc :_rev)))))))


(use-fixtures :once set-connection)
(use-fixtures :each clean-coll)

