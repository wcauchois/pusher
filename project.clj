(defproject pusher "1.0.0" 
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.1"]
                 [com.novemberain/monger "2.0.0"]
                 [compojure "1.1.9"]
                 [org.clojure/data.json "0.2.5"]
                 [environ "1.0.0"]
                 [ring.middleware.logger "0.5.0"]
                 [clj-http "1.0.0"]]
  :plugins [[lein-environ "1.0.0"]]
  :main pusher.core
  :aot [pusher.core])
