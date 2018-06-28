(ns o2sn.views.operation
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [o2sn.ui :as ui]))



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

(defn operation-segment [{:keys [state title progress sub-title m-success m-error]}]
  (let [success? (= state :success)
        error? (= state :error)]
    [ui/segment
     [ui/header {:as "h2"
                 :color (cond success? "green"
                              error? "red"
                              :else "black")
                 :text-align "left"}
      [ui/icon {:name (cond success? "check"
                            error? "x"
                            :else "world")}]
      [ui/header-content title
       [ui/header-subheader
        (cond success? (:sub-title m-success)
              error? (:sub-title m-error)
              :else sub-title)]]]
     [ui/divider {:hidden true}]
     [ui/progress {:success success?
                   :error error?
                   :percent progress}
      (cond success? "success"
            error? "error"
            :else  "operating ...")]
     (when (or success? error?)
       [ui/message {:success success?
                    :error error?}
        [ui/container {:text-align "center"}
         (if success?
           [:div
            (:done-msg m-success)
            [ui/divider {:hidden true}]
            [ui/button {:color "green"
                        :on-click (:on-click m-success)}
             (:btn-txt m-success)]]
           [:div
            (:done-msg m-error)
            [ui/divider {:hidden true}]
            [ui/button {:color "red"
                        :on-click (:on-click m-error)}
             (:btn-txt m-error)]])]])]))
