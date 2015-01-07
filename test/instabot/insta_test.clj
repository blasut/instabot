(ns instabot.insta-test
  (:require [midje.sweet :refer :all]
            [instabot.insta :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

;; Helper functions

(defn remove-trailing-zeros
  "Remove trailing zeros from date string to match instagram api"
  [date]
  (let [date (str date)]
    (subs date 0 (- (count date) 3))))

(defn create-date-string [date]
  (remove-trailing-zeros (c/to-long date)))


(fact "fix date should parse string or dates and return a sane default"
      (fix-date "") => 3600000)

(fact "use correct time zone should offset the timezone by one hour ahead"
      (use-correct-time-zone (t/date-time 2014 01 01 01 01 01)) => (c/to-long (t/date-time 2014 01 01 02 01 01)))

(fact "fix create time string should fix the created-time string to a proper epoch format"
      (fix-create-time-string {:created_time "123"}) => 123000)

(fact "pagination? checks if there exist pagination links"
      (pagination? {:pagination {:next_url "hej"}}) => true
      (pagination? {}) => false)

(fact "within-time-range filters media for images"
      (fact "within-time-range filters for images which have been created after the stop-date"
            (let [test-media [{:name "first"    :created_time (create-date-string (t/date-time 2014 01 02 01 01 01))}
                              {:name "second"   :created_time (create-date-string (t/date-time 2014 01 02 05 01 01))}
                              {:name "included" :created_time (create-date-string (t/date-time 2014 01 02 15 01 01))}]]
              (within-time-range test-media (c/to-long (t/date-time 2014 01 02 10 01 01))) => [{:created_time "1388674861",
                                                                                                :name "included"}]))
      (fact "within-time-range do not filter images which have been created at the exact stop-date"
            (let [test-media [{:name "first"  :created_time (create-date-string (t/date-time 2014 01 02 01 01 01))}
                              {:name "second" :created_time (create-date-string (t/date-time 2014 01 02 05 01 01))}
                              {:name "third"  :created_time (create-date-string (t/date-time 2014 01 02 10 01 01))}]]
              ; one hour ahead because of instagram API.
              (within-time-range test-media (c/to-long (t/date-time 2014 01 02 11 01 01))) => []))) 

(future-fact "get-all-users-from-media"
      (fact "it should map all the ids from media")
      (fact "it should remove duplicates")
      (fact "it should get the parsed-user-data for each"))
             
(future-fact "get-all-tagged-media"
      (fact "it should stop if the media in range is less than or equal to 19")
      (fact "it should stop if there is no more pagination links")
      (fact "it should be callable without and stop-date")
      (fact "it should only return the media in range")
      (fact "it should flatten the results")
      (fact "it should recur if there is pagination and out of range media")
      (fact "it should get paginated pages"))
