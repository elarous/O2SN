(ns o2sn.services.stories
  (:require [o2sn.db.stories :as db]
            [ring.util.http-response :refer :all]))

(defn by-user [user-key]
  (ok (db/by-user user-key)))

(defn by-channel [chan-key]
  (ok (db/by-channel chan-key)))

(defn claim-truth [story-key]
  (ok (db/saying-truth story-key)))

(defn claim-lie [story-key]
  (ok (db/saying-lie story-key)))

(defn get-owner [story-key]
  (ok (db/owner story-key)))

(defn like [story-k user-k]
  (ok (db/add-like! story-k user-k)))

(defn dislike [story-k user-k]
  (ok (db/add-dislike! story-k user-k)))

(defn unlike [story-k user-k]
  (ok (db/remove-like! story-k user-k)))

(defn undislike [story-k user-k]
  (ok (db/remove-dislike! story-k user-k)))

(defn mark-truth [story-k user-k]
  (ok (db/mark-truth! story-k user-k)))

(defn mark-lie [story-k user-k]
  (ok (db/mark-lie! story-k user-k)))

(defn unmark-truth [story-k user-k]
  (ok (db/unmark-truth! story-k user-k)))

(defn unmark-lie [story-k user-k]
  (ok (db/unmark-lie! story-k user-k)))
