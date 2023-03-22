(ns helcb.config
  (:require [maailma.core :as m]))

(def env (m/build-config (m/file "./config-local.edn")))