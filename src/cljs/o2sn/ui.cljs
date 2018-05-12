(ns o2sn.ui
  (:require [cljsjs.semantic-ui-react :as sm]
            [cljsjs.react-motion]
            [reagent.core :as r]
            [goog.object]))

;; this function was taken from https://opensourcery.co.za/2017/02/12/using-semantic-ui-react-with-re-frame/

(defn component
  [k & ks]
  (if (seq ks)
    (apply goog.object/getValueByKeys sm k ks)
    (goog.object/get sm k)))

(def button (component "Button"))
(def button-content (component "Button" "Content"))
(def icon (component "Icon"))
(def menu (component "Menu"))
(def menu-menu (component "Menu" "Menu"))
(def menu-item (component "Menu" "Item"))
(def input (component "Input"))
(def label (component "Label"))
(def grid (component "Grid"))
(def grid-column (component "Grid" "Column"))
(def header (component "Header"))
(def image (component "Image"))
(def segment (component "Segment"))
(def message (component "Message"))
(def form (component "Form"))
(def form-input (component "Form" "Input"))
(def form-field (component "Form" "Field"))
(def transition (component "Transition"))
(def transition-group (component "Transition" "Group"))
(def transitionable-portal (component "TransitionablePortal"))
(def responsive (component "Responsive"))

;; motions

(def motion (r/adapt-react-class js/ReactMotion.Motion))
(def presets js/ReactMotion.presets)

(defn spring [n opts]
  (js/ReactMotion.spring n opts))

(defn get-vmap [props vals]
  (let [vals (js->clj vals :keywordize-keys true)
        calc-pairs (map (fn [pair]
                          (let [f (first pair)
                                s (second pair)]
                            (hash-map f ((:fn s) (get vals (:val s)))))) props)
        vmap (reduce merge calc-pairs)]
    vmap))
