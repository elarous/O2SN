(ns o2sn.events
  (:require [o2sn.db :as db]
            [re-frame.core :refer [dispatch
                                   reg-event-fx
                                   reg-event-db
                                   reg-sub
                                   reg-fx
                                   debug]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [o2sn.validation :as v]))

;; helper functions

(defn get-story [db k]
  (some #(and (= k (:_key %)) %)
        (:stories db)))

(defn get-current-story [db]
  (let [k (get-in db [:story-modal :story])]
    (get-story db k)))

;;effect handlers
(reg-fx
 :validate
 (fn [[target value & args]]
   (case target
     :email (let [f (fn []
                      (v/email-exists?
                       value
                       #(dispatch [:email-valid])
                       #(dispatch [:email-not-valid %])))]
              (v/validate-email
               value
               f
               #(dispatch [:email-not-valid %])))
     :email-ajax (v/email-deliverable?
                  value
                  #(dispatch [:email-valid])
                  #(dispatch [:email-not-valid %]))
     :username (v/validate-username
                value
                #(dispatch [:username-valid (first args)])
                #(dispatch [:username-not-valid % (first args)]))
     :username-ajax (v/username-exists?
                     value
                     #(dispatch [:username-valid (first args)])
                     #(dispatch [:username-not-valid % (first args)]))
     :password (v/validate-password
                value
                #(dispatch [:password-valid (first args)])
                #(dispatch [:password-not-valid % (first args)]))

     :repassword (v/validate-repassword
                  value
                  (first args) ;; the password
                  #(dispatch [:repassword-valid])
                  #(dispatch [:repassword-not-valid %]))
     (js/alert (str "target :  " target " value : " value)))))

;;dispatchers

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :set-active-page
 (fn [{db :db} [_ page]]
   {:db (assoc-in db [:page :hiding?] true)
    :dispatch-later [{:ms (get-in db [:page :duration])
                      :dispatch [:set-page page]}]}))

(reg-event-db
 :set-page
 (fn [db [_ page]]
   (-> db
       (assoc-in [:page :active] page)
       (assoc-in [:page :hiding?] false))))

(reg-event-db
 :toggle-sidebar
 (fn [db [_]]
   (update-in db [:sidebar :visible] not)))

(reg-event-fx
 :load-user-channels
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/channels/user/current"
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-user-channels-success]
                 :on-failure [:load-user-channels-fail]}}))

(reg-event-db
 :load-user-channels-success
 (fn [db [_ resp]]
   (assoc-in db [:user-channels :all] resp)))

(reg-event-db
 :load-user-channels-fail
 (fn [db [_ resp]]
   (assoc-in db [:user-channels :all] [])))

(reg-event-fx
 :set-selected-user-chan
 (fn [{db :db} [_ k]]
   {:db (assoc-in db [:user-channels :selected] k)
    :dispatch [:load-stories k]}))

(reg-event-fx
 :load-stories
 (fn [{db :db} [_ k]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/channel/" k)
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-stories-success]
                 :on-failure [:load-stories-fail]}}))

(reg-event-db
 :load-stories-success
 (fn [db [_ resp]]
   (assoc db :stories resp)))

(reg-event-db
 :load-stories-fail
 (fn [db _]
   (assoc db :stories [])))

(reg-event-db
 :show-story-modal
 (fn [db [_ k]]
   (-> db
       (assoc-in [:story-modal :story] k)
       (assoc-in [:story-modal :visible] true)
       (assoc-in [:story-modal :images :current] 0))))

(reg-event-db
 :hide-story-modal
 (fn [db _]
   (assoc-in db [:story-modal :visible] false)))

(reg-event-db
 :next-story-modal-img
 (fn [db _]
   (let [cnt (-> (get-current-story db) :images count)
         curnt (get-in db [:story-modal :images :current])
         nxt (if (= curnt (dec cnt))
               0
               (inc curnt))]
     (assoc-in db [:story-modal :images :current] nxt))))

(reg-event-db
 :previous-story-modal-img
 (fn [db _]
   (let [cnt (-> (get-current-story db) :images count)
         curnt (get-in db [:story-modal :images :current])
         prv (if (= curnt 0)
               (dec cnt)
               (dec curnt))]
     (assoc-in db [:story-modal :images :current] prv))))

(reg-event-db
 :show-story-users-like-modal
 (fn [db [_ users]]
   (-> db
       (assoc-in [:story-like-modal :users] users)
       (assoc-in [:story-like-modal :visible] true))))

(reg-event-db
 :hide-story-users-like-modal
 (fn [db _]
   (assoc-in db [:story-like-modal :visible] false)))

(reg-event-fx
 :toggle-like-story
 (fn [{db :db} [_ story-k]]
   (let [liked? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                      (:likes (get-story db story-k)))
         like-uri (str "/stories/story/" story-k "/like")
         unlike-uri (str "/stories/story/" story-k "/unlike")]
     {:http-xhrio {:method :get
                   :uri (if liked? unlike-uri like-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-like-story-success story-k (not liked?)]
                   :on-failure [:toggle-like-story-fail]}
      :dispatch [:toggle-dislike-story-success story-k false]})))

(reg-event-db
 :toggle-like-story-success
 (fn [db [_ story-k now-liked?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-liked?
                          (update target :likes
                                  #(conj % (get-in db [:user :current])))
                          (update target :likes
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-like-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'like' of a story" resp)
   db))

(reg-event-fx
 :toggle-dislike-story
 (fn [{db :db} [_ story-k]]
   (let [disliked? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                      (:dislikes (get-story db story-k)))
         dislike-uri (str "/stories/story/" story-k "/dislike")
         undislike-uri (str "/stories/story/" story-k "/undislike")]
     {:http-xhrio {:method :get
                   :uri (if disliked? undislike-uri dislike-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-dislike-story-success story-k (not disliked?)]
                   :on-failure [:toggle-dislike-story-fail]}
      :dispatch [:toggle-like-story-success story-k false]})))

(reg-event-db
 :toggle-dislike-story-success
 (fn [db [_ story-k now-disliked?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-disliked?
                          (update target :dislikes
                                  #(conj % (get-in db [:user :current])))
                          (update target :dislikes
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-dislike-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'dislike' of a story" resp)
   db))

(reg-event-fx
 :toggle-truth-story
 (fn [{db :db} [_ story-k]]
   (let [truth? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                         (:truth (get-story db story-k)))
         truth-uri (str "/stories/story/" story-k "/mark/truth")
         not-truth-uri (str "/stories/story/" story-k "/unmark/truth")]
     {:http-xhrio {:method :get
                   :uri (if truth? not-truth-uri truth-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-truth-story-success story-k (not truth?)]
                   :on-failure [:toggle-truth-story-fail]}
      :dispatch [:toggle-lie-story-success story-k false]})))

(reg-event-db
 :toggle-truth-story-success
 (fn [db [_ story-k now-truth?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-truth?
                          (update target :truth
                                  #(conj % (get-in db [:user :current])))
                          (update target :truth
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-truth-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'truth' of a story" resp)
   db))

(reg-event-fx
 :toggle-lie-story
 (fn [{db :db} [_ story-k]]
   (let [lie? (some #(= (:_key %) (:_key (get-in db [:user :current])))
                      (:lie (get-story db story-k)))
         lie-uri (str "/stories/story/" story-k "/mark/lie")
         not-lie-uri (str "/stories/story/" story-k "/unmark/lie")]
     {:http-xhrio {:method :get
                   :uri (if lie? not-lie-uri lie-uri)
                   :format (ajax/text-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:toggle-lie-story-success story-k (not lie?)]
                   :on-failure [:toggle-lie-story-fail]}
      :dispatch [:toggle-truth-story-success story-k false]})))

(reg-event-db
 :toggle-lie-story-success
 (fn [db [_ story-k now-lie?]]
   (let [stories (:stories db)
         target (get-story db story-k)
         strs-without-target (filterv #(not= (:_key %) story-k) stories)
         updated-target (if now-lie?
                          (update target :lie
                                  #(conj % (get-in db [:user :current])))
                          (update target :lie
                                  (fn [o]
                                    (remove #(= (:_key %)
                                                (:_key (get-in db [:user :current])))
                                            o))))
         updated-stories (conj strs-without-target updated-target)]
     (assoc db :stories updated-stories))))

(reg-event-db
 :toggle-lie-story-fail
 (fn [db [_ resp]]
   (js/console.log "couldn't modify 'lie' of a story" resp)
   db))

(reg-event-db
 :toggle-map-visiblity
 (fn [db _]
   (update-in db [:story-modal :map-visible?] not)))

;;;;
(reg-event-db
 :set-docs
 (fn [db [_ docs]]
   (assoc db :docs docs)))

(reg-event-fx
 :check-authenticated
 (fn [{db :db} [_]]
   {:http-xhrio {:method :get
                 :uri "/user/authenticated"
                 :format (ajax/text-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-success]
                 :on-failure [:login-fail false]}}))

(reg-event-fx
 :login
 (fn [{db :db} [_]]
   {:http-xhrio {:method :post
                 :uri "/user/login"
                 :params {:username (get-in db [:login-form :username :value])
                          :password (get-in db [:login-form :password :value])}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-success]
                 :on-failure [:login-fail true]}
    :db (assoc-in db [:login-form :processing?] true)}))

(reg-event-fx
 :login-success
 (fn [{db :db} [_ resp]]
   (js/console.log resp)
   {:db (-> (assoc-in db [:user :logged-in?] true)
            (assoc-in [:user :current] resp)
            (assoc-in [:login-form :errors?] false)
            (assoc-in [:login-form :processing?] false))
    :dispatch [:set-active-page :home]}))

(reg-event-db
 :login-fail
 (fn [db [_ errors? resp]]
   (-> (assoc-in db [:user :logged-in?] false)
       (assoc-in [:login-form :errors?] errors?)
       (assoc-in [:login-form :processing?] false))))

(reg-event-fx
 :logout
 (fn [{db :db} [_]]
   {:http-xhrio {:method :post
                 :uri "/user/logout"
                 :format (ajax/text-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:logout-success]
                 :on-failure [:logout-fail]}}))

(reg-event-fx
 :logout-success
 (fn [{db :db} _]
   {:db (-> db
            (assoc-in [:user :logged-in?] false))
    :dispatch [:set-active-page :login]}))

(reg-event-db
 :logout-fail
 (fn [db _]
   (-> db
       (assoc-in [:user :logged-in?] true))))

(reg-event-fx
 :get-admin-content
 (fn [{db :db} [_]]
   {:http-xhrio {:method :get
                 :uri "/api/admin"
                 :format (ajax/text-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:set-admin-content]
                 :on-failure [:get-admin-content-failed]}}))

(reg-event-db
 :set-admin-content
 (fn [db [_ resp]]
   (println "RESP : " resp)
   (assoc db :admin-content resp)))

(reg-event-db
 :get-admin-content-failed
 (fn [db [_ resp]]
   (assoc db :admin-content "This page can viewed by admins only.")))

;; login form events
(reg-event-db
 :set-login-username
 (fn [db [_ new-val]]
   (assoc-in db [:login-form :username :value] new-val)))

(reg-event-db
 :set-login-password
 (fn [db [_ new-val]]
   (assoc-in db [:login-form :password :value] new-val)))

(reg-event-fx
 :validate-login-username
 (fn [{db :db} [_ username]]
   {:validate [:username username :login-form]
    :db (assoc-in db [:login-form :username :validating] true)}))

(reg-event-fx
 :validate-login-password
 (fn [{db :db} [_ password]]
   {:validate [:password password :login-form]
    :db (assoc-in db [:login-form :password :validating] true)}))

;; sign up form events
(reg-event-db
 :set-signup-email
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :email :value] new-val)))

(reg-event-db
 :set-signup-username
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :username :value] new-val)))

(reg-event-db
 :set-signup-password
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :password :value] new-val)))

(reg-event-db
 :set-signup-repassword
 (fn [db [_ new-val]]
   (assoc-in db [:signup-form :repassword :value] new-val)))

(reg-event-db
 :set-form-control-activated?
 (fn [db [_ form ctrl]]
   (assoc-in db [form ctrl :activated?] true)))

(reg-event-fx
 :validate-signup-email
 (fn [{db :db} [_ email]]
   {:validate [:email email]
    :db (assoc-in db [:signup-form :email :validating] true)}))

(reg-event-fx
 :validate-signup-email-ajax
 (fn [{db :db} [_ email]]
   {:validate [:email-ajax email]
    :db (assoc-in db [:signup-form :email :validating] true)}))

(reg-event-db
 :email-valid
 (fn [db _]
   (-> db
       (assoc-in [:signup-form :email :valid] true)
       (assoc-in [:signup-form :email :validating] false))))

(reg-event-db
 :email-not-valid
 (fn [db [_ err-map]]
   (-> db
       (assoc-in [:signup-form :email :valid] false)
       (assoc-in [:signup-form :email :error] (:email err-map))
       (assoc-in [:signup-form :email :validating] false))))

(reg-event-fx
 :validate-signup-username
 (fn [{db :db} [_ username]]
   {:validate [:username username :signup-form]
    :db (assoc-in db [:signup-form :username :validating] true)}))

(reg-event-fx
 :validate-signup-username-ajax
 (fn [{db :db} [_ username]]
   {:validate [:username-ajax username :signup-form]
    :db (assoc-in db [:signup-form :username :validating] true)}))

(reg-event-db
 :username-valid
 (fn [db [_ form]]
   (-> db
       (assoc-in [form :username :valid] true)
       (assoc-in [form :username :validating] false))))

(reg-event-db
 :username-not-valid
 (fn [db [_ err-map form]]
   (-> db
       (assoc-in [form :username :valid] false)
       (assoc-in [form :username :error] (:username err-map))
       (assoc-in [form :username :validating] false))))

(reg-event-fx
 :validate-signup-password
 (fn [{db :db} [_ password]]
   {:validate [:password password :signup-form]
    :db (assoc-in db [:signup-form :password :validating] true)}))

(reg-event-db
 :password-valid
 (fn [db [_ form]]
   (-> db
       (assoc-in [form :password :valid] true)
       (assoc-in [form :password :validating] false))))

(reg-event-db
 :password-not-valid
 (fn [db [_ err-map form]]
   (-> db
       (assoc-in [form :password :valid] false)
       (assoc-in [form :password :error] (:password err-map))
       (assoc-in [form :password :validating] false))))

(reg-event-fx
 :validate-signup-repassword
 (fn [{db :db} [_ repassword]]
   {:validate [:repassword repassword (get-in db [:signup-form :password :value])]
    :db (assoc-in db [:signup-form :repassword :validating] true)}))

(reg-event-db
 :repassword-valid
 (fn [db _]
   (-> db
       (assoc-in [:signup-form :repassword :valid] true)
       (assoc-in [:signup-form :repassword :validating] false))))

(reg-event-db
 :repassword-not-valid
 (fn [db [_ err-map]]
   (-> db
       (assoc-in [:signup-form :repassword :valid] false)
       (assoc-in [:signup-form :repassword :error] (:repassword err-map))
       (assoc-in [:signup-form :repassword :validating] false))))

(reg-event-fx
 :signup
 (fn [{db :db} _]
   (let [data (hash-map :email (get-in db [:signup-form :email :value])
                        :username (get-in db [:signup-form :username :value])
                        :password (get-in db [:signup-form :password :value]))]
     {:db (assoc-in db [:signup-form :processing?] true)
      :http-xhrio {:uri "/user/signup"
                   :method :post
                   :params data
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:signup-success]
                   :on-failure [:signup-failure]}})))

(reg-event-db
 :signup-success
 (fn [db _]
   (-> db
       (assoc-in [:signup-form :processing?] false)
       (assoc-in [:signup-form :errors?] false)
       (assoc-in [:signup-form :signed-up?] true))))

(reg-event-db
 :signup-failure
 (fn [db [_ {resp :response}]]
   (println "failed : " resp)
   (-> db
       (assoc-in [:signup-form :processing?] false)
       (assoc-in [:signup-form :errors?] true)
       (assoc-in [:signup-form :error :header] (:title resp))
       (assoc-in [:signup-form :error :msg] (:content resp)))))

;; welcome events
(reg-event-db
 :set-welcome-form
 (fn [db [_ form]]
   (assoc-in db [:welcome :form] form)))

(reg-event-db
 :set-welcome-anim-compl
 (fn [db [_ compl?]]
   (assoc-in db [:welcome :animation-completed?] compl?)))


;;subscriptions

(reg-sub
 :page
 (fn [db _]
   (get-in db [:page :active])))

(reg-sub
 :page-hiding?
 (fn [db _]
   (get-in db [:page :hiding?])))

(reg-sub
 :docs
 (fn [db _]
   (:docs db)))

(reg-sub
 :admin-content
 (fn [db _]
   (:admin-content db)))

(reg-sub
 :user-logged-in
 (fn [db _]
   (get-in db [:user :logged-in?])))

;; welcome page subscriptions
(reg-sub
 :welcome-form
 (fn [db _]
   (get-in db [:welcome :form])))

(reg-sub
 :welcome-animation-completed?
 (fn [db _]
   (get-in db [:welcome :animation-completed?])))

;; login form subscriptions

(reg-sub
 :login-username
 (fn [db _]
   (get-in db [:login-form :username :value])))

(reg-sub
 :login-password
 (fn [db _]
   (get-in db [:login-form :password :value])))

(reg-sub
 :login-errors?
 (fn [db _]
   (get-in db [:login-form :errors?])))

(reg-sub
 :login-processing?
 (fn [db _]
   (get-in db [:login-form :processing?])))

(reg-sub
 :login-username-valid?
 (fn [db _]
   (get-in db [:login-form :username :valid])))

(reg-sub
 :login-username-error
 (fn [db _]
   (get-in db [:login-form :username :error])))

(reg-sub
 :login-username-validating?
 (fn [db _]
   (get-in db [:login-form :username :validating])))

(reg-sub
 :login-password-valid?
 (fn [db _]
   (get-in db [:login-form :password :valid])))

(reg-sub
 :login-password-error
 (fn [db _]
   (get-in db [:login-form :password :error])))

(reg-sub
 :login-password-validating?
 (fn [db _]
   (get-in db [:login-form :password :validating])))

(reg-sub
 :login-button-enabled?
 (fn [db _]
   (and (get-in db [:login-form :username :valid])
        (get-in db [:login-form :password :valid]))))

;; sign up forms subscriptions

(reg-sub
 :signup-email
 (fn [db _]
   (get-in db [:signup-form :email :value])))

(reg-sub
 :signup-username
 (fn [db _]
   (get-in db [:signup-form :username :value])))

(reg-sub
 :signup-password
 (fn [db _]
   (get-in db [:signup-form :password :value])))

(reg-sub
 :signup-repassword
 (fn [db _]
   (get-in db [:signup-form :repassword :value])))

(reg-sub
 :signup-processing?
 (fn [db _]
   (get-in db [:signup-form :processing?])))

(reg-sub
 :signup-errors?
 (fn [db _]
   (get-in db [:signup-form :errors?])))

(reg-sub
 :signup-email-valid?
 (fn [db _]
   (get-in db [:signup-form :email :valid])))

(reg-sub
 :signup-username-valid?
 (fn [db _]
   (get-in db [:signup-form :username :valid])))

(reg-sub
 :signup-password-valid?
 (fn [db _]
   (get-in db [:signup-form :password :valid])))

(reg-sub
 :signup-repassword-valid?
 (fn [db _]
   (get-in db [:signup-form :repassword :valid])))

(reg-sub
 :signup-email-error
 (fn [db _]
   (get-in db [:signup-form :email :error])))

(reg-sub
 :signup-username-error
 (fn [db _]
   (get-in db [:signup-form :username :error])))

(reg-sub
 :signup-password-error
 (fn [db _]
   (get-in db [:signup-form :password :error])))

(reg-sub
 :signup-repassword-error
 (fn [db _]
   (get-in db [:signup-form :repassword :error])))

(reg-sub
 :signup-email-validating?
 (fn [db _]
   (get-in db [:signup-form :email :validating])))

(reg-sub
 :signup-username-validating?
 (fn [db _]
   (get-in db [:signup-form :username :validating])))

(reg-sub
 :signup-password-validating?
 (fn [db _]
   (get-in db [:signup-form :password :validating])))

(reg-sub
 :signup-repassword-validating?
 (fn [db _]
   (get-in db [:signup-form :repassword :validating])))

(reg-sub
 :signup-button-enabled
 (fn [db _]
   (and (get-in db [:signup-form :email :valid])
        (get-in db [:signup-form :username :valid])
        (get-in db [:signup-form :password :valid])
        (get-in db [:signup-form :repassword :valid]))))

(reg-sub
 :signup-error-header
 (fn [db _]
   (get-in db [:signup-form :error :header])))

(reg-sub
 :signup-error-msg
 (fn [db _]
   (get-in db [:signup-form :error :msg])))

(reg-sub
 :signed-up?
 (fn [db _]
   (get-in db [:signup-form :signed-up?])))

(reg-sub
 :form-control-activated?
 (fn [db [_ form ctrl]]
   (get-in db [form ctrl :activated?])))

(reg-sub
 :sidebar-visible
 (fn [db _]
   (get-in db [:sidebar :visible])))

(reg-sub
 :user-channels
 (fn [db _]
   (map #(hash-map :key (:_key %)
                   :value (:_key %)
                   :text (:name %))
        (get-in db [:user-channels :all] db))))

(reg-sub
 :selected-user-channel
 (fn [db _]
   (get-in db [:user-channels :selected])))

(reg-sub
 :stories
 (fn [db _]
   (sort-by :_key (:stories db))))

(reg-sub
 :category-color
 (fn [db [_ cat]]
   (get-in db [:categories cat])))

(reg-sub
 :current-story
 (fn [db _]
   (get-current-story db)))

(reg-sub
 :story-modal-visible?
 (fn [db _]
   (get-in db [:story-modal :visible])))

(reg-sub
 :current-user
 (fn [db _]
   (get-in db [:user :current])))

(reg-sub
 :story-modal-img
 (fn [db _]
   (let [story (get-current-story db)]
     (nth (:images story)
          (get-in db [:story-modal :images :current])))))

(reg-sub
 :story-like-modal-visible
 (fn [db _]
   (get-in db [:story-like-modal :visible])))

(reg-sub
 :story-like-modal-users
 (fn [db _]
   (get-in db [:story-like-modal :users])))

(reg-sub
 :like-story?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:likes (get-story db story-k)))))

(reg-sub
 :dislike-story?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:dislikes (get-story db story-k)))))

(reg-sub
 :marked-story-truth?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:truth (get-story db story-k)))))

(reg-sub
 :marked-story-lie?
 (fn [db [_ story-k]]
   (some #(= (:_key %) (:_key (get-in db [:user :current])))
         (:lie (get-story db story-k)))))

(reg-sub
 :story-map-visible?
 (fn [db _]
   (get-in db [:story-modal :map-visible?])))


