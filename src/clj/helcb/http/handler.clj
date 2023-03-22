(ns helcb.http.handler
  (:require
   [ring.util.http-response :as response]
   [reitit.ring :as ring]
   [helcb.http.routes :refer [routes]]))

(def handler (ring/ring-handler
              (ring/router routes)
              (ring/routes
               (ring/create-resource-handler {:path "/" :root "/public"}))
              (ring/create-default-handler
               {:not-found
                (constantly (response/not-found "404 - Page not found"))
                :method-not-allowed
                (constantly (response/method-not-allowed "405 - Not allowed"))
                :not-acceptable
                (constantly (response/not-acceptable "406 - Not acceptable"))})))