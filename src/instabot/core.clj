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
            [schejulure.core :as schejulure]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [throttler.core :refer [throttle-chan throttle-fn fn-throttler]]
            [clojure.tools.logging :as log]))

(defroutes main-routes
  (GET "/" [] (views/index (media/get-tag-list)))
  (GET "/tags/:tagname" [tagname] (views/tag tagname (media/get-by-tag tagname)))
  (POST "/tags" [tagname] (views/tag tagname (media/get-by-tag tagname)))
  (GET "/media/:id" [id] (views/media (media/get-by-id id)))
  (GET "/users/:id" [id] (views/user (users/get-by-id id)))
  (GET "/users/:id/media" [id] (views/user-media (users/get-by-id id) (media/get-media-by-user id)))
  (GET "/spaningar" [] (views/spaningar (spaning/all)))
  (GET "/spaningar/new" [] (views/spaningar-new))
  (POST "/spaningar" req (views/spaning (spaning/create (:params req))))
  (GET "/spaningar/:id/destroy" [id] (views/spaning-deleted (spaning/delete id)))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

;TODO: Add tests and refactor
(defn proper-start-date-for-spaning [spaning]
  (let [media-start-date (:created_date (media/get-first-by-tag (:tagname spaning)))
        spaning-start-date (:start_date spaning)]
    ; First check if media-start-date is falsey
    ; Check if spaning-start-date is falsey
    ; If not, compare them
    (cond
     (nil? media-start-date) spaning-start-date ; because "" is valid response
     (empty? spaning-start-date) media-start-date ; return media-start-date cause it's not nil
     (>= (c/to-long (f/parse (f/formatters :date) spaning-start-date)) (c/to-long media-start-date)) (f/parse (f/formatters :date) spaning-start-date)
     :else media-start-date)))

(defn run-spaningar []
  (log/info "run spaningar")
  (let [spaningar (spaning/all)]
    (dorun (map #(insta/fetch-and-save-a-tag (:tagname %) (proper-start-date-for-spaning %)) spaningar))))

(def app
  (ring-params/wrap-params main-routes))

(defn -main [& args]
  (log/info "main called")
  (schejulure/schedule {:minute (range 0 60 1) :second 0} run-spaningar)
  (ring/run-jetty #'app {:port 8080}))
