(ns instabot.users
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all]))

(defn get-by-id [id]
  (mc/find-one-as-map db "users" { :_id id }))

(defn get-media-by-user [id]
  (mc/find-maps db "media" {"user.id" id}))

