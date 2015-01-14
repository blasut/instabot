(ns instabot.media
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all]
            monger.joda-time))

(def coll "media")

(defn get-by-tag
  ([tag] (get-by-tag tag 1))
  ([tag page]
   (mq/with-collection db coll
     (mq/find {:tags tag})
     (mq/paginate :page page :per-page 200)
     (mq/sort (sorted-map :created_time -1)))))

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

(defn delete-all []
  (mc/remove db coll))

(defn delete-by-tag [tagname]
  (mc/remove db coll {:tags tagname}))
