(ns o2sn.common.db)

(def default-db
  {:server {:host "localhost"
            :port 3000}
   :user {:current nil
          :token nil
          :checking-auth? false}
   :operation {:state :progress
               :title "Operation Title"
               :progress 0.3
               :sub-title "Operation sub title"
               :success {:text "Operation Succeeded"
                         :sub-text "further explanation"
                         :btn-text "Click Me"
                         :route [:home]}
               :error {:text "Operation Failed"
                       :sub-text "further explanation"
                       :btn-text "Click Me"
                       :route [:home]}}
   :login-form {:username {:value ""
                           :valid? true
                           :error "Invalid Username"
                           :validating? false
                           :activated? false}
                :password {:value ""
                           :valid? true
                           :error "Invalid Password"
                           :validating? false
                           :activated? false}
                :errors? false
                :processing? false}
   :signup-form {:email {:value ""
                         :valid true
                         :error "Invalid Email"
                         :validating false
                         :activated? false}
                 :username {:value ""
                            :valid true
                            :error "Invalid Username"
                            :validating false
                            :activated? false}
                 :password {:value ""
                            :valid true
                            :error "Invalid Password"
                            :validating false
                            :activated? false}
                 :repassword {:value ""
                              :valid true
                              :error "Invalid Re Password"
                              :validating false
                              :activated? false}
                 :errors? false
                 :error {:header "Error header"
                         :msg "Error message"}
                 :processing? false
                 :signed-up? false}
   :topbar {:add-menu {:open? false}
            :notifications {:open? false}}
   :sidebar {:visible? false
             :overlay {:hovered-page :home
                       :loading? false
                       :visible? true}}
   :search {:value ""
            :content {:stories
                      {:name "stories"
                       :results []}
                      :users
                      {:name "users"
                       :results []}}
            :loading? false}
   :categories {"Event" "orange"
                "Accident" "blue"
                "Natural Disaster" "green"}
   :home {:channel ""
          :sort-by :date
          :order :desc
          :offset 0
          :count 4
          :can-load? false
          :first-loading? false
          :more-loading? false}
   :stories {:loading?  false}
   :story {:current nil
           :visible false
           :loading? false
           :images {:current 0}
           :map-visible? true}
   :new-story {:title {:value ""
                       :valid? true
                       :error "Invalid Password"
                       :validating? false
                       :activated? false}
               :map {:lat 32.053250726144796
                     :lng -7.407108638525983} ;; el kelaa cords
               :description  {:value ""
                              :valid? true
                              :error "Invalid Password"
                              :validating? false
                              :activated? false}
               :images []
               :category {:value ""
                          :valid? true
                          :error "Select A Category"
                          :validating? false
                          :activated? false}
               :datetime {:date ""
                          :time ""}
               :errors []}
   :channels {:all []
              :color {:country "red"
                      :admin-lvl-1 "blue"
                      :admin-lvl-2 "orange"
                      :locality "green"}
              :confirm-visible? {"1" false
                                 "2" false}
              :locations []
              :location-selected? false}
   :profile {:infos {:avatar ""
                     :fullname ""
                     :username ""
                     :email ""
                     :age 0
                     :gender ""
                     :country ""}
             :stats {:stories 0
                     :truths 0
                     :lies 0
                     :likes 0
                     :dislikes 0}
             :activities []
             :rating {:truths 0
                      :lies 0}
             :loading? {:infos false
                        :stats false
                        :activities false
                        :rating false}}
   :notifications {:all []
                   :unreads []
                   :alerts [{:_key "1"
                             :by {:avatar ""
                                  :name "soufiane"
                                  :_key "3"}
                             :header "New Like"
                             :action "Likes"
                             :target {:title "A Very good story to tell"
                                      :_key "23423"}}
                            {:_key "2"
                             :by {:avatar ""
                                  :name "nadal"
                                  :_key "1"}
                             :header "New Dislike"
                             :action "Dislikes"
                             :target {:title "A Very bad story to tell"
                                      :_key "23423"}}]}})
