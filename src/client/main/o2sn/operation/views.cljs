(ns o2sn.operation.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]))

;;; params exemple :

;; state :success
;; title "Publish a Story"
;; progress 0
;; sub-title "publishing your story"
;; m-success {:sub-title "Your story has been published"
;;           :on-click identity
;;           :btn-txt "back home"
;;           :done-msg "your story is now public, go to and ..."}
;; m-error {:sub-title "Something Went wrong"
;;         :on-click identity
;;         :btn-txt "back"
;;         :done-msg "error occured in file ... and in ... "}

(defn operation-page []
  (let [state @(rf/subscribe [:operation/state])
        success? (= state :success)
        error? (= state :error)]
    [:> ui/Segment
     [:> ui/Header {:as "h2"
                    :color (cond success? "green"
                                 error? "red"
                                 :else "black")
                    :text-align "left"}
      [:> ui/Icon {:name (cond success? "check"
                               error? "x"
                               :else "world")}]
      [:> ui/Header.Content @(rf/subscribe [:operation/title])
       [:> ui/Header.Subheader
        (cond success? @(rf/subscribe [:operation/success-text])
              error? @(rf/subscribe [:operation/error-text])
              :else @(rf/subscribe [:operation/sub-title]))]]]
     [:> ui/Divider {:hidden true}]
     [:> ui/Progress {:success success?
                      :error error?
                      :percent @(rf/subscribe [:operation/progress])}
      (cond success? "Success"
            error? "Error"
            :else  "Operating ...")]
     (when (or success? error?)
       [:> ui/Message {:success success?
                       :error error?}
        [:> ui/Container {:text-align "center"}
         (if success?
           [:div
            @(rf/subscribe [:operation/success-sub-text])
            [:> ui/Divider {:hidden true}]
            [:> ui/Button {:color "green"
                           :on-click
                           #(rf/dispatch
                             [:navigate
                              @(rf/subscribe [:operation/success-route])])}
             @(rf/subscribe [:operation/success-btn-text])]]
           [:div
            @(rf/subscribe [:operation/error-sub-text])
            [:> ui/Divider {:hidden true}]
            [:> ui/Button {:color "red"
                           :on-click
                           #(rf/dispatch
                             [:navigate
                              @(rf/subscribe [:operation/error-route])])}
             @(rf/subscribe [:operation/error-btn-text])]])]])]))
