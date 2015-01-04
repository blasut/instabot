(ns instabot.spaning
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [instabot.db :refer :all])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))
  
(defn all []
  (mc/find-maps db "spaningar"))

(defn create [spaning]
  (mc/insert-and-return db "spaningar" (merge {:_id (str (ObjectId.))} spaning)))

(defn find [id]
  (mc/find-one-as-map db "spaningar" { :_id id }))

(defn delete [id]
  (mc/remove-by-id db "spaningar" id))
