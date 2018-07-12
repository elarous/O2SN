(ns o2sn.events.search
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug]]))

(reg-event-fx
 :search/set-value
 (fn [{db :db} [_ v]]
   (if (pos? (count v))
     {:db (-> db
              (assoc-in [:search :value] v)
              (assoc-in [:search :loading?] true))
      :dispatch-n [[:search/find-stories v]
                   [:search/find-users v]]}
     {:db (assoc-in db [:search :value] v)})))

(reg-event-fx
 :search/find-stories
 (fn [{db :db} [_ v]]
   {:http-xhrio {:method :get
                 :uri (str "/search/stories/" v)
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format
                                   {:keywords? true})
                 :on-success [:search/find-stories-success]
                 :on-failure [:search/find-stories-fail]}}))

(reg-event-db
 :search/find-stories-success
 (fn [db [_ stories]]
   (if (seq stories)
     (-> db
         (assoc-in [:search :loading?] false)
         (assoc-in [:search :content :stories]
                   {:name "stories" :results stories}))
     (-> db
         (assoc-in [:search :loading?] false)
         (update-in [:search :content] dissoc :stories)))))

(reg-event-db
 :search/find-stories-fail
 (fn [db [_ resp]]
   (js/console.log resp)
   db))

(reg-event-fx
 :search/find-users
 (fn [{db :db} [_ v]]
   {:http-xhrio {:method :get
                 :uri (str "/search/users/" v)
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format
                                   {:keywords? true})
                 :on-success [:search/find-users-success]
                 :on-failure [:search/find-users-fail]}}))

(reg-event-db
 :search/find-users-success
 (fn [db [_ users]]
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
 (fn [db [_ resp]]
   (js/console.log resp)
   db))

(reg-event-db
 :search/set-content
 (fn [db [_ c]]
   (assoc-in db [:search :content] c)))

(reg-event-db
 :search/set-loading
 (fn [db [_ l]]
   (assoc-in db [:search :loading?] l)))

(reg-event-fx
 :search/view-result
 (fn [{db :db} [_ r]]
   (let [result (js->clj r :keywordize-keys true)]
     {:dispatch (if (= (:type result) "story")
                  [:search/view-story result]
                  [:search/view-user result])})))

(reg-event-fx
 :search/view-story
 (fn [{db :db} [_ story]]
   (js/console.log "View story : " (:title story))
   {:dispatch [:story/set-current (:_key story) true]}))

(reg-event-fx
 :search/view-user
 (fn [{db :db} [_ user]]
   (js/console.log "view user : " (:fullname user))
   {:dispatch-n [[:profile/load-by-user (:_key user)]
                 [:set-active-panel :profile]]}))
