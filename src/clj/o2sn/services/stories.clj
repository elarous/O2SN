(ns o2sn.services.stories
  (:require [o2sn.db.stories :as db]
            [ring.util.http-response :refer :all]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [buddy.core.hash :as h]
            [buddy.core.codecs :as c]
            [ajax.core :as ajax]
            [clojure.data.json :as json]
            [o2sn.db.core :refer [ednize]]
            [o2sn.db.locations :as locations]
            [o2sn.maps :as m])
  (:import java.time.Instant))

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

(defn- generate-filename! [filename]
  (let [ext (last (s/split filename #"\."))
        p1 (-> (Instant/now)
               .getEpochSecond
               str
               h/sha1
               c/bytes->hex)
        p2 (-> (rand-int 1000)
               str
               h/sha1
               c/bytes->hex)]
    (str p1 p2 "." ext)))

(defn- copy-img! [file filename]
  (io/copy file
           (io/file (str "resources/public/img/" filename))))

(defn new-story [{:keys [title mapcords description
                         images category datetime
                         user] :as m}]
  (let [imgs (when (some? images)
               (map #(update % :filename generate-filename!) images))
        location (promise)
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
        story (db/create-story {:title title
                                :datetime datetime
                                :category (str "categories/" category)
                                :location (:_id @location)
                                :description description
                                :images (if (some? imgs)
                                          (map :filename imgs)
                                          [])})]
    (when (some? imgs)
      (doseq [img imgs]
        (copy-img! (:tempfile img) (:filename img))))
    (db/set-owner (:_key story) user)
    (ok story)))
