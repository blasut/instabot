(ns instabot.media
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all]
            monger.joda-time))

(def coll "media")

(defn get-by-tag [tag]
  (mq/with-collection db coll
    (mq/find {:tags tag})
    (mq/sort (sorted-map :created_time -1))))

(defn get-first-by-tag [tag]
  (first (mq/with-collection db coll
    (mq/find {:tags tag})
    (mq/sort (sorted-map :created_time -1)))))

(defn get-by-id [id]
  (mc/find-one-as-map db coll { :_id id }))

(defn get-tag-list []
  (->> (mq/with-collection db coll 
         (mq/fields [ :tags ]))
       (map #(:tags %))
       (flatten)
       (distinct)
       (sort)))

(defn get-media-by-user [id]
  (mc/find-maps db coll {"user.id" id}))
