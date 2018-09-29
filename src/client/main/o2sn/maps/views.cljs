(ns o2sn.maps.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-google-maps" :as rgm]))

(def google-map rgm/GoogleMap)
(def with-google-map rgm/withGoogleMap)
(def with-scriptjs rgm/withScriptjs)

(defn extract-lat-lng [e]
  (js/console.log e)
  (hash-map :lat (.lat e)
            :lng (.lng e)))

(defn wrapped-map [{:keys [lat lng on-click]}]
  (let [map-class
        (r/adapt-react-class
         (with-scriptjs
           (with-google-map
             (fn [p]
               (r/create-element
                google-map
                #js {:defaultZoom 10
                     :defaultCenter #js {:lat lat, :lng lng}
                     :onClick on-click}
                (r/as-element
                 [:> rgm/Marker
                  {:position
                   {:lat lat
                    :lng lng}}]))))))]
    [map-class
     {:container-element (r/as-element [:div {:style {:height "400px"}}])
      :map-element (r/as-element [:div {:style {:height "100%"}}])
      :google-map-URL "https://maps.googleapis.com/maps/api/js?key=AIzaSyBUGwGf5iRDVzcJ-22B-JhzpTrCA2FMW1o&v=3.exp&libraries=geometry,drawing,places"
      :loading-element (r/as-element [:div {:style {:height "100%"}}])}]))

