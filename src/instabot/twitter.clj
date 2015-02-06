(ns instabot.twitter
  (:require [clojure.data.json :as json]
            [http.async.client :as ac]
            [environ.core :refer [env]]
            [clj-http.client :as client])
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful]))

(def app-consumer-key         (:app-consumer-key env))
(def app-consumer-secret      (:app-consumer-secret env))
(def user-access-token        (:user-access-token env))
(def user-access-token-secret (:user-access-token-secret env))


(def my-creds (make-oauth-creds app-consumer-key
                                app-consumer-secret
                                user-access-token
                                user-access-token-secret))

(defn parsed-tweets [blob]
  (:statuses (:body blob)))

(defn get-new-tweets [params tweets]
  (let [max-id (subs (get-in tweets [:body :search_metadata :next_results]) 8 26)]
    (println "proper max id" max-id)
    (search-tweets :oauth-creds my-creds
                   :params {:q (:q params)
                            ;:since_id (get-in tweets [:body :search_metadata :since_id])
                            :max_id max-id})))

(defn get-all-tweets-since [params start-date]
  (loop [result []
         tweets (search-tweets :oauth-creds my-creds :params params)
         times 0]
    (println "getting the tweets")
    (Thread/sleep (* 30 1000))
    (println "start date" start-date)
    (println "first:" (:id (first (parsed-tweets tweets))))
    (println "last: " (:id (last (parsed-tweets tweets))))
    (println "count:" (count (parsed-tweets tweets)))
    (println "times:" times)
    (println "result count:" (count result))
    (println "since id" (get-in tweets [:body :search_metadata :since_id]))
    (println "max id" (get-in tweets [:body :search_metadata :max_id]))
    (println "metadata" (get-in tweets [:body :search_metadata]))
    (println (map :id (parsed-tweets tweets)))
    (println "\n")
    (if (= times 5)
      (flatten result)
      (recur
       (conj result (parsed-tweets tweets))
       (get-new-tweets params tweets)
       (+ 1 times)))))


;   (let [stop-date (fix-date stop-date)]
;     (loop [result []
;            media (slow-get-media-blob tagname)]
;       (let [parsed-media (parse-content media)]
;         (log/info "number of media for this fetch" (count parsed-media))
;         (log/info "number of parsedmedia in timerange: " (count (within-time-range parsed-media stop-date)))
;         (log/info "pagination " (not (pagination? media)))
;         (log/info "last medias time: " (tc/from-long (use-correct-time-zone (fix-create-time-string (last parsed-media)))))
;
;         (if (or (not (pagination? media))
;                 (<= (count (within-time-range parsed-media stop-date)) 1))
;           (flatten (conj result (within-time-range parsed-media stop-date)))
;           (recur 
;            (conj result parsed-media)
;            (slow-get-by-pagination-url media))))))))


