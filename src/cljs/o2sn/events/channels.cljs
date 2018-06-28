(ns o2sn.events.channels
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug
                                   dispatch]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.maps :as m]))

(reg-fx
 :channels/get-locations
 (fn [cords]
   (m/get-locations cords #(dispatch [:channels/set-locations %]))))

(reg-event-fx
 :channels/delete
 (fn [{db :db} [_ chan-k]]
   {:http-xhrio {:method :post
                 :uri "/channels/unsubscribe"
                 :params {:chan-k chan-k}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:channels/delete-success]
                 :on-failure [:channels/delete-failure]}}))

(reg-event-db
 :channels/delete-success
 (fn [db [_ {chan-k :chan-k}]]
   (update-in db [:channels :all]
              (fn [o] (remove #(= (:_key %) chan-k) o)))))

(reg-event-db
 :channels/delete-failure
 (fn [db [_ resp]]
   (js/console.log resp)
   db))

(reg-event-db
 :channels/confirm-delete
 (fn [db [_ chan-k]]
   (assoc-in db [:channels :confirm-visible? chan-k] true)))

(reg-event-db
 :channels/cancel-delete
 (fn [db [_ chan-k]]
   (assoc-in db [:channels :confirm-visible? chan-k] false)))

(reg-event-fx
 :channels/select-point
 (fn [{db :db} [_ cords]]
   {:channels/get-locations cords
    :dispatch [:channels/set-location-selected true]}))

(reg-event-db
 :channels/set-locations
 (fn [db [_ locations]]
   (assoc-in db [:channels :locations] locations)))

(reg-event-db
 :channels/set-location-selected
 (fn [db [_ new-val]]
   (assoc-in db [:channels :location-selected?] new-val)))

(reg-event-db
 :channels/switch-tab
 (fn [db [_ new-val]]
   (assoc-in db [:channels :active-tab] new-val)))

(reg-event-db
 :channels/switch-named-tab
 (fn [db [_ new-val]]
   (assoc-in db [:channels :active-tab]
             (get-in db [:channels :tabs new-val]))))

(reg-event-db
 :channels/show-saving
 (fn [db _]
   (-> db
       (assoc-in [:channels :saving :visible] true)
       (assoc-in [:channels :saving :progress] 0)
       (assoc-in [:channels :saving :state] :progress)
       (assoc-in [:channels :saving :error-msg] ""))))

(reg-event-db
 :channels/hide-saving
 (fn [db _]
   (-> db
       (assoc-in [:channels :saving :visible] false)
       (assoc-in [:channels :active-tab] 0))))

(reg-event-db
 :channels/set-progress
 (fn [db [_ p]]
   (assoc-in db [:channels :saving :progress] p)))

(reg-event-db
 :channels/set-state
 (fn [db [_ s]]
   (assoc-in db [:channels :saving :state] s)))

(reg-event-db
 :channels/set-error-msg
 (fn [db [_ msg]]
   (assoc-in db [:channels :saving :error-msg] msg)))

(reg-event-fx
 :channels/load
 (fn [{db :db} _]
   (when (empty? (get-in db [:channels :all]))
     {:http-xhrio {:method :get
                   :uri "/channels/user/current"
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:channels/loading-success]
                   :on-failure [:channels/loading-failure]}})))

(reg-event-db
 :channels/loading-success
 (fn [db [_ chans]]
   (assoc-in db [:channels :all]
             (map #(update % :type keyword) chans))))

(reg-event-db
 :channels/loading-failure
 (fn [db [_ resp]]
   (js/alert resp)
   db))

(reg-event-fx
 :channels/create-chan
 (fn [{db :db} [_ t]]
   (let [all-chans (get-in db [:channels :locations])
         prev-chans (take-while #(not= (:type %) t) (reverse all-chans))
         target (some #(and (= (:type %) t) %) all-chans)
         locations (conj (reverse prev-chans) target)]
     {:http-xhrio {:method :post
                   :uri "/channels/add"
                   :params {:locations locations}
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format
                                     {:keywords? true})
                   :on-success [:channels/creation-success]
                   :on-failure [:channels/creation-failure]}
      :dispatch [:channels/show-saving]})))

(reg-event-fx
 :channels/creation-success
 (fn [{db :db} [_ new-chan]]
   {:db (if (some? new-chan)
          (update-in db [:channels :all] conj
                     (update new-chan :type keyword))
          db)
    :dispatch-n [[:channels/set-progress 100]
                 [:channels/set-state :success]]}))

(reg-event-fx
 :channels/creation-failure
 (fn [{db :db} [_ resp]]
   {:dispatch-n [[:channels/set-progress 100]
                 [:channels/set-state :error]
                 [:channels/set-error-msg
                  (get-in resp [:response :error])]]}))

