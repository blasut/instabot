(ns instabot.media
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all]))

(defn get-by-tag [tag]
  (mq/with-collection db "media"
  (mq/find {:tags tag})
  (mq/sort (sorted-map :created_time -1))))

(defn get-by-id [id]
  (mc/find-one-as-map db "media" { :_id id }))

(defn get-tag-list []
  (distinct (flatten (map #(:tags %) (mq/with-collection db "media" (mq/fields [ :tags ]))))))
