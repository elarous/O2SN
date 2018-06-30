(ns o2sn.ui
  (:require [cljsjs.semantic-ui-react :as sm]
            [cljsjs.react-motion]
            [cljsjs.react-google-maps]
            [reagent.core :as r]
            [goog.object]))

;; this function was taken from https://opensourcery.co.za/2017/02/12/using-semantic-ui-react-with-re-frame/

(defn component
  [k & ks]
  (if (seq ks)
    (apply goog.object/getValueByKeys sm k ks)
    (goog.object/get sm k)))

(def container             (r/adapt-react-class (component "Container")))
(def button                (r/adapt-react-class (component "Button")))
(def button-content        (r/adapt-react-class (component "Button" "Content")))
(def button-or             (r/adapt-react-class (component "Button" "Or")))
(def button-group          (r/adapt-react-class (component "Button" "Group")))
(def icon                  (r/adapt-react-class (component "Icon")))
(def menu                  (r/adapt-react-class (component "Menu")))
(def menu-menu             (r/adapt-react-class (component "Menu" "Menu")))
(def menu-item             (r/adapt-react-class (component "Menu" "Item")))
(def input                 (r/adapt-react-class (component "Input")))
(def label                 (r/adapt-react-class (component "Label")))
(def grid                  (r/adapt-react-class (component "Grid")))
(def grid-column           (r/adapt-react-class (component "Grid" "Column")))
(def header                (r/adapt-react-class (component "Header")))
(def header-content        (r/adapt-react-class (component "Header" "Content")))
(def header-subheader      (r/adapt-react-class (component "Header" "Subheader")))
(def image                 (r/adapt-react-class (component "Image")))
(def image-group           (r/adapt-react-class (component "Image" "Group")))
(def segment               (r/adapt-react-class (component "Segment")))
(def message               (r/adapt-react-class (component "Message")))
(def message-content       (r/adapt-react-class (component "Message" "Content")))
(def message-header        (r/adapt-react-class (component "Message" "Header")))
(def form                  (r/adapt-react-class (component "Form")))
(def form-input            (r/adapt-react-class (component "Form" "Input")))
(def form-field            (r/adapt-react-class (component "Form" "Field")))
(def transition            (r/adapt-react-class (component "Transition")))
(def transition-group      (r/adapt-react-class (component "Transition" "Group")))
(def transitionable-portal (r/adapt-react-class (component "TransitionablePortal")))
(def responsive            (r/adapt-react-class (component "Responsive")))
(def sidebar               (r/adapt-react-class (component "Sidebar")))
(def sidebar-pushable      (r/adapt-react-class (component "Sidebar" "Pushable")))
(def sidebar-pusher        (r/adapt-react-class (component "Sidebar" "Pusher")))
(def search                (r/adapt-react-class (component "Search")))
(def dropdown              (r/adapt-react-class (component "Dropdown")))
(def dropdown-menu         (r/adapt-react-class (component "Dropdown" "Menu")))
(def dropdown-item         (r/adapt-react-class (component "Dropdown" "Item")))
(def popup                 (r/adapt-react-class (component "Popup")))
(def feed                  (r/adapt-react-class (component "Feed")))
(def feed-event            (r/adapt-react-class (component "Feed" "Event")))
(def tab                   (r/adapt-react-class (component "Tab")))
(def tab-pane              (r/adapt-react-class (component "Tab" "Pane")))
(def card                  (r/adapt-react-class (component "Card")))
(def card-content          (r/adapt-react-class (component "Card" "Content")))
(def card-header           (r/adapt-react-class (component "Card" "Header")))
(def card-meta             (r/adapt-react-class (component "Card" "Meta")))
(def card-description      (r/adapt-react-class (component "Card" "Description")))
(def reveal                (r/adapt-react-class (component "Reveal")))
(def reveal-content        (r/adapt-react-class (component "Reveal" "Content")))
(def textarea              (r/adapt-react-class (component "TextArea")))
(def modal                 (r/adapt-react-class (component "Modal")))
(def modal-content         (r/adapt-react-class (component "Modal" "Content")))
(def modal-actions         (r/adapt-react-class (component "Modal" "Actions")))
(def modal-header          (r/adapt-react-class (component "Modal" "Header")))
(def modal-description     (r/adapt-react-class (component "Modal" "Description")))
(def list                  (r/adapt-react-class (component "List")))
(def list-item             (r/adapt-react-class (component "List" "Item")))
(def list-content          (r/adapt-react-class (component "List" "Content")))
(def list-header           (r/adapt-react-class (component "List" "Header")))
(def divider               (r/adapt-react-class (component "Divider")))
(def progress              (r/adapt-react-class (component "Progress")))

(def dimmer                (r/adapt-react-class (component "Dimmer")))
(def loader                (r/adapt-react-class (component "Loader")))
(def select                (r/adapt-react-class (component "Select")))
(def confirm               (r/adapt-react-class (component "Confirm")))
(def flag                  (r/adapt-react-class (component "Flag")))
(def statistic             (r/adapt-react-class (component "Statistic")))
(def statistic-group       (r/adapt-react-class (component "Statistic" "Group")))
(def statistic-value       (r/adapt-react-class (component "Statistic" "Value")))
(def statistic-label       (r/adapt-react-class (component "Statistic" "Label")))
(def rating                (r/adapt-react-class (component "Rating")))



;; motions

(def motion (r/adapt-react-class js/ReactMotion.Motion))
(def presets js/ReactMotion.presets)
(def spring js/ReactMotion.spring)

;; google maps

(def google-map js/ReactGoogleMaps.GoogleMap)
(def marker (r/adapt-react-class js/ReactGoogleMaps.Marker))
(def with-google-map js/ReactGoogleMaps.withGoogleMap)
(def with-scriptjs js/ReactGoogleMaps.withScriptjs)
