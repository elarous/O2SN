(ns o2sn.maps
  (:require [ajax.core :as ajax]))

(defn- make-url [{:keys [lat lng]}]
  (str "https://maps.googleapis.com/maps/api/geocode/json?latlng="
       lat "," lng
       "&key=AIzaSyBUGwGf5iRDVzcJ-22B-JhzpTrCA2FMW1o"
       "&language=fr"
       "&result_type=country%7Cadministrative_area_level_1"
       "%7Cadministrative_area_level_2%7Clocality"))

(defn- transform-data [data]
  (let [addr-compts (-> data
                        (get "results")
                        first
                        (get "address_components"))
        with-names (map #(hash-map :name (get % "long_name")
                                   :type (if (vector? (get % "types"))
                                           (first (get % "types"))
                                           (get % "types")))
                        addr-compts)
        shorter-types (map (fn [m]
                             (update m :type
                                     #(cond (= "administrative_area_level_1" %)
                                            "admin-lvl-1"
                                            (= "administrative_area_level_2" %)
                                            "admin-lvl-2"
                                            :else %)))
                           with-names)
        filtered-locs (filter
                       #(#{"country" "admin-lvl-1" "admin-lvl-2" "locality"}
                         (:type %)) shorter-types)]
    filtered-locs))

(defn get-locations [{:keys [lat lng] :as cords} cb]
  (ajax/GET (make-url cords)
            {:handler #(cb (transform-data %))}))

