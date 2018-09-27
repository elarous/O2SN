(set-env!
 :source-paths #{"src/server/main" "src/server/test"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.9.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.apache.logging.log4j/log4j-core "2.11.1"]
                 ;; [org.clojure/test.check "0.10.0-alpha2"]
                 ;; [org.clojure/tools.cli "0.3.6"]
                 [org.clojure/tools.logging "0.4.0"]
                 ;; [org.clojure/tools.namespace "0.2.11"]
                 ;; [org.clojure/tools.trace "0.7.9"]
                 ;; [cider/cider-nrepl "0.15.1"]
                 [manifold "0.1.8"]
                 [byte-streams "0.2.4"]
                 [aleph "0.4.6"]
                 [funcool/struct "1.2.0"]
                 [cljs-ajax "0.7.3"]
                 [samestep/boot-refresh "0.1.0" :scope "test"]
                 [buddy "2.0.0"]
                 [clj-time "0.14.3"]
                 [com.arangodb/arangodb-java-driver "4.3.4"]
                 [com.cognitect/transit-clj "0.8.309"]
                 [com.draines/postal "2.0.2"]
                 [compojure "1.6.0"]
                 [metosin/compojure-api "1.1.12"]
                 [metosin/ring-http-response "0.9.0"]
                 [mount "0.1.12"]
                 [org.clojure/data.json "0.2.6"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring-middleware-format "0.7.2"]
                 [ring-webjars "0.2.0"]
                 [ring-cors "0.1.12"]
                 [ring-ttl-session "0.3.1"]
                 [metosin/muuntaja "0.5.0"]])

(require '[samestep.boot-refresh :refer [refresh]])

(deftask build
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (comp
   (aot :namespace #{'o2sn.core})
   (uber)
   (jar :file "project.jar" :main 'o2sn.core)
   (sift :include #{#"project.jar"})
   (target)))

(deftask dev
  "Run the development repl with refreshing"
  []
  (comp
   (repl :server true)
   (watch :exclude [#"\.#.*"])
   (refresh)))

(deftask run
  "Run this project"
  []
  (require 'o2sn.core)
  (fn middleware [next-handler]
    (fn handler [fileset]
      (apply (resolve 'o2sn.core/-main) [])
      (next-handler fileset))))
