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
  (GET "/tags/:tagname/page/:page" [tagname page] (views/tag tagname (media/get-by-tag tagname (read-string page))))
  (POST "/tags" [tagname] (views/tag tagname (media/get-by-tag tagname)))
  (GET "/media/:id" [id] (views/media (media/get-by-id id)))
  (GET "/users/:id" [id] (views/user (users/get-by-id id)))
  (GET "/users/:id/media" [id] (views/user-media (users/get-by-id id) (media/get-media-by-user id)))

  (GET "/location/:id/media" [id]
       (let [location (spaning/find-one id)]
         (views/location (media/get-by-location location))))

  ; SPANINGAR
  (GET "/spaningar" [] (views/spaningar (spaning/all)))
  (GET "/spaningar/new" [] (views/spaningar-new))
  (POST "/spaningar" req (views/spaning (spaning/create (:params req))))
  (GET "/spaningar/:id/destroy" [id] (views/spaning-deleted (spaning/delete id)))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

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

(def app
  (ring-params/wrap-params main-routes))

(defn -main [& args]
  (log/info "main called")
  (schejulure/schedule {:minute (range 0 60 15) :second 0} run-spaningar-for-hashtags)
  (schejulure/schedule {:minute (range 0 60 19) :second 0} run-spaningar-for-locations)
  (ring/run-jetty #'app {:port 8080}))
