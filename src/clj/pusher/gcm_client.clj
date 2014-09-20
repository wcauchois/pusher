(ns pusher.gcm-client
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]))

; Documentation for the GCM server API:
; http://developer.android.com/google/gcm/server.html

(def default-send-options {
  ; Can be used to collapse a group of like-messages when the device comes back online.
  :collapse-key nil
  ; How long the message should be kept in GCM storage if the device is offline.
  :time-to-live nil
  ; Indicates GCM should wait until the device becomes active before sending the message.
  :delay-while-idle nil
})

(def google-api-server "android.googleapis.com")

(defn make-json-request [endpoint payload]
  (let [json-string
          (json/write-str payload :key-fn #(-> % name (.replace "-" "_")))
        full-url (format "http://%s%s" google-api-server endpoint)]
    (log/infof "Sending POST request to %s" full-url)
    (log/infof "Payload: %s" json-string)
    (let [resp
            (http/post full-url {
               :body json-string
               :headers {"Authorization" (format "key=%s" (env :google-api-key))}
               :content-type :json
             })
          json-body (json/read-str (:body resp))]
       (log/infof "Received %d response from server" (:status resp))
       (log/infof "Body: %s" {:body resp})
       (if (not (= (:status resp) 200))
         (throw (Exception. "Failed to make GCM request, non-200 status code")))
       (if (= (get json-body "failure") 1)
         (let [error-message (get-in json-body ["results" 0 "error"])]
          (throw (Exception. (format "Failed to make GCM request: %s" error-message)))))
       nil)))

; At a minimum, require :registration-ids option (list of IDs to send to).
(defn dosend [data & options]
  (let [options-map (apply hash-map options)
        combined-options
          (filter (fn [[_ v]] (not (nil? v)))
            (into default-send-options options-map))
        payload (into {:data data} combined-options)]
    (make-json-request "/gcm/send" payload)))

