(ns instabot.spaning
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

(def coll "spaningar")

(defn all []
  (mq/with-collection db coll
    (mq/find {})
    (mq/sort (sorted-map :start_time -1))))

(defn find-one [id]
  (mc/find-one-as-map db coll { :_id id }))

(defn hashtags []
  (mc/find-maps db coll {:type "Hashtag"}))

(defn locations []
  (mc/find-maps db coll {:type "Location"}))

(defn create [spaning]
  (mc/insert-and-return db coll (merge {:_id (str (ObjectId.))} spaning)))

(defn delete [id]
  (mc/remove-by-id db coll id))
