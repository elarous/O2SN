(ns o2sn.new-story.events
  (:require [kee-frame.core :refer [reg-event-fx reg-event-db]]
            [re-frame.core :refer [dispatch path]]
            [ajax.core :as ajax]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [o2sn.common.interceptors :refer [server auth]]))


(reg-event-db
 :new-story/clear-errors
 (fn [db _]
   (assoc-in db [:new-story :errors] nil)))

(reg-event-db
 :new-story/set-title
 [(path :new-story :title :value)]
 (fn [_ [new-val]]
   new-val))

(reg-event-fx
 :new-story/validate-title
 (fn [_ [title]]
   {:timeout {:id :new-story-title
              :event [:new-story/validating-title title]
              :time 500}}))

(reg-event-fx
 :new-story/validating-title
 (fn [{db :db} [title]]
   {:validate {:target :title
               :value title
               :on-success (fn []
                             (dispatch [:new-story/title-valid])
                             (dispatch [:new-story/clear-errors]))
               :on-failure #(dispatch [:new-story/title-not-valid %])}
    :db (assoc-in db [:new-story :title :validating?] true)}))

(reg-event-db
 :new-story/title-valid
 [(path :new-story :title)]
 (fn [title _]
   (-> title
       (assoc :valid? true)
       (assoc :validating? false))))

(reg-event-db
 :new-story/title-not-valid
 [(path :new-story :title)]
 (fn [title [err-map]]
   (-> title
       (assoc :valid? false)
       (assoc :validating? false)
       (assoc :error (:title err-map)))))

(reg-event-db
 :new-story/set-desc
 [(path :new-story :description :value)]
 (fn [_ [new-val]]
   new-val))

(reg-event-fx
 :new-story/validate-desc
 (fn [_ [desc]]
   {:timeout {:id :new-story-desc
              :event [:new-story/validating-desc desc]
              :time 500}}))

(reg-event-fx
 :new-story/validating-desc
 (fn [{db :db} [desc]]
   {:validate {:target :description
               :value desc
               :on-success (fn []
                             (dispatch [:new-story/desc-valid])
                             (dispatch [:new-story/clear-errors]))
               :on-failure #(dispatch [:new-story/desc-not-valid %])}
    :db (assoc-in db [:new-story :description :validating?] true)}))

(reg-event-db
 :new-story/desc-valid
 [(path :new-story :description)]
 (fn [desc _]
   (-> desc
       (assoc :valid? true)
       (assoc :validating? false))))

(reg-event-db
 :new-story/desc-not-valid
 [(path :new-story :description)]
 (fn [desc [err-map]]
   (-> desc
       (assoc :valid? false)
       (assoc :validating? false)
       (assoc :error (:description err-map)))))

(reg-event-db
 :new-story/move-marker
 (fn [db [{:keys [lat lng]}]]
   (-> db
       (assoc-in [:new-story :map :lat] lat)
       (assoc-in [:new-story :map :lng] lng))))

(reg-event-db
 :new-story/add-img
 (fn [db [img-infos]]
   (update-in db [:new-story :images] #(conj % img-infos))))

(reg-event-db
 :new-story/remove-img
 (fn [db [fname]]
   (update-in db [:new-story :images]
              (fn [imgs] (remove #(= fname (.-name (:file %)))
                                 imgs)))))

(reg-event-db
 :new-story/set-category
 (fn [db [new-cat]]
   (let [new-key (some #(and (= (:name %) new-cat) (:_key %))
                       (:categories db))]
     (assoc-in db [:new-story :category :value] new-key))))

(reg-event-fx
 :new-story/validate-category
 (fn [_ [category]]
   {:timeout {:id :new-story-category
              :event [:new-story/validating-category category]
              :time 500}}))

(reg-event-fx
 :new-story/validating-category
 (fn [{db :db} [category]]
   {:validate {:target :category
               :value category
               :on-success (fn []
                             (dispatch [:new-story/category-valid])
                             (dispatch [:new-story/clear-errors]))
               :on-failure #(dispatch [:new-story/category-not-valid %])}
    :db (assoc-in db [:new-story :category :validating?] true)}))

(reg-event-db
 :new-story/category-valid
 [(path :new-story :category)]
 (fn [category _]
   (-> category
       (assoc :valid? true)
       (assoc :validating? false))))

(reg-event-db
 :new-story/category-not-valid
 [(path :new-story :category)]
 (fn [category [err-map]]
   (-> category
       (assoc :valid? false)
       (assoc :validating? false)
       (assoc :error (:description err-map)))))

;;

(reg-event-db
 :new-story/set-date
 (fn [db [new-date]]
   (-> db
       (assoc-in [:new-story :datetime :date] new-date)
       (assoc-in [:new-story :errors] nil))))

(reg-event-db
 :new-story/set-time
 (fn [db [new-time]]
   (-> db
    (assoc-in [:new-story :datetime :time] new-time)
    (assoc-in [:new-story :errors] nil))))

(reg-event-fx
 :new-story/validate-all
 (fn [{db :db} _]
   (let [new-story (:new-story db)]
     {:validate {:target :new-story
                 :data {:title (get-in new-story [:title :value])
                        :lat (get-in new-story [:map :lat])
                        :lng (get-in new-story [:map :lng])
                        :date (get-in new-story [:datetime :date])
                        :time (get-in new-story [:datetime :time])
                        :description (get-in new-story [:description :value])
                        :category (get-in new-story [:category :value])}
                 :on-success #(dispatch [:new-story/all-valid])
                 :on-failure #(dispatch [:new-story/all-not-valid %])}})))

(reg-event-fx
 :new-story/all-valid
 (fn [{db :db} _]
   {:db (assoc-in db [:new-story :errors] [])
    :dispatch [:new-story/save]}))

(reg-event-db
 :new-story/all-not-valid
 (fn [db [errors-map]]
   (assoc-in db [:new-story :errors] errors-map)))

(reg-event-fx
 :new-story/save
 [server auth]
 (fn [{db :db} _]
   (let [new-story (:new-story db)
         title (get-in new-story [:title :value])
         lat (get-in new-story [:map :lat])
         lng (get-in new-story [:map :lng])
         desc (get-in new-story [:description :value])
         imgs (map :file (:images new-story))
         category (get-in new-story [:category :value])
         [year month day]
         (->> (clojure.string/split (get-in new-story [:datetime :date])
                                    #"-")
              (map js/parseInt))
         [hour minute]
         (->> (clojure.string/split (get-in new-story [:datetime :time])
                                    #":")
              (map js/parseInt))
         datetime (f/unparse (f/formatters :date-time-no-ms)
                             (t/date-time year month day hour minute))
         form-data (doto (js/FormData.)
                     (.append "title" title)
                     (.append "lat" lat)
                     (.append "lng" lng)
                     (.append "description" desc)
                     (.append "category" category)
                     (.append "datetime" datetime))]
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
                   :on-failure [:new-story/save-fail]}
      :dispatch [:operation/start
                 {:title "Publish A New Story"
                  :sub-title "Publishing Your New Story"
                  :success {:text "Your Story Has Been Published Successfully"
                            :sub-text "your story is public now, so everyone can see it and rate it"
                            :btn-text "Home"
                            :route [:home]}
                  :error {:text "Could Not Publish Your Story !"
                          :sub-text ""
                          :btn-text "Back"
                          :route [:new-story]}}]})))

(reg-event-fx
 :new-story/save-success
 (fn [{db :db} [story]]
   {:notifs/send {:type :new-story
                  :target (:_id story)
                  :location (:location story)}
    :dispatch-n [[:operation/set-progress 100]
                 [:operation/set-state :success]
                 [:new-story/reset]]}))

(reg-event-fx
 :new-story/save-fail
 (fn [{db :db} [resp]]
   {:dispatch-n [[:operation/set-progress 100]
                 [:operation/set-state :error]
                 [:operation/set-error-sub-text (str resp)]]}))

(reg-event-db
 :new-story/data-not-valid
 (fn [db [errors]]
   (assoc-in db [:new-story :errors] errors)))

(reg-event-fx
 :new-story/reset
 (fn [db _]
   {:dispatch [:reset :new-story]}))
