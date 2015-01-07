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
            [throttler.core :refer [throttle-chan throttle-fn fn-throttler]]
            monger.joda-time
            [clojure.tools.logging :as log]
            [clj-time.format :as f])
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
  (log/info "get media blob" tagname)
  (walk/keywordize-keys (second (first (conj {} ; to get the kind of map we want
                 (get-tagged-medias :oauth *creds* :params {:tag_name tagname}))))))

(defn get-by-pagination-url [media]
  (log/info "get by pagination url")
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

(def df (f/formatters :date-hour-minute-second-ms))

(defn use-correct-time-zone [long]
  (->> (t/from-time-zone (tc/from-long long) (t/time-zone-for-offset -1))
       (f/unparse df)
       (tc/to-long)))

(defn fix-create-time-string [image]
  ; Manually adding three '0' to the end of the created time string because not correct epoch format
  ; use: clojure.tools.reader.edn/read-string instead
  (read-string (str (:created_time image) "000")))

(defn within-time-range [media stop-date]
  (filter (fn [image]
            ; TODO: right now we are getting the last media as well, because of the "="
            (> (use-correct-time-zone (fix-create-time-string image))
                         stop-date )) media))

(defn fix-date [date]
  (let [default-tl (use-correct-time-zone (tc/to-long (t/epoch)))
        tl (use-correct-time-zone (tc/to-long date))]
    (cond
     (= "" date) default-tl
     :else tl)))
    

;; Good test hashtag: #nailsgram

(defn get-all-tagged-media 
  "This function takes a tagname to search for at instagram and an optional date (DateTime) for when to stop.
   The stop-date defaults to start of epoch time.
   If no DateTime is provided the function returns when there are no more media.
   If there is no more media before the date, the function returns." 
  ([tagname] (get-all-tagged-media tagname (t/epoch)))
  ([tagname stop-date]
   (log/info "get all tagged media")
   (log/info tagname)
   (let [stop-date (fix-date stop-date)]
     (loop [result []
            media (slow-get-media-blob tagname)]
       (let [parsed-media (parse-content media)]
         (log/info "number of parsedmedia in timerange: " (count (within-time-range parsed-media stop-date)))
         (log/info "pagination " (not (pagination? media)))
         (log/info "stop date " (<= (count (within-time-range parsed-media stop-date)) 19))
         (if (or (not (pagination? media))
                 (<= (count (within-time-range parsed-media stop-date)) 19))
           ; if the range is 19 or less that means at least one is out of range and we can return.
           (flatten (conj result (within-time-range parsed-media stop-date)))
           (recur 
            (conj result (within-time-range parsed-media stop-date))
            (slow-get-by-pagination-url media))))))))


(defn get-user-data [id]
  (log/info "get user data for id: " id)
  (get-user :oauth *creds* :params {:user_id id}))

(def slow-get-user-data
  (api-throttler get-user-data))

(defn parse-user-data [blob]
  (log/info "parse user data")
  (get (get blob :body) "data"))

(defn get-all-users-from-media [media]
  (let [ids (distinct (map #(get-in % [:user :id]) media))]
    (map parse-user-data (map slow-get-user-data ids))))

(defn save-users-and-media
  "This function takes a blob of media and a blob of users, and saves them."
  [media users]
  (log/info "save users and media")
  (let [users (map #(merge % {:_id (get % "id")}) users)
        media (->> (map #(merge % {:_id (get % :id)}) media)
                   (map #(merge % {:created_date (tc/from-long (fix-create-time-string %))})))]
    ; We have to "upsert" the users because they might already be existing.
    (println (clj-time.core/now) "finished mapping over data")
    (dorun (map #(mc/update db "users" {:_id (:_id %)} % {:upsert true}) users))
    (dorun (map #(mc/update db "media" {:_id (:_id %)} % {:upsert true}) media))))

(defn fetch-and-save-a-tag [tag stop-date]
  (log/info "face and save tag:" tag "with stop-date: " stop-date)
  (let [media (get-all-tagged-media tag stop-date)
        users (get-all-users-from-media media)]
    (save-users-and-media media users)))

