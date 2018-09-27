(ns o2sn.core
  (:require [mount.core :as m]
            [aleph.http :as http]
            [o2sn.handler :as handler])
  (:gen-class))


(m/defstate http-server
  :start (http/start-server handler/app {:port 3000})
  :stop (.close http-server))


(defn -main [& args]
  (m/start)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. m/stop)))

