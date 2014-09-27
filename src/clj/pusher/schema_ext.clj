(ns pusher.schema-ext
  (require [schema.core :as s]))

(def ObjectId (s/pred #(instance? org.bson.types.ObjectId %) 'ObjectId))

(def DateTime (s/pred #(instance? org.joda.time.DateTime %) 'DateTime))

