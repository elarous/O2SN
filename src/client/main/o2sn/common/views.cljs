(ns o2sn.common.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [kee-frame.core :as k]
            ["semantic-ui-react" :as ui]
            ["react-motion" :as motion]))

(defn error-label [{:keys [valid? message path-to-ctrl]}]
  (let [[start-x end-x] (if valid? [35 0] [0 35])
        [start-scale end-scale] (if valid? [1 0] [0 1])
        anim-opts (if valid? (clj->js {:stiffness 65
                                       :damping 17})
                      (.-wobbly motion/presets))
        activated? @(rf/subscribe [:forms/control-activated? path-to-ctrl])]
    (when (or activated? (not valid?))
      (rf/dispatch [:forms/activate-control path-to-ctrl])
      [:> motion/Motion {:default-style {:x start-x :s start-scale}
                  :style {:s (motion/spring end-scale anim-opts)
                          :x (motion/spring end-x anim-opts)}}
       (fn [v]
         (r/as-element
          [:div {:style {:transform (str "scale(" (.-s v) "," (.-s v) ")")
                         :opacity (.-s v)
                         :height (str (.-x v) "px")}}
           [:> ui/Label {:basic true
                      :color (if valid? "teal" "red")
                      :pointing "below"}
            (if-not valid? message "(^ _ ^)'")]]))])))
