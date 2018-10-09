(ns o2sn.channels.events
  (:require [kee-frame.core :refer [reg-event-fx reg-event-db]]
            [re-frame.core :refer [reg-fx dispatch]]
            [ajax.core :as ajax]
            [o2sn.maps.helpers :as m]
            [o2sn.common.interceptors :refer [server auth]]))

(reg-fx
 :channels/get-locations
 (fn [cords]
   (m/get-locations cords #(dispatch [:channels/set-locations %]))))

(reg-event-fx
 :channels/load
 [server auth]
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
 (fn [db [chans]]
   (let [channels (map #(update % :type keyword) chans)]
     (-> db
         (assoc-in [:channels :all] channels)
         (assoc-in [:home :channel] (-> channels first :_key))))))

(reg-event-db
 :channels/loading-failure
 (fn [db [resp]]
   (js/console.log resp)
   db))

(reg-event-fx
 :channels/delete
 [server auth]
 (fn [{db :db} [chan-k]]
   {:http-xhrio {:method :post
                 :uri "/channels/unsubscribe"
                 :params {:chan-k chan-k}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:channels/delete-success]
                 :on-failure [:channels/delete-failure]}}))

(reg-event-db
 :channels/delete-success
 (fn [db [{chan-k :chan-k}]]
   (update-in db [:channels :all]
              (fn [o] (remove #(= (:_key %) chan-k) o)))))

(reg-event-db
 :channels/delete-failure
 (fn [db [resp]]
   (js/console.log resp)
   db))

(reg-event-db
 :channels/confirm-delete
 (fn [db [chan-k]]
   (assoc-in db [:channels :confirm-visible? chan-k] true)))

(reg-event-db
 :channels/cancel-delete
 (fn [db [chan-k]]
   (assoc-in db [:channels :confirm-visible? chan-k] false)))

(reg-event-fx
 :channels/select-point
 (fn [{db :db} [cords]]
   {:channels/get-locations cords
    :dispatch [:channels/set-location-selected true]}))

(reg-event-db
 :channels/set-locations
 (fn [db [locations]]
   (assoc-in db [:channels :locations] locations)))

(reg-event-db
 :channels/set-location-selected
 (fn [db [new-val]]
   (assoc-in db [:channels :location-selected?] new-val)))

(reg-event-fx
 :channels/create-chan
 [server auth]
 (fn [{db :db} [t]]
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
      :dispatch [:operation/start
                 {:title "Add A Channel"
                  :sub-title "Adding A New Channel"
                  :success {:text "The Channel Has Been Added Successfully"
                            :sub-text "You can now select this channel from the dropdown in the home page"
                            :btn-text "All Channels"
                            :route [:list-channels]}
                  :error {:text "Could Not Add The Channel !"
                          :sub-text ""
                          :btn-text "Back"
                          :route [:add-channel]}}]})))

(reg-event-fx
 :channels/creation-success
 (fn [{db :db} [new-chan]]
   {:db (if (some? new-chan)
          (update-in db [:channels :all] conj
                     (update new-chan :type keyword))
          db)
    :dispatch-n [[:operation/set-progress 100]
                 [:operation/set-state :success]]}))

(reg-event-fx
 :channels/creation-failure
 (fn [{db :db} [resp]]
   {:dispatch-n [[:operation/set-progress 100]
                 [:operation/set-state :error]
                 [:operation/set-error-sub-text
                  (get-in resp [:response :error])]]}))
