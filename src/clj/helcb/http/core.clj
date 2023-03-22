(ns helcb.http.core
  (:require
   [ring.adapter.jetty :as jetty]
   [helcb.http.handler :refer [handler]]
   [helcb.http.middleware :refer [wrap-nocache]]
   [ring.middleware.reload :refer [wrap-reload]]
   [mount.core :as mount]
   [helcb.config :refer [env]]))

(def app (-> #'handler
             wrap-nocache 
             wrap-reload))

(mount/defstate ^{:on-reload :noop} http-server
  :start (jetty/run-jetty app (env :jetty-opt))
  :stop (.stop http-server))