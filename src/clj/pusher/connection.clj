(ns pusher.connection
  (require [monger.core :as mg]))

(def mongo-conn (atom nil))
(def mongo-db (atom nil))

(defn mongo-connect! []
  ; Connects to Mongo instance on localhost
  (reset! mongo-conn (mg/connect))
  (reset! mongo-db (mg/get-db @mongo-conn "pusher")))
