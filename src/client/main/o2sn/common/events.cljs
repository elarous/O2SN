(ns o2sn.common.events
  (:require [kee-frame.core :refer [reg-event-db reg-event-fx reg-chain]]
            [ajax.core :as ajax]
            [o2sn.common.interceptors :refer [server auth]]
            [o2sn.common.db :refer [default-db]]))

(reg-event-fx
 :navigate
 (fn [{db :db} [& route]]
   {:navigate-to (vec (flatten route))}))

(reg-event-db
 :reset
 (fn [db path]
   (assoc-in db path (get-in default-db path))))

(reg-event-fx
 :common/load-categories
 [server auth]
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/categories/all"
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:common/categories-loaded]}}))

(reg-event-db
 :common/categories-loaded
 (fn [db [resp]]
   (assoc db :categories resp)))

(reg-event-db
 :forms/activate-control
 (fn [db [path-to-ctrl]]
   (assoc-in db (vec (concat path-to-ctrl [:activated?])) true)))
