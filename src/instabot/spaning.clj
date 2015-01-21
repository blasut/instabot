(ns instabot.spaning
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

(defn all []
  (mq/with-collection db "spaningar"
    (mq/find {})
    (mq/sort (sorted-map :start_time -1))))

(defn find-one [id]
  (mc/find-one-as-map db "spaningar" { :_id id }))

(defn hashtags []
  (mc/find-maps db "spaningar" {:type "Hashtag"}))

(defn locations []
  (mc/find-maps db "spaningar" {:type "Location"}))

(defn create [spaning]
  (mc/insert-and-return db "spaningar" (merge {:_id (str (ObjectId.))} spaning)))

(defn delete [id]
  (mc/remove-by-id db "spaningar" id))
