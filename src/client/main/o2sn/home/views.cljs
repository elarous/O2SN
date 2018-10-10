(ns o2sn.home.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["semantic-ui-react" :as ui]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [clojure.string :as s]))

(defn- format-dt [dt-str]
  (when dt-str
    (let [dt (f/parse (f/formatters :date-time-no-ms) dt-str)]
      (f/unparse (f/formatters :mysql) dt))))

(defn card-likes-modal []
  [:> ui/Modal {:open @(rf/subscribe [:home/likes-modal-visible?])
                :close-icon true
                :size "mini"
                :on-close #(rf/dispatch [:home/hide-likes-modal])}
   [:> ui/Modal.Content
    [:> ui/List {:selection true
                 :vertical-align "middle"}
     (for [u @(rf/subscribe [:home/likes-modal-users])]
       ^{:key (:_key u)}
       [:> ui/List.Item  {:on-click #(rf/dispatch [:navigate :profile :user (:_key u)])}
        [:> ui/Image {:src (or (:avatar u)
                               "/img/user.svg")
                      :avatar true
                      :size "mini"}]
        [:> ui/List.Content
         [:> ui/List.Header
          [:> ui/Header {:as "h3"}
           (:username u)]]]])]]])

(defn card [{:keys [image title date distance description story-k category]}]
  (let [face (r/atom :main)]
    (fn []
      (let [likes @(rf/subscribe [:home/likes story-k])
            dislikes @(rf/subscribe [:home/dislikes story-k])]
        [:div#card {:on-click (fn [e]
                                (reset! face :actions))}
         [:div
          [:div.img-container
           [:img {:src (str "data:image/*;base64," image)}]]
          [:div.body
           [:div.title title]
           [:div.infos
            [:div.date date]
            [:div.distance distance "km away"]]
           [:div.description description]]]
         (when (= @face :actions)
           [:div.overlay
            [:div.header
             [:i.icon.fas.fa-arrow-left
              {:on-click (fn [e] (.stopPropagation e)
                           (reset! face :main))}]
             [:h3.category category]]
            [:div.body
             [:div.view-container
              {:on-click (fn [e]
                           (rf/dispatch [:navigate :view-story :story story-k]))}
              [:i.fas.fa-eye]
              [:p "View Story"]]]
            [:div.actions
             [:div.action
              [:i
               {:on-click #(rf/dispatch [:home/toggle-like story-k])
                :class-name (str "icon fas fa-thumbs-up"
                                 (if @(rf/subscribe [:home/like? story-k])
                                   " thumb-active " ""))}]
              [:div
               {:on-click (fn [e]
                            (.stopPropagation e)
                            (reset! face :likes))
                :class-name (str "count"
                                 (if @(rf/subscribe [:home/like? story-k])
                                   " count-active " ""))}
               (count likes)]]
             [:div.action
              [:i
               {:on-click #(rf/dispatch [:home/toggle-dislike story-k])
                :class-name (str "icon fas fa-thumbs-down"
                                 (if @(rf/subscribe [:home/dislike? story-k])
                                   " thumb-active " ""))}]
              [:div
               {:on-click (fn [e]
                            (.stopPropagation e)
                            (reset! face :dislikes))
                :class-name (str "count"
                                 (if @(rf/subscribe [:home/dislike? story-k])
                                   " count-active " ""))}
               (count dislikes)]]]])
         (when (some #{@face} [:likes :dislikes])
           (let [items (if (= @face :likes) likes dislikes)]
             [:div.likes-dislikes
              {:on-click (fn [e] (.stopPropagation e))}
              [:div.header
               [:i.icon.fas.fa-arrow-left
                {:on-click (fn [e]
                             (.stopPropagation e)
                             (reset! face :actions))}]
               [:h3 (if (= @face :likes) "Likes" "Dislikes")]]
              [:div.list
               (for [item items]
                 ^{:key (:_key item)}
                 [:div.item
                  [:div.avatar
                   [:img {:src (:avatar item)
                          :on-click #(rf/dispatch [:navigate :profile :user (:_key item)])}]]
                  [:div.username
                   {:on-click #(rf/dispatch [:navigate :profile :user (:_key item)])}
                   (:username item)]])]]))]))))

(defn chan-contents []
  [:div
   (let [stories @(rf/subscribe [:home/stories])]
     [:div#news-cards
      (for [story stories]
        ^{:key (:_key story)}
        [card {:image (get-in story [:cover_img :body])
               :title (:title story)
               :date (format-dt (:datetime story))
               :distance 5
               :description (str
                             (->> (clojure.string/split (:description story) #"\s")
                                  (take 10)
                                  (reduce (fn [acc n] (str acc " " n))))
                             "...")
               :category (get-in story [:category :name])
               :story-k (:_key story)
               :likes (:likes story)
               :dislikes (:dislikes story)}])])
   (when (and @(rf/subscribe [:home/can-load?])
              (not= 0 @(rf/subscribe [:home/stories-count])))
     [:div.load-more-stories
      (let [more-loading? @(rf/subscribe [:home/more-loading?])]
        [:button
         {:on-click #(rf/dispatch [:home/load-stories])
          :disabled more-loading?}
         (when more-loading?
             [:div.loader-container
              [:div#load-more-btn.btn-loader]])
         [:div "Load More Stories"]])])
   (when (zero? @(rf/subscribe [:home/stories-count]))
     [:h1#no-stories
      "No Stories To Show"])])


(defn channel-search []
  (let [opened? (r/atom false)
        filtered-chans (r/atom [])
        search-val (r/atom "")
        filter-by-type
        (fn [type] (reset! filtered-chans
                           (filter #(= type (:type %)) @(rf/subscribe [:home/channels]))))]
    (fn []
      [:div#channel-search
       {:on-focus #(reset! opened? true)}
       [:input {:type "text"
                :placeholder "Channel"
                :value @search-val
                :on-focus (fn [e]
                            (-> e .-target .select)
                            (when (empty? @filtered-chans)
                              (reset! filtered-chans @(rf/subscribe [:home/channels]))))
                :style (when @opened? {:border-bottom "none"
                                       :border-radius "5px 5px 0px 0px"})
                :on-key-press
                (fn [e]
                  (when (and (= "Enter" (.-key e))
                             (seq @filtered-chans))
                    (let [chan (first @filtered-chans)]
                      (rf/dispatch [:home/set-channel (:key chan)])
                      (reset! opened? false)
                      (reset! search-val (:text chan)))))
                :on-change
                (fn [e]
                  (let [v (-> e .-target .-value)]
                    (when-not @opened?
                      (reset! opened? true))
                    (reset! search-val v)
                    (reset! filtered-chans
                            (filter #(s/includes?
                                      (s/lower-case (:text %))
                                      (s/lower-case v))
                                    @(rf/subscribe [:home/channels])))))}]
       (when @opened?
         [:div.results
          [:div.filters
           [:button
            {:on-click (partial filter-by-type :country)}
            "country"]
           [:button
            {:on-click (partial filter-by-type :admin-lvl-1)}
            "admin-lvl-1"]
           [:button
            {:on-click (partial filter-by-type :admin-lvl-2)}
            "admin-lvl-2"]
           [:button
            {:on-click (partial filter-by-type :locality)}
            "locality"]]
          [:div.items
           (if (seq @filtered-chans)
             (for [chan @filtered-chans]
               ^{:key (:key chan)}
               [:div.item
                {:on-click (fn [e]
                             (rf/dispatch [:home/set-channel (:key chan)])
                             (reset! opened? false)
                             (reset! search-val (:text chan)))}
                [:div.channel-name (:text chan)]
                [:div.channel-type (name (:type chan))]])
             [:div.no-result
              "No Channel Found For The Given Search"])]])])))

(defn sorting-selection []
  (let [set-sort (fn [by]
                   (rf/dispatch [:home/set-sort-by by]))
        make-classes (fn [by]
                       (hash-map
                        :class-name
                        (if (= by @(rf/subscribe [:home/sort-by]))
                          "container selected" "container")))]
    [:div#sorting-selection
     [:div
      (make-classes :date)
      [:div.option
       {:on-click (partial set-sort :date)}
       [:i.fas.fa-calendar-alt]
       [:div.text "Date"]]]
     [:div
      (make-classes :title)
      [:div.option
       {:on-click (partial set-sort :title)}
       [:i.fas.fa-align-justify]
       [:div.text "Title"]]]
     [:div
      (make-classes :distance)
      [:div.option
       {:on-click (partial set-sort :distance)}
       [:i.fas.fa-ruler]
       [:div.text "Distance"]]]
     [:div
      (make-classes :truth)
      [:div.option
       {:on-click (partial set-sort :truth)}
       [:i.fas.fa-check-circle]
       [:div.text "Truths"]]]
     [:div
      (make-classes :lie)
      [:div.option
       {:on-click (partial set-sort :lie)}
       [:i.fas.fa-times-circle]
       [:div.text "Lies"]]]]))

(defn order-selection []
  (let [set-order (fn [by]
                     (rf/dispatch [:home/set-order by]))
        make-classes (fn [by]
                       (hash-map
                        :class-name
                        (if (= by @(rf/subscribe [:home/order]))
                          "container selected" "container")))]
    [:div#order-selection
     [:div
      (make-classes :desc)
      [:div.option
       {:on-click (partial set-order :desc)}
       [:i.fas.fa-sort-alpha-up]]]
     [:div
      (make-classes :asc)
      [:div.option
       {:on-click (partial set-order :asc)}
       [:i.fas.fa-sort-alpha-down]]]]))

(defn chan-selection []
  [:div.chan-selection
   [:div.selection-controls
    [:div#chan-segment.selection-segment
     [:h1 "Get News By Channel"]
     [channel-search]
     [:div.load-stories
      (let [first-loading? @(rf/subscribe [:home/first-loading?])]
        [:button
         {:on-click #(rf/dispatch [:home/first-load-stories])
          :disabled first-loading?}
         (when first-loading?
             [:div.loader-container
              [:div#load-stories-btn.btn-loader]])
         [:div "Load Stories"]])]]
    [:div#sort-segment
     [:h1 "Sorting"]
     [:div#sorting
      [sorting-selection]]
     [:div#ordering
      [order-selection]]]]])

(defn home-page []
  [:div#home
   [chan-selection]
   [chan-contents]])
