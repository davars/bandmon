(defproject bandmon "1.0.0-SNAPSHOT"
  :description "Simple script for running and recording the results of various network connection tests."
  :dependencies [[org.clojure/clojure "1.2.0-beta1"]
                 [org.clojure/clojure-contrib "1.2.0-beta1"]
		 [postgresql/postgresql "8.4-701.jdbc4"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :namespaces [bandmon.core]
  :main bandmon.core)
