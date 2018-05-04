(ns o2sn.ui
  (:require [cljsjs.semantic-ui-react :as sm]
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
(def form (component "Form"))
(def segment (component "Segment"))
(def form-input (component "Form" "Input"))
(def message (component "Message"))
