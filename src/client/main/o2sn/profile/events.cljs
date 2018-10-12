(ns o2sn.profile.events
  (:require [kee-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [re-frame.core :refer [reg-fx dispatch]]
            [ajax.core :as ajax]
            [o2sn.common.interceptors :refer [server auth]]))

(reg-event-fx
 :profile/load
 (fn [{db :db} _]
   {:dispatch [:profile/load-by-user
               (get-in db [:user :current :_key])]}))

(reg-event-fx
 :profile/load-by-user
 (fn [{db :db} [user-k]]
   {:dispatch-n [[:profile/load-profile user-k]
                 [:profile/load-stats user-k]
                 [:profile/load-activities user-k]
                 [:profile/load-rating user-k]]}))

(reg-event-fx
 :profile/load-profile
 [server auth]
 (fn [{db :db} [user-k]]
   {:http-xhrio {:method :get
                 :uri (str "/profiles/user/"
                           user-k
                           "/profile")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:profile/load-profile-success]
                 :on-failure [:profile/load-profile-failure]}
    :db (assoc-in db [:profile :loading? :infos] true)}))

(reg-event-fx
 :profile/load-profile-success
 (fn [{db :db} [resp]]
   {:db (-> db
            (assoc-in [:profile :infos] resp)
            (assoc-in [:profile :loading? :infos] false))
    :dispatch-later [{:ms 200
                      :dispatch [:sidebar/stop-loading]}]}))

(reg-event-db
 :profile/load-profile-failure
 (fn [db [resp]]
   (assoc-in db [:profile :loading? :infos] false)))

(reg-event-fx
 :profile/load-stats
 [server auth]
 (fn [{db :db} [user-k]]
   {:http-xhrio {:method :get
                 :uri (str "/profiles/user/"
                           user-k
                           "/stats")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:profile/load-stats-success]
                 :on-failure [:profile/load-stats-failure]}
    :db (assoc-in db [:profile :loading? :stats] true)}))

(reg-event-db
 :profile/load-stats-success
 (fn [db [resp]]
   (-> db
       (assoc-in [:profile :stats] resp)
       (assoc-in [:profile :loading? :stats] false))))

(reg-event-db
 :profile/load-stats-failure
 (fn [db [resp]]
   (js/console.log resp)
   db))

(reg-event-fx
 :profile/load-activities
 [server auth]
 (fn [{db :db} [user-k]]
   {:http-xhrio {:method :get
                 :uri (str "/activities/last/" user-k "/5")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:profile/load-activities-success]
                 :on-failure [:profile/load-activities-failure]}
    :db (assoc-in db [:profile :loading? :activities] true)}))

(reg-event-db
 :profile/load-activities-success
 (fn [db [resp]]
   (-> db
       (assoc-in [:profile :activities] resp)
       (assoc-in [:profile :loading? :activities] false))))

(reg-event-db
 :profile/load-activities-failure
 (fn [db [resp]]
   (-> db
       (assoc-in [:profile :activities] [])
       (assoc-in [:profile :loading? :activities] false))))

(reg-event-fx
 :profile/load-rating
 [server auth]
 (fn [{db :db} [user-k]]
   {:http-xhrio {:method :get
                 :uri (str "/profiles/user/"
                           user-k
                           "/rating")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:profile/load-rating-success]
                 :on-failure [:profile/load-rating-failure]}
    :db (assoc-in db [:profile :loading? :rating] true)}))

(reg-event-db
 :profile/load-rating-success
 (fn [db [resp]]
   (-> db
       (assoc-in [:profile :rating] resp)
       (assoc-in [:profile :loading? :rating] false))))



(reg-event-db
 :profile/set-loading
 (fn [db [segment new-val]]
   (assoc-in db [:profile :loading? segment] new-val)))
