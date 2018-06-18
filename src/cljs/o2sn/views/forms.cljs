(ns o2sn.views.forms
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [o2sn.ui :as ui]))

(defn error-label [valid? msg ctrl form]
  (let [[start-x end-x] (if valid? [35 0] [0 35])
        [start-scale end-scale] (if valid? [1 0] [0 1])
        anim-opts (if valid? (clj->js {:stiffness 65
                                       :damping 17})
                      (.-wobbly ui/presets))
        activated? @(rf/subscribe [:form-control-activated? form ctrl])]
    (when (or activated? (not valid?))
      (rf/dispatch [:set-form-control-activated? form ctrl])
      [ui/motion {:default-style {:x start-x :s start-scale}
                  :style {:s (ui/spring end-scale anim-opts)
                          :x (ui/spring end-x anim-opts)}}
       (fn [v]
         (r/as-element
          [:div {:style {:transform (str "scale(" (.-s v) "," (.-s v) ")")
                         :opacity (.-s v)
                         :height (str (.-x v) "px")}}
           [ui/label {:basic true
                      :color (if valid? "teal" "red")
                      :pointing "below"}
            (if-not valid? msg "(^ _ ^)'")]]))])))
