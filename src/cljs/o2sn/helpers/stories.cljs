(ns o2sn.helpers.stories)

(defn story-in-stories? [db story-k]
  (some #{story-k} (map :_key (:stories db))))

(defn update-in-stories [db story]
  (-> db
      (update :stories
              (fn [o] (remove #(= (:_key %) (:_key story)) o)))
      (update :stories
              (fn [o] (conj o story)))))

(defn get-story [db k]
  (some #(and (= k (:_key %)) %)
        (:stories db)))

(defn get-current-story [db]
  (let [k (get-in db [:story :current])]
    (get-story db k)))

(defn format-date [datetime]
  (str (:date datetime) " " (:time datetime)))
