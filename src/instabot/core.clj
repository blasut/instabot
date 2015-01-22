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

(def app
  (ring-params/wrap-params main-routes))

(defn -main [& args]
  (log/info "main called")
  (schejulure/schedule {:minute (range 0 60 15) :second 0} jobs/run-spaningar-for-hashtags)
  (schejulure/schedule {:minute (range 0 60 19) :second 0} jobs/run-spaningar-for-locations)
  (ring/run-jetty #'app {:port 8080}))
