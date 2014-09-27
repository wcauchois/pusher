(ns pusher.model
  (require [schema.core :as s]
           [pusher.schema-ext :as ext]
           [pusher.connection :refer :all]
           [clojure.data.json :as json]
           [clojure.string :refer [split]]
           [clojure.core.match :refer [match]]
           [monger.core :as mg]
           [monger.collection :as mc]
           [monger.operators :refer :all]))

; Convert FooBarBaz to foo-bar-baz
(defn decamelcase
  ([s sep]
     (apply str
       (mapcat (fn [[c not-first]]
                  (if (Character/isUpperCase c)
                    (conj (if not-first [sep] []) (Character/toLowerCase c))
                    [c]))
               (map vector s (cons false (repeat true))))))
   ([s] (decamelcase s \-)))

(defn pluralize [s]
  (if (.endsWith s "s") s (str s "s")))

(defn update-keys [m f & args]
  (reduce (fn [r [k v]] (assoc r (apply f k args) v)) {} m))

(defn collection [m]
  (:collection-name (meta m)))

(defmacro defmodel
  "Given the CamelCase name of the model and a schema for it, defines the schema
  and generates several helper functions including"
  [model-name schema]
  (let [model-name-str (str model-name)
        decamelcased-name (decamelcase model-name-str)
        make-function-name #(symbol (format % decamelcased-name))
        collection-name (pluralize (decamelcase model-name-str \_))]
    `(do
        (def ^{:collection-name ~collection-name} ~model-name ~schema)
        ; validate-model
        (defn ~(make-function-name "validate-%s") [m#]
          (s/validate ~model-name m#))
        ; valid-model?
        (defn ~(make-function-name "valid-%s?") [m#]
          (try (do (s/validate ~model-name m#) true) (catch Throwable t# false)))
        ; find-model
        (defn ~(make-function-name "find-%s") [id#]
          (mc/find-map-by-id @mongo-db ~collection-name {:_id id#}))
        ; save-model
        (defn ~(make-function-name "save-%s") [m# & opts#]
          (s/validate ~model-name m#)
          (apply mc/insert-and-return
            (concat [@mongo-db ~collection-name m#] opts#)))
        ; model->json
        (defn ~(make-function-name "%s->json") [m#]
          (s/validate ~model-name m#)
          (json/write-str m#)))))

(defmodel Question
  {:_id ext/ObjectId
   :query s/Str
   :choices [s/Str]
   :user_id ext/ObjectId})

(def RecurrenceRule
  (let [single-rule
          (s/either
            [s/Int]
            {:from s/Int :to s/Int}
            s/Bool)]
  (update-keys
    {:days single-rule
     :hours single-rule
     :minutes single-rule}
    s/optional-key)))

(defmodel QuestionSchedule
  {:_id ext/ObjectId
   :question_id ext/ObjectId
   :active s/Bool
   :next_run (s/maybe ext/DateTime)
   :recurrence_rule RecurrenceRule})

(defmodel User
  {:_id ext/ObjectId
   (s/optional-key :registration_id) s/Str})

(defmodel Answers
  {:_id ext/ObjectId
   :question_id ext/ObjectId
   :entries [{:ts ext/DateTime :result s/Int}]})

(defn parse-recurrence-rule
  "Parse a cron-style recurrence like '1,2 * *' into a map. Note only
  days, hours, and minutes are supported so there should only be 3 parts."
  [r]
  (defn parse-part [s]
    (cond
      (re-matches #"[0-9](,[0-9])*" s)
        (apply vector (map #(Integer/parseInt %) (split s #",")))
      (re-matches #"[0-9]-[0-9]" s)
        (let [[from to] (split s #"-")] {:from from :to to})
      (= "*" s) true
      :else (throw (Throwable. "Invalid recurrence pattern"))))
  (match (split r #" +")
    [days hours minutes]
      {:days (parse-part days)
       :hours (parse-part hours)
       :minutes (parse-part minutes)}
    :else (throw (Throwable. "Not enough parts for recurrence pattern"))))

; TODO base off node-schedule?
(defn calculate-next-run-time [schedule now]
  nil)

