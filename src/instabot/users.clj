(ns instabot.users
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all]))

(def coll "users")

(defn get-by-id [id]
  (mc/find-one-as-map db coll { :_id id }))

