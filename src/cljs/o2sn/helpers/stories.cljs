(ns o2sn.helpers.stories)

(defn get-story [db k]
  (some #(and (= k (:_key %)) %)
        (:stories db)))

(defn get-current-story [db]
  (let [k (get-in db [:story-modal :story])]
    (get-story db k)))
