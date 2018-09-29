(ns o2sn.notifications.effects
  (:require [re-frame.core :refer [reg-fx dispatch]]
            [o2sn.websockets :as ws]))

(reg-fx
   :notifs/create-ws
   (fn [{:keys [host port user-k]}]
     (ws/make-websocket! (str "ws://" host ":" port "/ws/notifs/" user-k)
                         :notifs
                         #(dispatch [:notifs/receive %]))))

(reg-fx
   :notifs/close
   (fn [_]
     (ws/close! :notifs)))

(reg-fx
   :notifs/send
   (fn [data]
          (ws/send! :notifs data)))



