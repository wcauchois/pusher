(ns pusher.core
  (:gen-class)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.joda-time]
            [clojure.data.json :as json]
            [monger.util :refer [object-id]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.params :refer [wrap-params]]
            [environ.core :refer [env]]
            [ring.util.response :refer :all]
            [clojure.tools.logging :as log]
            [pusher.gcm-client :as gcm-client]
            [pusher.connection :refer :all])
  (:import [java.util.concurrent Executor]
           [org.bson.types ObjectId]
           [org.joda.time DateTime]))

(defn json-resp [coll]
  (-> (response (json/write-str coll))
      (content-type "application/json")))

(defn list-users-handler [request]
  (let [all-users (mc/find-maps @mongo-db "users")
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

(defn register-device-handler [request]
  (let [reg-id (or (get (:params request) "regId")
                   (throw (Exception. "Missing regId parameter")))
        _ (log/infof "Received registration ID: %s" reg-id)
        existing-user (mc/find-one-as-map @mongo-db "users" {:registration_id reg-id})
        new-or-existing-user
          (if existing-user
             (do
               (log/infof "Found existing user: %s" (:_id existing-user))
               existing-user)
             (do
               (log/info "No existing user found, creating a new one")
               (mc/insert-and-return @mongo-db "users" {:registration_id reg-id}))
           )]
    (json-resp {:user_id (.toString (:_id new-or-existing-user))})))

(defn test-send-handler [request]
  (let [user-id (ObjectId. (get (:params request) "userId"))
        user (or (mc/find-map-by-id @mongo-db "users" user-id)
                 (throw (Exception. "User not found")))
        msg (or (get (:params request) "msg") "Did you have lunch today?")
        data {
          :json-data (json/write-str
                        {:question msg
                         :question_id (str (ObjectId.))
                         :options [{:answer "Yes" :code 0}
                                   {:answer "No" :code 1}]})
        }]
     (gcm-client/send data :registration_ids [(:registration_id user)])
     (json-resp {"OK" true})))

(defn record-response-handler [request]
  (let [question-id (get-in request [:params "questionId"])
        answer-code (get-in request [:params "code"])]
    (mc/update @mongo-db "answers"
      {:question-id question-id} {$set {:question-id question-id}
        $push {:answers {:code answer-code :date (DateTime/now)}}
      } {:upsert true})
    (json-resp {"OK" true})))

(defroutes app
  (GET "/" [] "Pusher is an app. For your phone.")
  (GET "/api/listusers" [] list-users-handler)
  (POST "/api/registerdevice" [] register-device-handler)
  (POST "/api/testsend" [] test-send-handler)
  (POST "/api/recordresponse" [] record-response-handler)
  (route/not-found "<h1>Page not found</h1>"))

(def full-app
  (-> app
      wrap-params ; Adds :query-params, :form-params, and :params to each request.
      wrap-with-logger))

(defn -main [& args]
  (mongo-connect!)
  (let [polling-thread (start-polling-thread)]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. (fn []
                 (log/info "Shutting down")
                 (stop-polling-thread polling-thread))))
    (log/info (format "Using port %d" 8001))
    (run-jetty full-app {:port 8001})))

