(ns o2sn.common.interceptors
  (:require [re-frame.core :refer [->interceptor]]))

(def server
  (->interceptor
   :id :server
   :after (fn [context]
            (if (contains? (:effects context) :http-xhrio)
              (let [s (get-in context [:coeffects :db :server])]
                (update-in context [:effects :http-xhrio :uri]
                           #(str s %)))
              context))))

(def auth
  (->interceptor
   :id :auth
   :after (fn [context]
            (if (contains? (:effects context) :http-xhrio)
              (let [t (get-in context [:coeffects :db :user :token])]
                (assoc-in context [:effects :http-xhrio :headers]
                          {"Authorization" (str "Token " t)}))
              context))))
