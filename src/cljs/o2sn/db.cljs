(ns o2sn.db
  (:require [o2sn.views.home :as home]
            [o2sn.views.login :as login]
            [o2sn.views.signup :as signup]
            [o2sn.views.new-story :as new-story]
            [o2sn.views.channels :as channels]))

(def default-db
  {:checking-auth? false
   :page {:active :login
          :duration 500
          :hiding? false}
   :pages {:home {:with-menu? true
                  :require-login? true
                  :page #'home/home-page}
           :login {:with-menu? false
                   :require-login? false
                   :page #'login/login-form}
           :signup {:with-menu? false
                    :require-login? false
                    :page #'signup/signup-form}}
   :panel {:active :home
           :duration 500
           :hiding? false}
   :panels {:home #'home/home-main
            :messages #'home/messages-panel
            :new-story #'new-story/new-story-panel
            :channels #'channels/channels-panel}

   :sidebar {:visible true}
   :user {:logged-in? false
          :current nil}
   :selected-channel ""
   :stories []
   :story-modal {:story  nil
                 :visible false
                 :images {:current 0}
                 :map-visible? false}
   :story-like-modal {:visible false
                      :users []}
   :login-form {:username {:value ""
                           :valid true
                           :error "Invalid Username"
                           :validating false
                           :activated? false}
                :password {:value ""
                           :valid true
                           :error "Invalid Password"
                           :validating false
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
   :new-story {:title ""
               :map {:lat 0
                     :lng 0}
               :description ""
               :images []
               :category ""
               :datetime {:date ""
                          :time ""}
               :errors []
               :phase :editing ;; editing or saving
               :saving {:progress 0
                        :state :progress ;; error success or progress
                        :message ""}}
   :channels {:all []
              :color {:country "red"
                      :admin-lvl-1 "blue"
                      :admin-lvl-2 "orange"
                      :locality "green"}
              :confirm-visible? {"1" false
                                 "2" false}
              :locations []
              :location-selected? false
              :active-tab 0
              :tabs {:channels 0
                     :add 1}
              :saving {:visible false
                       :progress 0
                       :state :progress
                       :error-msg ""}}})
