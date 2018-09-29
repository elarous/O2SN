(ns o2sn.config
  (:require [mount.core :as mount]))

(def default {:dev true
              :port 3000
              ;; when :nrepl-port is set the application starts the nREPL server on load
              :nrepl-port 7000
              :database {:host "localhost"
                         :port 8529
                         :user "root"
                         :password "123456"
                         :dbname "o2sn"}
              :server {:addr "localhost"
                       :port 3000}
              :email-config {:email "elarbaouioussama@gmail.com"
                             :host "smtp.gmail.com"
                             :user "elarbaouioussama"
                             :pass "" 
                             :ssl "465"}
              :cloudinary {"cloud_name" "dofhpvqpj"
                           "api_key" "889295516416771"
                           "api_secret" "gjfsEAW5CYtGeVBSLGpXZYaNrR8"}})

(defn- get-conf! []
  default)

(mount/defstate conf
  :start (get-conf!))
