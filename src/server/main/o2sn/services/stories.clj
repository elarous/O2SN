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
            [o2sn.services.activities :as activities]
            [byte-streams :as bs]
            [clojure.string :as str])
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

(defn toggle-like [story-k user-k]
  (let [toggle-map (db/toggle-like! story-k user-k)]
    (when (:liked toggle-map)
      (activities/add-activity {:type :like
                                :target (str "stories/" story-k)
                                :by (str "users/" user-k)}))
    (ok toggle-map)))

(defn toggle-dislike [story-k user-k]
  (let [toggle-map (db/toggle-dislike! story-k user-k)]
    (when (:disliked toggle-map)
      (activities/add-activity {:type :dislike
                                :target (str "stories/" story-k)
                                :by (str "users/" user-k)}))
    (ok toggle-map)))

(defn toggle-truth [story-k user-k]
  (let [toggle-map (db/toggle-truth! story-k user-k)]
    (when (:truth toggle-map)
      (activities/add-activity {:type :truth
                                :target (str "stories/" story-k)
                                :by (str "users/" user-k)}))
    (ok toggle-map)))

(defn toggle-lie [story-k user-k]
  (let [toggle-map (db/toggle-lie! story-k user-k)]
    (when (:lie toggle-map)
      (activities/add-activity {:type :lie
                                :target (str "stories/" story-k)
                                :by (str "users/" user-k)}))
    (ok toggle-map)))

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

        imgs (->> images
                  (map :tempfile)
                  (map encode-img)
                  (db/insert-imgs)
                  (map :_key))
        story (db/create-story {:title title
                                :datetime datetime
                                :category (str "categories/" category)
                                :location (:_id @location)
                                :description description
                                :images imgs})]
    (db/set-owner (:_key story) user)
    (activities/add-activity {:type :new-story
                              :by (str "users/" user)
                              :location (:location story)
                              :target (:_id story)})
    (ok story)))
