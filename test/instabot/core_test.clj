(ns instabot.core-test
  (:require [midje.sweet :refer :all]
            [instabot.core :refer :all]))

(future-fact "proper-start-date-for-spaning"
        (fact "it should return the spaning-start date if media-start-date is nil")
        (fact "it should return the media-start-date if spaning start date is empty")
        (fact "it should return the spaning-start-date if that is sooner than media-start-date")
        (fact "it should return the media-start-date if that is sooner than spaning-start-date"))
