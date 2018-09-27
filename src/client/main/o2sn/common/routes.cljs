(ns o2sn.common.routes
  (:require [bidi.bidi :as bidi]))

(def routes ["" {"/" :home
                 "/story" {["/view/" :story] :view-story
                           "/new" :new-story}
                 "/login" :login
                 "/logout" :logout
                 "/signup" :signup
                 "/channels" {"/list":list-channels
                              "/add" :add-channel}
                 "/profile" {"" :my-profile
                             ["/" :user] :profile}
                 "/messages" :messages
                 "/friends" :friends
                 "/settings" :settings
                 "/operation" :operation}])
