(ns helcb.notification
  (:require
   [helcb.state :as state :refer [msg]]))

(defn notification []
  (when (some? @msg)
    [:div.columns.is-centered.m-3
     [(if (:error @msg) :div.notification.is-danger :div.notification.is-success)
      [:button.delete {:on-click #(state/close-msg!)}]
      (:text @msg)]]))