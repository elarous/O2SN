(ns o2sn.topbar.events
  (:require [kee-frame.core :refer [reg-event-fx
                                    reg-event-db]]
            [o2sn.common.interceptors :refer [server auth]]
            [ajax.core :as ajax]))

(reg-event-db
 :topbar/toggle-sidebar
 (fn [db _]
   (update-in db [:sidebar :visible?] not)))

(reg-event-db
 :topbar/open-add
 (fn [db _]
   (assoc-in db [:topbar :add-menu :open?] true)))

(reg-event-db
 :topbar/close-add
 (fn [db _]
   (assoc-in db [:topbar :add-menu :open?] false)))

;; search events

(reg-event-fx
 :search/set-value
 (fn [{db :db} [v]]
   (if (pos? (count v))
     {:db (-> db
              (assoc-in [:search :value] v)
              (assoc-in [:search :loading?] true))
      :timeout-n {:id :search
                  :events [[:search/find-stories v]
                           [:search/find-users v]]
                  :time 500}}
     {:db (assoc-in db [:search :value] v)})))

(reg-event-fx
 :search/find-stories
 [server auth]
 (fn [{db :db} [v]]
   (if (>= (count v) 3)
     {:http-xhrio {:method :get
                   :uri (str "/search/stories/" v)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format
                                     {:keywords? true})
                   :on-success [:search/find-stories-success]
                   :on-failure [:search/find-stories-fail]}}
     {:db (-> db
              (assoc-in [:search :loading?] false)
              (update-in [:search :content] dissoc :stories))})))

(reg-event-db
 :search/find-stories-success
 (fn [db [stories]]
   (if (seq stories)
     (-> db
         (assoc-in [:search :loading?] false)
         (assoc-in [:search :content :stories]
                   {:name "stories"
                    :results (->> stories
                                  (map #(assoc % :key (:_key %))))}))
     (-> db
         (assoc-in [:search :loading?] false)
         (update-in [:search :content] dissoc :stories)))))

(reg-event-db
 :search/find-stories-fail
 (fn [db [resp]]
   (js/console.log resp)
   db))

(reg-event-fx
 :search/find-users
 [server auth]
 (fn [{db :db} [v]]
   (if (>= (count v) 3)
     {:http-xhrio {:method :get
                   :uri (str "/search/users/" v)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format
                                     {:keywords? true})
                   :on-success [:search/find-users-success]
                   :on-failure [:search/find-users-fail]}}
     {:db (-> db
              (assoc-in [:search :loading?] false)
              (update-in [:search :content] dissoc :users))})))

(reg-event-db
 :search/find-users-success
 (fn [db [users]]
   (if (seq users)
     (-> db
         (assoc-in [:search :loading?] false)
         (assoc-in [:search :content :users]
                   {:name "users" :results users}))
     (-> db
         (assoc-in [:search :loading?] false)
         (update-in [:search :content] dissoc :users)))))

(reg-event-db
 :search/find-users-fail
 (fn [db [resp]]
   (js/console.log resp)
   db))

(reg-event-db
 :search/set-content
 (fn [db [c]]
   (assoc-in db [:search :content] c)))

(reg-event-db
 :search/set-loading
 (fn [db [l]]
   (assoc-in db [:search :loading?] l)))

(reg-event-fx
 :search/view-result
 (fn [{db :db} [r]]
   (let [result (js->clj r :keywordize-keys true)]
     {:dispatch (if (= (:type result) "story")
                  [:search/view-story result]
                  [:search/view-user result])})))

(reg-event-fx
 :search/view-story
 (fn [{db :db} [story]]
   {:dispatch [:navigate :view-story :story (:_key story)]}))

(reg-event-fx
 :search/view-user
 (fn [{db :db} [user]]
   {:dispatch [:navigate :profile :user (:_key user)]}))
