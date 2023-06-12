(ns helcb.menu  
  (:require 
   [reagent.core :as r]
   [helcb.state :as state]
   [helcb.importer :as importer]
   [helcb.stations :as stations]
   [helcb.explorer :as explorer]
   [helcb.leaflet-utils :as leaflet]))

(def active-menu (r/atom false))

(defn toggle-active-menu []
  (reset! active-menu (not @active-menu)))

(defn reset-everything []
  (state/reset-to-initial!)
  (importer/reset-to-initial!)
  (stations/reset-to-initial!)
  (explorer/reset-to-initial!)
  (leaflet/hide-every-station))

(defn set-explore! [state]
  (state/update-state! state)
  (explorer/initialise))

(defn menu []
[:div.columns.is-centered.is-desktop.m-5
 [:nav.navbar {:id "navbar" :role "navigation" :aria-label "main navigation"}
  [:div.navbar-brand
   [:a.navbar-item {:on-click #(reset-everything)}
    [:img {:src "svg/bicycle_with_basket.svg"}]]
  [:a.navbar-burger {:role "button" :aria-label "menu" :aria-expanded "false" 
                     :data-target "navbarHelcb" :on-click toggle-active-menu}
   [:span {:aria-hidden "true"}]
   [:span {:aria-hidden "true"}]
   [:span {:aria-hidden "true"}]]]
  [(if @active-menu :div.navbar-menu.is-active :div.navbar-menu) {:style {:overflow "visible"} :id "navbarHelcb"}
   [:div.navbar-start {:style {:overflow "visible"}}
    [:a.navbar-item {:on-click #(set-explore! :explore-journeys)} "Search journeys"]
    [:a.navbar-item {:on-click #(set-explore! :explore-stations)} "Search stations"]
    [:div.navbar-item.has-dropdown.is-hoverable
     [:a.navbar-link.is-arrowless "Import"]
     [:div.navbar-dropdown {:style {:overflow "visible"}}
      [:a.navbar-item {:on-click #(state/update-state! :import-journeys)} "Journeys"]
      [:a.navbar-item {:on-click #(state/update-state! :import-stations)} "Stations"]
      [:hr.navbar-divider]
      [:a.navbar-item {:href "mailto:afjellstad@gmail.com"} "Report an issue"]]]]]]])