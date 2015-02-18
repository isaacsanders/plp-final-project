(defproject plp-final-project "0.1.0-SNAPSHOT"
  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2816"]
                 [cljs-ajax "0.3.10"]
                 [compojure "1.1.6"]
                 [domina "1.0.3-SNAPSHOT"]
                 [hiccups "0.2.0"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-ring "0.8.8"]]

  :ring {:handler plp-final-project.core/handler}

  :cljsbuild {:builds
              {:prod {;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/final.js"

                           ;; advanced optimization
                           :optimizations :advanced

                           ;; no need prettyfication
                           :pretty-print false}}}})
