(ns o2sn.services.notifications
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [manifold.bus :as bus]
            [cognitect.transit :as transit]
            [o2sn.db.channels :as chans-db]
            [o2sn.db.users :as users]
            [o2sn.db.stories :as stories]
            [ring.util.http-response :refer :all])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def users-chans (bus/event-bus))


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

(defn- fetch-fields [msg]
  (-> msg
      (update :by
              #(users/get-user
                :key
                (second (clojure.string/split
                         %
                         #"/"))))
      (update :target stories/by-id)))


(defn notify [notif users]
  (println "Notify : " users)
  (println "Message : " notif)
  (doseq [user-chan (map #(str "user_" %) users)]
    (bus/publish! users-chans user-chan (-> notif
                                            fetch-fields
                                            clj->transit))))

(defn notifs-handler [req user-k]
  (d/let-flow
   [conn (d/catch
          (http/websocket-connection req)
          (fn [_] nil))]
   (if-not conn
     non-websocket-request
     (do
       (s/connect
        (bus/subscribe users-chans (str "user_" user-k))
        conn)
       (s/consume
        #(println "Websocket Msg received :" %)
        conn)
       (ok "Notications WebSocket Connected")))))
