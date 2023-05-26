(defproject helcb "0.1.0-SNAPSHOT"
  :description "Helsinki City Bike App"
  :url "https://github.com/mntandy/helcb"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.csv "1.0.1"]
                 [org.clojure/clojurescript "1.11.60" :scope "provided"]
                 [com.google.javascript/closure-compiler-unshaded "v20230411"]
                 [funcool/struct "1.4.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.11"]
                 [cprop "0.1.19"]
                 [ring "1.8.2"]
                 [seancorfield/next.jdbc "1.1.613"]
                 [conman "0.9.5"]
                 [mount "0.1.16"]
                 [org.postgresql/postgresql "42.2.8"]
                 [com.layerware/hugsql "0.5.3"]
                 [reagent "1.0.0"]
                 [hiccup "1.0.5"]
                 [cljs-ajax "0.8.1"]
                 [thheller/shadow-cljs "2.23.3"]]

  :source-paths ["src/clj" "src/cljs" "src/cljc" "src/js"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]

  :main ^:skip-aot helcb.core
  :target-path "target/%s"
  :aot [helcb.core]


  :profiles 
  {:cljs {:source-paths ["src/cljs"]
          :dependencies [[thheller/shadow-cljs "2.23.3"]
                         [hiccup "1.0.5"]
                         [cljs-ajax "0.8.1"]
                         [reagent "1.0.0"]]}
   :uberjar {:omit-source true
             :aot :all
             :uberjar-name "helcb.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}
   :dev [:project/dev :profiles/dev]
   :test [:project/dev :project/test :profiles/test]
   
   :project/dev {:jvm-opts ["-Dconf=dev-config.edn"]
                 :source-paths ["env/dev/clj"]
                 :resource-paths ["env/dev/resources"]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
