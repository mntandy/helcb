(ns helcb.select
  (:require 
   [reagent.core :as r]
   [helcb.state :as state]
   [helcb.import-data :as import-data]
   [helcb.language :as language]))

(defn element-width [s]
  (. (.getElementById js/document s) -offsetWidth))

(defn on-main-button-click! []
  (if (state/is-initial)
    (state/adjust-by-selected!)
    (do (state/reset-to-initial!)
        (import-data/reset-to-initial!))))

(defn select []
  (let [width (r/atom 0)]
    (fn []
      (when (= @width 0) (r/after-render #(reset! width (element-width "MainButton"))))
      [:div.columns.m-6
       [:div.column.is-one-third]
       [:div.column.is-one-third
       [:div.field.has-addons
        [:p.control>span.select
         (into
          [:select.select {:value @state/selected
                           :disabled (state/is-not-initial)
                           :id "selector"
                           :on-change #(state/update-selector! (-> % .-target .-value))}]
          (for [k (keys state/selector-options)]
            [:option {:key k :value k} (get state/selector-options k)]))]
        [:p.control>a.button
         {:id "MainButton"
          :style {:min-width @width}
          :on-click #(on-main-button-click!)
          :disabled (:disabled @state/main-button)}
         (:text @state/main-button)]]]
       [:div.column.is-one-third (language/selector)]])))