(ns helcb.menu  
  (:require 
   [helcb.state :as state]
   [helcb.import.state :as import.state]
   [helcb.explore.state :as explore.state]))

(defn reset-everything []
  (state/reset-to-initial!)
  (import.state/reset-to-initial!)
  (explore.state/reset-to-initial!))

(defn set-explore! [state]
  (state/update-state! state)
  (explore.state/reset-to-initial!)
  (explore.state/download-initial-explorer-data))

(defn menu []
[:div.columns.is-centered.m-5
 [:nav.navbar {:id "navbar" :role "navigation" :aria-label "main navigation"}
  [:div.navbar-brand
   [:a.navbar-item {:on-click #(reset-everything)}
    [:img {:src "svg/bicycle_with_basket.svg"}]]]
  [:a.navbar-burger {:role "button" :aria-label "menu" :aria-expanded "false" :data-target "navbarHelcb"}
   [:span {:aria-hidden "true"}]
   [:span {:aria-hidden "true"}]
   [:span {:aria-hidden "true"}]]
  [:div.navbar-menu {:id "navbarHelcb"}
   [:div.navbar-start
    [:a.navbar-item {:on-click #(set-explore! :explore-journeys)} "Search journeys"]
    [:a.navbar-item {:on-click #(set-explore! :explore-stations)} "Search stations"]
    [:div.navbar-item.has-dropdown.is-hoverable
     [:a.navbar-link.is-arrowless "Import"]
     [:div.navbar-dropdown {:style {:overflow "visible"}}
      [:a.navbar-item {:on-click #(state/update-state! :import-journeys)} "Journeys"]
      [:a.navbar-item {:on-click #(state/update-state! :import-stations)} "Stations"]
      [:hr.navbar-divider]
      [:a.navbar-item {:href "mailto:afjellstad@gmail.com"} "Report an issue"]]]]]]])