(ns pusher.core
  (:gen-class)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.data.json :as json]
            [monger.util :refer [object-id]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.logger :as logger]
            [clojure.tools.logging :as log])
  (:import [java.util.concurrent Executor]))

(def ^:dynamic *mongo-conn* nil)
(def ^:dynamic *mongo-db* nil)

(defn json-resp [coll]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str coll)})

(defn list-users-handler [request]
  (let [all-users (mc/find-maps *mongo-db* "users")
        rendered-users
          (map (fn [u] (update-in u [:_id] #(.toString %))) all-users)]
    (json-resp {:users rendered-users})))

(defn start-polling-thread []
  (let [exit-atom (atom false)]
    {:exit exit-atom
     :thread
       (doto
         (Thread.
           (fn []
             (try (Thread/sleep 1000) (catch InterruptedException e nil))
             (if (not @exit-atom) (recur))))
         (.start))
    }))

(defn stop-polling-thread [t]
  (log/info "Shutting down polling thread")
  (reset! (:exit t) true)
  (doto (:thread t) (.interrupt) (.join)))

(defroutes app
  (GET "/" [] "Hello")
  (GET "/api/v1/listusers" [] list-users-handler)
  (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  ; Connect to Mongo instance on localhost
  (alter-var-root (var *mongo-conn*) (fn [_] (mg/connect)))
  (alter-var-root (var *mongo-db*) (fn [_] (mg/get-db *mongo-conn* "pusher")))
  (let [polling-thread (start-polling-thread)]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. (fn []
                 (println "Shutting down")
                 (stop-polling-thread polling-thread))))
    (run-jetty (logger/wrap-with-logger app) {:port 8080})))

