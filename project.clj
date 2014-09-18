(defproject pusher "1.0.0" 
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.1"]
                 [com.novemberain/monger "2.0.0"]
                 [compojure "1.1.9"]
                 [org.clojure/data.json "0.2.5"]]
  :main pusher.core
  :aot [pusher.core])
