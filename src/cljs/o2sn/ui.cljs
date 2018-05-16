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

(def button                (r/adapt-react-class (component "Button")))
(def button-content        (r/adapt-react-class (component "Button" "Content")))
(def icon                  (r/adapt-react-class (component "Icon")))
(def menu                  (r/adapt-react-class (component "Menu")))
(def menu-menu             (r/adapt-react-class (component "Menu" "Menu")))
(def menu-item             (r/adapt-react-class (component "Menu" "Item")))
(def input                 (r/adapt-react-class (component "Input")))
(def label                 (r/adapt-react-class (component "Label")))
(def grid                  (r/adapt-react-class (component "Grid")))
(def grid-column           (r/adapt-react-class (component "Grid" "Column")))
(def header                (r/adapt-react-class (component "Header")))
(def image                 (r/adapt-react-class (component "Image")))
(def segment               (r/adapt-react-class (component "Segment")))
(def message               (r/adapt-react-class (component "Message")))
(def form                  (r/adapt-react-class (component "Form")))
(def form-input            (r/adapt-react-class (component "Form" "Input")))
(def form-field            (r/adapt-react-class (component "Form" "Field")))
(def transition            (r/adapt-react-class (component "Transition")))
(def transition-group      (r/adapt-react-class (component "Transition" "Group")))
(def transitionable-portal (r/adapt-react-class (component "TransitionablePortal")))
(def responsive            (r/adapt-react-class (component "Responsive")))

;; motions

(def motion (r/adapt-react-class js/ReactMotion.Motion))
(def presets js/ReactMotion.presets)
(def spring js/ReactMotion.spring)
