(ns o2sn.services.activities
  (:require [o2sn.db.activities :as db]
            [o2sn.db.channels :as chans]
            [o2sn.db.users :as users]
            [o2sn.db.stories :as stories]
            [ring.util.http-response :refer :all]))

(defn new-story-users [activity]
  (->> (:channels activity)
       (map #(chans/subscribers (:_id %)))
       flatten
       (filter some?)
       set
       (map :_key)))

(defn like-dislike-users [activity]
  (-> (stories/owner (-> (:target activity)
                         (clojure.string/split #"/")
                         second))
      :_key
      vector))

(defn truth-lie-users [activity]
  (let [story-k (-> (:target activity)
                    (clojure.string/split #"/")
                    second)
        to-keys #(map :_key %)
        users-truth (-> (stories/saying-truth story-k) to-keys)
        users-lie (-> (stories/saying-lie story-k) to-keys)
        users-like (-> (stories/liking (str "stories/" story-k)) to-keys)
        users-dislike (-> (stories/disliking (str "stories/" story-k)) to-keys)]
    (set
     (concat users-truth users-lie users-like users-dislike))))


(defmulti add-activity (fn [m] (case (:type m)
                                 :new-story :new-story
                                 :like :like-dislike
                                 :dislike :like-dislike
                                 :truth :truth-lie
                                 :lie :truth-lie)))

(defmethod add-activity :new-story
  [m]
  (let [chans (chans/all-by-loc (:location m))
        activity (-> m
                     (assoc :channels chans)
                     db/create-activity)
        users (new-story-users activity)]
    (db/relevant-to-all (:_key activity) users)
    activity))

(defmethod add-activity :like-dislike
  [m]
  (let [activity (db/create-activity m)
        users (like-dislike-users activity)]
    (db/relevant-to (:_key activity) (first users))
    activity))

(defmethod add-activity :truth-lie
  [m]
  (let [activity (db/create-activity m)
        users-ks (truth-lie-users activity)]
    (db/relevant-to-all (:_key activity) users-ks)
    activity))

(defn unreads [user-id]
  (let [actvs (db/get-unreads user-id)]
    (ok (if (seq actvs)
          actvs
          []))))

(defn all [user-id]
  (ok (db/get-all user-id)))

(defn mark-read [user-id activity-id]
  (ok {:marked (db/mark-read user-id activity-id)}))

(defn mark-read-all [user-id]
  (ok {:marked (db/mark-read-all user-id)}))

(defn get-last [user-id n]
  (ok (db/get-last user-id n)))
