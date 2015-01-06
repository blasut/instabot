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
            [schejulure.core :as schejulure]
            [throttler.core :refer [throttle-chan throttle-fn fn-throttler]]))

(defroutes main-routes
  (GET "/" [] (views/index (media/get-tag-list)))
  (GET "/tag/:tagname" [tagname] (views/tag tagname (media/get-by-tag tagname)))
  (POST "/tag" [tagname] (views/tag tagname (media/get-by-tag tagname)))
  (GET "/media/:id" [id] (views/media (media/get-by-id id)))
  (GET "/users/:id" [id] (views/user (users/get-by-id id)))
  (GET "/users/:id/media" [id] (views/user-media (users/get-by-id id) (media/get-media-by-user id)))
  (GET "/spaningar" [] (views/spaningar (spaning/all)))
  (GET "/spaningar/new" [] (views/spaningar-new))
  (POST "/spaningar" req (views/spaning (spaning/create (:params req))))
  (GET "/spaningar/:id/destroy" [id] (views/spaning-deleted (spaning/delete id)))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))


(defn run-spaningar []
  (println (clj-time.core/now) "run spaningar")
  (let [spaningar (spaning/all)]
    ; TODO: start date is either the time of the last image on this tag, or if no images exist its the spanings start date
    (dorun (map #(insta/fetch-and-save-a-tag (:tagname %) (:start_date %)) spaningar))))

(def app
  (ring-params/wrap-params main-routes))

(defn -main [& args]
  (println "main called")
  (schejulure/schedule {:minute (range 0 60 15) :second 0} run-spaningar)
  (ring/run-jetty #'app {:port 8080}))
