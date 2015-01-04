(ns instabot.db
  (:require [monger.core :as mg])
  (:import
    ([com.mongodb MongoOptions ServerAddress])))

(def conn (mg/connect))
(def db   (mg/get-db conn "monger-test"))
