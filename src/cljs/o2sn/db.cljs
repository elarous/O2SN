(ns o2sn.db
  (:require [o2sn.views.home :as home]
            [o2sn.views.login :as login]
            [o2sn.views.signup :as signup]))

(def default-db
  {:page {:active :home
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
            :messages #'home/messages-panel}

   :sidebar {:visible true}
   :user {:logged-in? false
          :current nil}
   :welcome {:form :login-form
             :animation-completed? true}
   :user-channels {:selected nil
                   :all []}
   :stories []
   :story-modal {:story  nil
                 :visible false
                 :images {:current 0}
                 :map-visible? false}
   :story-like-modal {:visible false
                      :users []}
   :categories {"Accident" "purple"
                "Natural Disaster" "blue"
                "Event" "violet"}
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
                 :signed-up? false}})
