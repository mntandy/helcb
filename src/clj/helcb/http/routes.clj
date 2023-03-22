(ns helcb.http.routes
  (:require
   [ring.util.http-response :as response]
   [clojure.edn :as edn]
   [helcb.http.middleware :as middleware]))

(defn html-handler [_]
  (response/ok
   (slurp "resources/html/index.html")))

(def routes
  [""
   {:middleware [middleware/wrap-formats]}
   ["/" {:get html-handler}]])
