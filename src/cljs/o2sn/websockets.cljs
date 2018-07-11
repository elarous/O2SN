(ns o2sn.websockets
  (:require [cognitect.transit :as t]
            [cljs.reader :refer [read-string]]))

;; websockets connections
(defonce conns (atom {:notifs nil}))

(def json-reader (t/reader :json))
(def json-writer (t/writer :json))

(defn receive!
  [update-fn]
  (fn [msg]
    (update-fn
     (->> msg .-data (t/read json-reader)))))

(defn send!
  [conn msg]
  (if-let [ws-chan (get @conns conn)]
    (.send ws-chan (t/write json-writer msg))
    (throw (js/Error. "Websocket connection failed!"))))

(defn make-websocket! [url conn receive-handler]
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onmessage chan) (receive! receive-handler))
      (set! (.-onopen chan) #(js/console.log "Connection established"))
      (swap! conns assoc conn chan))
    (throw (js/Error. "WebSocket connection failed!"))))
