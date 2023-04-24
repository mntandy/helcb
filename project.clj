(defproject helcb "0.1.0-SNAPSHOT"
  :description "Helsinki City Bike App"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.csv "1.0.1"]
                 [org.clojure/clojurescript "1.10.764" :scope "provided"]
                 [funcool/struct "1.4.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.11"]
                 [metosin/maailma "1.1.0"]
                 [ring "1.8.2"]
                 [seancorfield/next.jdbc "1.1.613"]
                 [conman "0.9.5"]
                 [mount "0.1.16"]
                 [org.postgresql/postgresql "42.2.8"]
                 [com.layerware/hugsql "0.5.3"]
                 [reagent "1.0.0"]
                 [hiccup "1.0.5"]
                 [cljs-ajax "0.8.1"]]

  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :main ^:skip-aot helcb.core
  :target-path "target/%s"

  :plugins [[lein-cljsbuild "1.1.8"]]

  :cljsbuild
  {:builds
   {:app {:source-paths ["src/cljs" "src/cljc"]
          :compiler {:output-to "target/cljsbuild/public/js/app.js"
                     :output-dir "target/cljsbuild/public/js/out"
                     :main "helcb.main"
                     :asset-path "/js/out"
                     :optimizations :none
                     :source-map true
                     :pretty-print true}}}}

  :clean-targets
  ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
