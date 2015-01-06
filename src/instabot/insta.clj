(ns instabot.insta
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.walk :as walk]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [instabot.db :refer :all]
            [throttler.core :refer [throttle-chan throttle-fn fn-throttler]])
  (:use
    instagram.oauth
    instagram.callbacks
    instagram.callbacks.handlers
    instagram.api.endpoint)
  (:import
    (instagram.callbacks.protocols SyncSingleCallback)))

(def api-throttler (fn-throttler 5000 :hour))

(def client-id (:client-id env))
(def client-secret (:client-secret env))
(def redirect-uri (:redirect-uri env))

(def ^:dynamic *creds* (make-oauth-creds client-id
                                         client-secret
                                         redirect-uri))
(defn get-media-blob [tagname]
  (println (clj-time.core/now) "get media blob")
  (walk/keywordize-keys (second (first (conj {} ; to get the kind of map we want
                 (get-tagged-medias :oauth *creds* :params {:tag_name tagname}))))))

(defn get-by-pagination-url [media]
  (println (clj-time.core/now) "get by pagination url")
  (let [url (get (get media :pagination) :next_url)]
    (walk/keywordize-keys (get (client/get url {:as :json}) :body))))

(def slow-get-by-pagination-url
  (api-throttler get-by-pagination-url))

(def slow-get-media-blob
  (api-throttler get-media-blob))

(defn pagination? [media]
  (not (nil? (get (get media :pagination) :next_url))))

(defn parse-content [media]
  (get media :data))

(defn within-time-range [media stop-date]
  ; Manually adding three '0' to the end of the created time string because not correct epoch format
  ; use: clojure.tools.reader.edn/read-string instead
  (filter (fn [image] (> (read-string (str (:created_time image) "000")) 
                         stop-date )) media))

(defn fix-date [date]
  (let [tl (tc/to-long date)]
    (if tl
      tl
      (tc/to-long (t/epoch)))))

;; Good test hashtag: #nailsgram

(defn get-all-tagged-media 
  "This function takes a tagname to search for at instagram and an optional date (DateTime) for when to stop.
   The stop-date defaults to start of epoch time.
   If no DateTime is provided the function returns when there are no more media.
   If there is no more media before the date, the function returns." 
  ([tagname] (get-all-tagged-media tagname (t/epoch)))
  ([tagname stop-date]
   (println "get all tagged media")
   (println tagname)
   (let [stop-date (fix-date stop-date)]
     (loop [result []
            media (slow-get-media-blob tagname)]
       (let [parsed-media (parse-content media)]
         (if (or (not (pagination? media))
                 (= 0 (count (within-time-range parsed-media stop-date)))) ; not 0.
           (flatten (conj result (within-time-range parsed-media stop-date)))
           (recur 
            (conj result parsed-media)
            (slow-get-by-pagination-url media))))))))


(defn get-user-data [id]
  (println "Get user data for id:" (str id))
  (get-user :oauth *creds* :params {:user_id id}))

(def slow-get-user-data
  (api-throttler get-user-data))

(defn parse-user-data [blob]
  (println "parse user data")
  (get (get blob :body) "data"))

(defn get-all-users-from-media [media]
  (let [ids (map #(get-in % [:user :id]) media)]
    (map parse-user-data (map slow-get-user-data ids))))

(defn save-users-and-media
  "This function takes a blob of media and a blob of users, and saves them."
  [media users]
  (println (clj-time.core/now) "save users and media")
  (let [users (map #(merge % {:_id (get % "id")}) users)
        media (map #(merge % {:_id (get % :id)}) media)]
    ; We have to "upsert" the users because they might already be existing.
    (println (clj-time.core/now) "finished mapping over data")
    (dorun (map #(mc/update db "users" {:_id (:_id %)} % {:upsert true}) users))
    (dorun (map #(mc/update db "media" {:_id (:_id %)} % {:upsert true}) media))))

(defn fetch-and-save-a-tag [tag stop-date]
  (println (clj-time.core/now) "fetch and save a tag")
  (println tag stop-date)
  (let [media (get-all-tagged-media tag stop-date)
        users (get-all-users-from-media media)]
    (save-users-and-media media users)))

(defn get-tag-list []
  (distinct (flatten (map #(:tags %) (mq/with-collection db "media" (mq/fields [ :tags ]))))))
  
