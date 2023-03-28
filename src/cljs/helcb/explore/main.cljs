(ns helcb.explore.main
  (:require 
   [helcb.explore.table :refer [table get-more-rows]]
   [helcb.state :as state]))

(defn explorer []
  (when (state/is-exploring @state/display)
    [:div
     [:div.columns.is-centered
      [table]]
     [:div.columns.is-centered
      [get-more-rows]]]))