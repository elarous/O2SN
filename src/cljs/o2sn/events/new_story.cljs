(ns o2sn.events.new-story
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   debug
                                   dispatch]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.db :as db-ns]))

;; events for the editing phase
(reg-event-db
 :new-story/set-title
 (fn [db [_ new-val]]
   (assoc-in db [:new-story :title] new-val)))

(reg-event-db
 :new-story/move-marker
 (fn [db [_ {:keys [lat lng]}]]
   (-> db
       (assoc-in [:new-story :map :lat] lat)
       (assoc-in [:new-story :map :lng] lng))))

(reg-event-db
 :new-story/set-desc
 (fn [db [_ new-val]]
   (assoc-in db [:new-story :description] new-val)))

(reg-event-db
 :new-story/add-img
 (fn [db [_ img-infos]]
   (update-in db [:new-story :images] #(conj % img-infos))))

(reg-event-db
 :new-story/remove-img
 (fn [db [_ fname]]
   (update-in db [:new-story :images]
              (fn [imgs] (remove #(= fname (.-name (:file %)))
                                 imgs)))))

(reg-event-db
 :new-story/set-category
 (fn [db [_ new-cat]]
   (let [new-key (some #(and (= (:name %) new-cat) (:_key %))
                       (:categories db))]
     (assoc-in db [:new-story :category] new-key))))

(reg-event-db
 :new-story/set-date
 (fn [db [_ new-date]]
   (assoc-in db [:new-story :datetime :date] new-date)))

(reg-event-db
 :new-story/set-time
 (fn [db [_ new-time]]
   (assoc-in db [:new-story :datetime :time] new-time)))

(reg-event-fx
 :new-story/validate
 (fn [{db :db} _]
   {:validate [:new-story (:new-story db)]}))

(reg-event-fx
 :new-story/data-valid
 (fn [{db :db} _]
   (let [new-story (:new-story db)
         title (:title new-story)
         lat (get-in new-story [:map :lat])
         lng (get-in new-story [:map :lng])
         desc (:description new-story)
         imgs (map :file (:images new-story))
         category (:category new-story)
         date (get-in new-story [:datetime :date])
         time (get-in new-story [:datetime :time])
         form-data (doto (js/FormData.)
                     (.append "title" title)
                     (.append "lat" lat)
                     (.append "lng" lng)
                     (.append "description" desc)
                     (.append "category" category)
                     (.append "date" date)
                     (.append "time" time))]
     (if (empty? imgs)
       (.append form-data "images" nil)
       (doseq [img imgs]
         (.append form-data "images" img)))
     {:db (-> db
              (assoc-in [:new-story :errors] nil)
              (assoc-in [:new-story :phase] :saving)
              (assoc-in [:new-story :saving :progress] 0)
              (assoc-in [:new-story :saving :state] :progress))
      :http-xhrio {:method :post
                   :uri "/stories/story/new"
                   :body form-data
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:new-story/save-success]
                   :on-failure [:new-story/save-fail]}})))

(reg-event-db
 :new-story/save-success
 (fn [db [_ story]]
   (-> db
       (assoc-in [:new-story :saving :state] :success)
       (assoc-in [:new-story :saving :progress] 100))))

(reg-event-db
 :new-story/save-fail
 (fn [db [_ resp]]
   (-> db
       (assoc-in [:new-story :saving :state] :error)
       (assoc-in [:new-story :saving :progress] 100)
       (assoc-in [:new-story :saving :message] (str resp)))))

(reg-event-db
 :new-story/data-not-valid
 (fn [db [_ errors]]
   (assoc-in db [:new-story :errors] errors)))

(reg-event-db
 :new-story/reset
 (fn [db _]
   (assoc db :new-story (:new-story db-ns/default-db))))


;; events for the saving phase
(reg-event-db
 :new-story/set-progress
 (fn [db [_ percent]]
   (assoc-in db [:new-story :saving :progress] percent)))

(reg-event-db
 :new-story/set-saving-state
 (fn [db [_ state]]
   (assoc-in db [:new-story :saving :state] state)))

(reg-event-db
 :new-story/set-saving-msg
 (fn [db [_ new-msg]]
   (assoc-in db [:new-story :saving :message] new-msg)))

(reg-event-db
 :new-story/set-phase
 (fn [db [_ phase]]
   (assoc-in db [:new-story :phase] phase)))

