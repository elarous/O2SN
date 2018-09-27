(ns o2sn.dummy-effects
  (:require [reagent.core :as r]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   reg-fx
                                   dispatch
                                   debug]]))

(def user {:_id "users/1", :email "elarbaouioussama@gmail.com", :hash 1234234, :_rev "_XDU2IK2--_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "oussama", :activated false, :avatar "myAvatar.svg", :profile "profiles/2", :_key "1"})

(def all-users [{:_id "users/3", :email "karim@gmail.com", :hash 1234234, :_rev "_XDU3fG---_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "karim", :activated false, :avatar "otherAvatar.jpg", :_key "3"} {:_id "users/4", :_key "4", :_rev "_W26kyZC--_", :activated false, :email "ahmed@gmail.com", :hash 1234234, :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "ahmed"} {:_id "users/5", :_key "5", :_rev "_W26lLFO--_", :activated false, :email "yassine@gmail.com", :hash 1234234, :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "yassine"} {:_id "users/1139844", :_key "1139844", :_rev "_W0nefQe--_", :activated false, :email "octobus.deals@gmail.com", :hash "45c51d86ebd9b5d5c2ce1f975c61fcef709f45ecc7fc8bd5c0f81334bd555070", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "sami"} {:_id "users/2177137", :email "elhattab.ahmed@gmail.com", :hash "0b4788f73221c136d8cb62267dbd768e7946b4daab00a9aebd037def86920aed", :_rev "_XHNi29m--_", :password "bcrypt+sha512$a1d86aa47e0a592c81527961c8270a97$12$b18d54dde8c15ff1ea045484501580013aa8d014491e97f7", :username "ahmad", :activated false, :avatar "default.svg", :profile "profiles/2177135", :_key "2177137"} {:_id "users/2171877", :email "exir.bessma@gmail.com", :hash "4554bbd0557d034e78727562798ca91332f617de2774102635cc13e872641108", :_rev "_XHL31Um--_", :password "bcrypt+sha512$36fa1d85f8a5a37790628268e0998fb4$12$8ecb5977832959fea443fbc971f1bf6fd8157f6569e89d38", :username "nadal", :activated false, :avatar "default.svg", :profile "profiles/2171875", :_key "2171877"} {:_id "users/2", :_key "2", :_rev "_W26kI4u--_", :activated false, :email "sami@gmail.com", :hash 1234234, :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "sami"} {:_id "users/1", :email "elarbaouioussama@gmail.com", :hash 1234234, :_rev "_XDU2IK2--_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "oussama", :activated false, :avatar "myAvatar.svg", :profile "profiles/2", :_key "1"}])

(def all-categories [{:_id "categories/2", :_key "2", :_rev "_XBKAfaC--_", :color "orange", :name "Natural Disaster"} {:_id "categories/3", :_key "3", :_rev "_XBJds7q--_", :color "purple", :name "Event"} {:_id "categories/1", :_key "1", :_rev "_XBJd8S6---", :color "red", :name "Accident"}])

(def channels-for-user-1
  [{:_key "2232426", :name "Centre Commune Ouled Imloul", :subscribers 1, :type "locality"} {:_key "1839379", :name "El Kelâa des Sraghna", :subscribers 1, :type "locality"} {:_key "1839360", :name "El Kelaâ des Sraghna", :subscribers 3, :type "admin-lvl-2"} {:_key "2269438", :name "Ouled Ayad", :subscribers 1, :type "locality"} {:_key "2108049", :name "Algérie", :subscribers 2, :type "country"} {:_key "1839449", :name "El Attaouia", :subscribers 1, :type "locality"} {:_key "2198171", :name "Souss Massa", :subscribers 1, :type "admin-lvl-1"} {:_key "1839328", :name "Maroc", :subscribers 4, :type "country"} {:_key "1839343", :name "Marrakech-Safi", :subscribers 2, :type "admin-lvl-1"} {:_key "2198268", :name "Tinghir", :subscribers 1, :type "locality"} {:_key "2269460", :name "Province Fkih Ben Salah", :subscribers 1, :type "admin-lvl-2"} {:_key "2267884", :name "Agadir", :subscribers 1, :type "locality"} {:_key "1841063", :name "Béni Mellal-Khénifra", :subscribers 1, :type "admin-lvl-1"} {:_key "2181896", :name "Rehamna", :subscribers 1, :type "admin-lvl-2"} {:_key "2232539", :name "Chichaoua", :subscribers 1, :type "admin-lvl-2"}])

(def stories-by-channel
  {"2232426" nil
   "2108049" [{:description "Lorem ipsum dolor sit amet, in non vitae integer luctus, sed a, orci risus sed Ante nonummy ipsum, sed justo dignissim, nostra suspendisse dis erat eu. Nec faucibus ", :category {:_id "categories/1", :_key "1", :_rev "_XBJd8S6---", :color "red", :name "Accident"}, :_id "stories/2108024", :lie [{:_id "users/3", :email "karim@gmail.com", :hash 1234234, :_rev "_XDU3fG---_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "karim", :activated false, :avatar "otherAvatar.jpg", :_key "3"}], :_rev "_XHkhu7a--E", :dislikes [{:_id "users/3", :email "karim@gmail.com", :hash 1234234, :_rev "_XDU3fG---_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "karim", :activated false, :avatar "otherAvatar.jpg", :_key "3"}], :images ["331cb6d272f926c9836344c8fc9a2d4234037e59ed665fd661589a7a1e89af479e198fcea0048c15.png" "331cb6d272f926c9836344c8fc9a2d4234037e596d0e1050797b03d8826ea5ad224adba68621f692.png"], :likes [{:_id "users/1", :email "elarbaouioussama@gmail.com", :hash 1234234, :_rev "_XDU2IK2--_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "oussama", :activated false, :avatar "myAvatar.svg", :profile "profiles/2", :_key "1"}], :title "Mollis eget tempor, ante libero urna suspendisse", :datetime {:date "2018-07-16", :time "14:22"}, :truth [{:_id "users/1", :email "elarbaouioussama@gmail.com", :hash 1234234, :_rev "_XDU2IK2--_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "oussama", :activated false, :avatar "myAvatar.svg", :profile "profiles/2", :_key "1"}], :location {:_id "locations/2108018", :_key "2108018", :_rev "_XH2xuS6--D", :lat 31.937339012308502, :lng 5.333162836637257, :name "point", :type "point"}, :owner {:_id "users/3", :email "karim@gmail.com", :hash 1234234, :_rev "_XDU3fG---_", :password "bcrypt+sha512$5eb828043d5cbca0c84450b320e8fea3$12$321a6209ff37425722e95846f1f5209410c3164833980cda", :username "karim", :activated false, :avatar "otherAvatar.jpg", :_key "3"}, :_key "2108024"}]})

(def profile-user-1
  {:_id "profiles/2", :email "elarbaouioussama@gmail.com", :_rev "_XCzEMDG--_", :age 23, :username "oussama", :fullname "Oussama El Arbaoui", :avatar "myAvatar.svg", :gender "male", :country "ma", :_key "2"})

(def search-stories-o
  [{:_id "stories/2182682", :_key "2182682", :channel "El Kelâa des Sraghna", :title "nonummy ipsum, sed justo dignissim, nostra", :type "story"} {:_id "stories/2232856", :_key "2232856", :channel "El Kelâa des Sraghna", :title "Lorem ipsum dolor sit amet, in non vitae", :type "story"} {:_id "stories/1845587", :_key "1845587", :channel "El Kelâa des Sraghna", :title "Mollis eget tempor, ante libero urna suspendisse", :type "story"} {:_id "stories/2269535", :_key "2269535", :channel "El Kelâa des Sraghna", :title "another one another one another one another one ", :type "story"} {:_id "stories/2350311", :_key "2350311", :channel "El Kelâa des Sraghna", :title "yokohama yokohama yokohama yokohama yokohama yokohama ", :type "story"} {:_id "stories/2046993", :_key "2046993", :channel "El Attaouia", :title "nonummy ipsum, sed justo dignissim, nostra", :type "story"} {:_id "stories/1845809", :_key "1845809", :channel "El Attaouia", :title "Tempor id aenean sit, non mauris ac ", :type "story"} {:_id "stories/2108024", :_key "2108024", :channel "Ouargla", :title "Mollis eget tempor, ante libero urna suspendisse", :type "story"} {:_id "stories/2234962", :_key "2234962", :channel "Agadir", :title "Mollis eget tempor, ante libero urna suspendisse", :type "story"} {:_id "stories/2270407", :_key "2270407", :channel "Marrakech", :title "you will never walk alone", :type "story"} {:_id "stories/1905541", :_key "1905541", :channel "Demnate", :title "Tempor id aenean sit, non mauris ac ", :type "story"} {:_id "stories/2120935", :_key "2120935", :channel "Ouarzazate", :title "sodales faucibus proin, elit sollicitudin.", :type "story"}])

(def search-users-o
  [{:_id "users/1", :_key "1", :avatar "myAvatar.svg", :fullname "Oussama El Arbaoui", :username "oussama", :type "user"}])

(def all-activities
  [{:_id "activities/2189297", :_key "2189297", :_rev "_XHRZPEG--_", :by "users/1", :target "stories/2182682", :type "truth"} {:_id "activities/2074244", :_key "2074244", :_rev "_XFtpzEG--_", :by "users/1", :target "stories/1845809", :type "like"} {:_id "activities/2073279", :_key "2073279", :_rev "_XFtWfOG--_", :by "users/1", :target "stories/1845809", :type "like"} {:_id "activities/2076647", :_key "2076647", :_rev "_XFuan2G--_", :by "users/1", :target "stories/1845809", :type "lie"} {:_id "activities/2074273", :_key "2074273", :_rev "_XFtqT3G--_", :by "users/1", :target "stories/1845809", :type "like"} {:_id "activities/2134471", :_key "2134471", :_rev "_XGmXcDG--_", :by "users/1", :target "stories/1845809", :type "truth"}])

(defmulti success (fn [d] (:target d)))

;; search

(def story-like (r/atom true))
(def story-dislike (r/atom false))

(def story-truth (r/atom true))
(def story-lie (r/atom false))

(defmethod success :search/stories [{on-success :on-success}]
  (dispatch (conj on-success (take 5 search-stories-o))))

(defmethod success :search/users [{on-success :on-success}]
  (dispatch (conj on-success (take 2 search-users-o))))

(defmethod success :channels/load [{on-success :on-success}]
  (dispatch (conj on-success channels-for-user-1)))

(defmethod success :home/stories [{on-success :on-success}]
  (dispatch (conj on-success (get stories-by-channel "2108049"))))

(defmethod success :home/toggle-like [{on-success :on-success}]
  (if @story-like
    (reset! story-like false)
    (do
      (reset! story-like true)
      (reset! story-dislike false)))
  (dispatch (conj on-success {:liked @story-like
                              :disliked @story-dislike})))

(defmethod success :home/toggle-dislike [{on-success :on-success}]
  (if @story-dislike
    (reset! story-dislike false)
    (do
      (reset! story-dislike true)
      (reset! story-like false)))
  (dispatch (conj on-success {:liked @story-like
                              :disliked @story-dislike})))

(defmethod success :story/toggle-truth [{on-success :on-success}]
  (if @story-truth
    (reset! story-truth false)
    (do
      (reset! story-truth true)
      (reset! story-lie false)))
  (dispatch (conj on-success {:truth @story-truth
                              :lie @story-lie})))

(defmethod success :story/toggle-lie [{on-success :on-success}]
  (if @story-lie
    (reset! story-lie false)
    (do
      (reset! story-lie true)
      (reset! story-truth false)))
  (dispatch (conj on-success {:truth @story-truth
                              :lie @story-lie})))

(defmethod success :story/load-by-key [{on-success :on-success}]
  (dispatch (conj on-success (first (get stories-by-channel "2108049")))))

(reg-fx
 :dummy-http-xhrio
 (fn [{:keys [uri on-success target] :as d}]
   (success d)))
