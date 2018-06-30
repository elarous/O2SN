(ns o2sn.events.profile
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug
                                   dispatch]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(reg-event-fx
 :profile/load
 (fn [{db :db} _]
   {:dispatch [:profile/load-by-user
               (get-in db [:user :current :_key])]}))

(reg-event-fx
 :profile/load-by-user
 (fn [{db :db} [_ user-k]]
   {:dispatch-n [[:profile/load-profile user-k]
                 [:profile/load-stats user-k]
                 [:profile/load-activities user-k]
                 [:profile/load-rating user-k]]}))

(reg-event-fx
 :profile/load-profile
 (fn [{db :db} [_ user-k]]
   {:http-xhrio {:method :get
                 :uri (str "/profiles/user/"
                           user-k
                           "/profile")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:profile/load-profile-success]
                 :on-failure [:profile/load-profile-failure]}
    :db (assoc-in db [:profile :loading? :infos] true)}))

(reg-event-db
 :profile/load-profile-success
 (fn [db [_ resp]]
   (-> db
       (assoc-in [:profile :infos] resp)
       (assoc-in [:profile :loading? :infos] false))))

(reg-event-db
 :profile/load-profile-failure
 (fn [db [_ resp]]
   (js/console.log resp)
   (assoc-in db [:profile :loading? :infos] false)))

(reg-event-fx
 :profile/load-stats
 (fn [{db :db} [_ user-k]]
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
 (fn [db [_ resp]]
   (-> db
       (assoc-in [:profile :stats] resp)
       (assoc-in [:profile :loading? :stats] false))))

(reg-event-db
 :profile/load-stats-failure
 (fn [db [_ resp]]
   (js/console.log resp)
   db))

(reg-event-fx
 :profile/load-activities
 (fn [{db :db} [_ user-k]]
   {:http-xhrio {:method :get
                 :uri (str "/profiles/user/"
                           user-k
                           "/activities")
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:profile/load-activities-success]
                 :on-failure [:profile/load-activities-failure]}
    :db (assoc-in db [:profile :loading? :activities] true)}))

(reg-event-db
 :profile/load-activities-success
 (fn [db [_ resp]]
   (-> db
       (assoc-in [:profile :activities] resp)
       (assoc-in [:profile :loading? :activities] false))))

(reg-event-db
 :profile/load-activities-failure
 (fn [db [_ resp]]
   (js/console.log resp)
   db))


(reg-event-fx
 :profile/load-rating
 (fn [{db :db} [_ user-k]]
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
 (fn [db [_ resp]]
   (-> db
       (assoc-in [:profile :rating] resp)
       (assoc-in [:profile :loading? :rating] false))))

(reg-event-db
 :profile/load-activities-failure
 (fn [db [_ resp]]
   (js/console.log resp)
   db))



(reg-event-db
 :profile/set-loading
 (fn [db [_ segment new-val]]
   (assoc-in db [:profile :loading? segment] new-val)))
