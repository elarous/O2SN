(ns o2sn.common.controllers
  (:require [kee-frame.core :refer [reg-controller]]))

(reg-controller :home
                {:params (fn [{handler :handler}]
                           (when (= handler :home)
                             true))
                 :start [:set-home-contents]})

(reg-controller :login
                {:params (fn [{handler :handler}]
                           (when (= handler :login)
                             true))
                 :start [:set-login-contents]})

(reg-controller :view-story
                {:params (fn [{:keys [handler route-params]}]
                           (when (= handler :view-story)
                             (:story route-params)))
                 :start [:story/load-by-key]})

(reg-controller :my-profile
                {:params (fn [{handler :handler}]
                           (when (= handler :my-profile) true))
                 :start [:profile/load]})

(reg-controller :profile
                {:params (fn [{:keys [handler route-params]}]
                           (when (= handler :profile)
                             (:user route-params)))
                 :start [:profile/load-by-user]})

(reg-controller :list-channels
                {:params (fn [{:keys [handler route-params]}]
                           (when (= handler :list-channels)
                             true))
                 :start [:channels/load]})

(reg-controller :notifications
                {:params (fn [{:keys [handler route-params]}]
                           (when (= handler :notifications)
                             true))
                 :start [:notifs/get-notifs]})
