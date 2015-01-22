(ns instabot.core
  (:gen-class)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as ring]
            [ring.middleware.params :as ring-params]
            [instabot.insta :as insta]
            [instabot.spaning :as spaning]
            [instabot.views :as views]
            [instabot.media :as media]
            [instabot.users :as users]
            [instabot.logging :as logging]
            [instabot.jobs :as jobs]
            [schejulure.core :as schejulure]
            [throttler.core :refer [throttle-chan throttle-fn fn-throttler]]
            [clojure.tools.logging :as log]
            [instabot.routes :as routes]))

(def app
  (ring-params/wrap-params routes/main-routes))

(defn -main [& args]
  (log/info "main called")
  (schejulure/schedule {:minute (range 0 60 15) :second 0} jobs/run-spaningar-for-hashtags)
  (schejulure/schedule {:minute (range 0 60 19) :second 0} jobs/run-spaningar-for-locations)
  (ring/run-jetty #'app {:port 8080}))
