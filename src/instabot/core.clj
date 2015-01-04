(ns instabot.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as ring]
            [ring.middleware.params :as ring-params]
            [instabot.insta :as insta]
            [instabot.spaning :as spaning]
            [instabot.views :as views]))

(defroutes main-routes
  (GET "/" [] (views/index))
  (GET "/tag/:tagname" [tagname] (views/tag tagname (insta/get-by-tag tagname)))
  (POST "/tag" [tagname] (views/tag tagname (insta/get-by-tag tagname)))
  (GET "/media/:id" [id] (views/media (insta/get-media-by-id id)))
  (GET "/user/:id" [id] (views/user (insta/get-user-by-id id)))
  (GET "/user/:id/media" [id] (views/user-media (insta/get-user-by-id id) (insta/get-media-by-user-id id)))
  (GET "/spaningar" [] (views/spaningar (spaning/all)))
  (GET "/spaningar/new" [] (views/spaningar-new))
  (POST "/spaningar" req (views/spaning (spaning/create (:params req))))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (ring-params/wrap-params main-routes))

(defn -main []
  (ring/run-jetty #'app {:port 8080}))

