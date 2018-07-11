(ns o2sn.services.notifications
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [manifold.bus :as bus]
            [cognitect.transit :as transit]
            [o2sn.db.channels :as chans-db]
            [o2sn.services.activities :as activities]
            [o2sn.db.users :as users]
            [o2sn.db.stories :as stories])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def channels (bus/event-bus))

(def non-websocket-request
  {:status 400
   :headers {"Content-Type" "application/text"}
   :body "Expected a websocket request."})

(defn- transit->clj [str]
  (-> str
      bs/to-input-stream
      (transit/reader :json)
      transit/read))

(defn- clj->transit [data]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer data)
    (.toString out)))

(defn- get-channels [activity]
  (let [cause-k  (second (clojure.string/split
                          (:by activity)
                          #"/"))
        users-ks (case (keyword (:type activity))
                   :new-story (activities/new-story-users activity)
                   (:like :dislike) (activities/like-dislike-users activity)
                   (:truth :lie) (activities/truth-lie-users activity))]
    (->> users-ks
         (remove #{cause-k})
         (map #(str "user_" %)))))

(defn- fetch-fields [msg]
  (-> msg
      (update :by
              #(users/get-user
                :key
                (second (clojure.string/split
                         %
                         #"/"))))
      (update :target stories/by-id)))

(defn notifs-handler [req]
  (d/let-flow
   [conn (d/catch
          (http/websocket-connection req)
          (fn [_] nil))]
   (if-not conn
     non-websocket-request
     (do
       (s/connect
        (bus/subscribe channels (str "user_" (:identity req)))
        conn)
       (s/consume
        #(let [msg (transit->clj %)
               activity (-> (assoc msg :by (str "users/" (:identity req)))
                            activities/add-activity)
               transit-activity (-> activity
                                  fetch-fields
                                  clj->transit)]
           (doseq [chan (get-channels activity)]
             (bus/publish!
              channels
              chan
              transit-activity)))
        conn)))))
