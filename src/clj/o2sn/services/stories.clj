(ns o2sn.services.stories
  (:require [o2sn.db.stories :as db]
            [ring.util.http-response :refer :all]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [buddy.core.hash :as h]
            [buddy.core.codecs :as c]
            [ajax.core :as ajax]
            [clojure.data.json :as json]
            [mount.core :as mount]
            [o2sn.db.core :refer [ednize]]
            [o2sn.db.locations :as locations]
            [o2sn.config :as config]
            [o2sn.maps :as m]
            [byte-streams :as bs])
  (:import java.time.Instant
           java.util.Base64))

(defn by-user [user-key]
  (ok (db/by-user user-key)))

(defn by-channel [chan-key]
  (ok (db/by-channel chan-key)))

(defn by-key [story-key]
  (ok (db/by-key story-key)))

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

(defn- encode-img [file]
  (->> (bs/to-byte-array file)
       (.encodeToString (Base64/getEncoder))))

(defn new-story [{:keys [title mapcords description
                         images category datetime
                         user] :as m}]
  (let [location (promise)
        _ (m/get-locations
           mapcords
           (fn [data]
             (deliver location
                      (-> data
                          (conj {:lat (:lat mapcords)
                                 :lng (:lng mapcords)
                                 :type "point"
                                 :name (str "point_" (:lat mapcords) "_" (:lng mapcords))})
                          reverse
                          locations/insert-locs-seq!))))
        imgs (map encode-img (map :tempfile images))
        story (db/create-story {:title title
                                :datetime datetime
                                :category (str "categories/" category)
                                :location (:_id @location)
                                :description description
                                :images imgs})]
    (db/set-owner (:_key story) user)
    (ok story)))
