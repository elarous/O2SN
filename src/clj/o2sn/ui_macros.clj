(ns o2sn.ui-macros)

(defmacro mchild [props child-style child]
  (let [vals-sym (gensym)]
    `(fn [~vals-sym]
       (let ~['vmap `(o2sn.ui/get-vmap ~props ~vals-sym)
              'style-map `(hash-map :style ~'vmap)]
         (reagent.core/as-element
          (-> (vector (first ~child))
              (conj (update ~'style-map :style #(merge % ~child-style)))
              (concat (rest ~child))
              vec))))))

#_(clojure.pprint/pprint (macroexpand '(mchild
                                      {:font-size {:val :x :fn (fn [x] (* 3 x))}}
                                      {:color "pink"}
                                      [:div
                                       (str "something : ")])))

#_(defmacro mchild [props child-style child]
  (let [vals-sym (gensym)]
    `(fn [~vals-sym]
       (let ~['vmap `(o2sn.ui/get-vmap ~props ~vals-sym)
              'style-map `(hash-map :style ~'vmap)]
         (reagent.core/create-element
          "div"
          (cljs.core/clj->js (update ~'style-map :style #(merge % ~child-style)))
          (reagent.core/as-element ~child))))))
