(ns helcb.explore
  (:require 
   [helcb.table :refer [table get-more-rows]]
   [helcb.state :as state]))

(defn explorer []
  (when (state/is-exploring)
    [:div
     [:div.columns.is-centered
      [table]]
     [:div.columns.is-centered
      [get-more-rows]]]))