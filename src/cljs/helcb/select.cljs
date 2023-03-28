(ns helcb.select
  (:require 
   [reagent.core :as r]
   [helcb.state :as state]
   [helcb.http :as http]
   [helcb.import.state :as import.state]
   [helcb.language :as language]
   [helcb.explore.state :as explore.state]))

(defn element-width [s]
  (. (.getElementById js/document s) -offsetWidth))

(defn on-main-button-click! []
  (if (state/is-initial)
    (do 
      (http/download-initial-data @state/selected)
      (state/update-state! @state/selected))
    (do (state/reset-to-initial!)
        (import.state/reset-to-initial!)
        (explore.state/reset-to-initial!))))

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