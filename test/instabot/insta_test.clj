(ns instabot.insta-test
  (:require [midje.sweet :refer :all]
            [instabot.insta :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))


(fact "fix date should parse string or dates and return a sane default"
      (fix-date "") => 3600000)

(fact "use correct time zone should offset the timezone by one hour ahead"
      (use-correct-time-zone (t/date-time 2014 01 01 01 01 01)) => (c/to-long (t/date-time 2014 01 01 02 01 01))
      )
