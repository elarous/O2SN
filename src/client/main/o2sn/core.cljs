(ns o2sn.core
  (:require [reagent.core :as  r]
            [re-frame.core :as rf]
            [kee-frame.core :as k]
            ["semantic-ui-react" :as ui]
            [o2sn.validation.validation]
            [o2sn.common.routes :refer [routes]]
            [o2sn.common.db :refer [default-db]]
            [o2sn.common.effects]
            [o2sn.common.subs]
            [o2sn.common.events]
            [o2sn.common.controllers]
            [o2sn.operation.subs]
            [o2sn.operation.events]
            [o2sn.operation.views :refer [operation-page]]
            [o2sn.login.views :refer [login-form]]
            [o2sn.login.subs]
            [o2sn.login.events]
            [o2sn.logout.events]
            [o2sn.signup.views :refer [signup-form]]
            [o2sn.signup.subs]
            [o2sn.signup.events]
            [o2sn.home.views :refer [home-page]]
            [o2sn.home.subs]
            [o2sn.home.events]
            [o2sn.topbar.views :refer [main-menu]]
            [o2sn.topbar.events]
            [o2sn.topbar.subs]
            [o2sn.sidebar.views :refer [side-bar]]
            [o2sn.sidebar.subs]
            [o2sn.sidebar.events]
            [o2sn.channels.events]
            [o2sn.channels.subs]
            [o2sn.channels.views :refer [list-channels-page
                                         add-channel-page]]
            [o2sn.story.events]
            [o2sn.story.subs]
            [o2sn.story.views :refer [story-page]]
            [o2sn.new-story.subs]
            [o2sn.new-story.events]
            [o2sn.new-story.views :refer [new-story-page]]
            [o2sn.profile.subs]
            [o2sn.profile.events]
            [o2sn.profile.views :refer [profile-page]]
            [o2sn.notifications.views :refer [notifications-page
                                              notifications-alert]]
            [o2sn.notifications.events]
            [o2sn.notifications.subs]
            [o2sn.notifications.effects]
            [o2sn.dummy-effects]))


(defn normal-page [page-contents]
  [:div#normal-page
   [main-menu]
   [side-bar page-contents]
   [notifications-alert]])

(defn full-page [page-contents]
  [:div#full-page
   page-contents])


(defn msgs-page []
  [:div {:style {:height "100vh"
                 :width "100vw"
                 :display "flex"
                 :justify-content "center"
                 :align-items "center"}}
   #_[card {:image "/img/crash.jpg"
          :title "The Title Of The Story"
          :date "22/10/2017 12:34"
          :distance 5
          :description "This is just a normal story and it's not special at all as you see"
          :category "Event"
          :story-k "2589162"
          :likes (repeat 20
                         {:_key "1"
                          :username "karim"
                          :avatar "/img/user.svg"})
          :dislikes (repeat 20
                         {:_key "3"
                          :username "karim"
                          :avatar "/img/user.svg"})}]])

(defn content []
  [k/switch-route (fn [route]
                    (if (or @(rf/subscribe [:common/user])
                            (= (:handler route) :signup))
                      (:handler route)
                      :login))
   :home [normal-page [home-page]]
   :view-story [normal-page [story-page]]
   :new-story [normal-page [new-story-page]]
   :list-channels [normal-page [list-channels-page]]
   :add-channel [normal-page [add-channel-page]]
   :profile [normal-page [profile-page]]
   :my-profile [normal-page [profile-page]]
   :messages [normal-page [msgs-page]]
   :friends [normal-page [:div "my friends"]]
   :settings [normal-page [:div "my settings"]]
   :operation [normal-page [operation-page]]
   :notifications [normal-page [notifications-page]]
   :login [full-page [login-form]]
   :signup [full-page [signup-form]]
   nil [full-page [:div "Page Not Found"]]])

(defn load-data! []
  (rf/dispatch [:common/load-categories]))

(defn start! []
  (println "Starting ...")
  (rf/clear-subscription-cache!)
  (k/start! {:initial-db default-db
             :routes routes
             :root-component [content]
             :debug? true})
  (load-data!))

(defn stop! []
  (println "Stopping ..."))
