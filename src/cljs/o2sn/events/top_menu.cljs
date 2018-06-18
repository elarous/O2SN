(ns o2sn.events.top-menu
  (:require [o2sn.db :as db]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(reg-event-db
 :toggle-sidebar
 (fn [db [_]]
   (update-in db [:sidebar :visible] not)))
