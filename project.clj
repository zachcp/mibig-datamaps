(defproject mibig-datamaps "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [datamaps "0.1.1-SNAPSHOT"]
                 [cheshire "5.7.1"]]
  :main ^:skip-aot mibig-datamaps.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
