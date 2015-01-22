(ns instabot.jobs
  (:require
            [instabot.insta :as insta]
            [instabot.spaning :as spaning]
            [instabot.media :as media]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.tools.logging :as log]))

;TODO: Add tests and refactor
(defn proper-start-date-for-spaning [spaning media]
  (let [media-start-date (:created_date media)
        spaning-start-date (:start_date spaning)]
    ; First check if media-start-date is falsey
    ; Check if spaning-start-date is falsey
    ; If not, compare them
    (cond
     (nil? media-start-date) spaning-start-date ; because "" is valid response
     (empty? spaning-start-date) media-start-date ; return media-start-date cause it's not nil
     (>= (c/to-long (f/parse (f/formatters :date) spaning-start-date)) (c/to-long media-start-date)) (f/parse (f/formatters :date) spaning-start-date)
     :else media-start-date)))

(defn run-spaningar-for-locations []
  (log/info "run locations spaningar")
  (let [spaningar (spaning/locations)]
    (dorun (map #(insta/fetch-and-save-a-location {:lat (:lat %)
                                                   :lng (:lng %)
                                                   :min_ts (proper-start-date-for-spaning % (media/get-first-by-location %))
                                                   :dst (:dst %)}) spaningar))))
(defn run-spaningar-for-hashtags []
  (log/info "run hashtag spaningar")
  (let [spaningar (spaning/hashtags)]
    (dorun (map #(insta/fetch-and-save-a-tag
                  (:tagname %)
                  (proper-start-date-for-spaning % (media/get-first-by-tag (:tagname %)))) spaningar))))
