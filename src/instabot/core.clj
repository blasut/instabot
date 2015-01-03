(ns instabot.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as ring]
            [hiccup.page :as page]
            [hiccup.core :refer [h]]
            [instabot.insta :as insta]))

(defn index [media]
  (page/html5
   [:head
    [:title "Welcome to Instabot"]]
   [:body
    [:div "Hello, and welcome."]
    [:div (map
           (fn [m] [:h2 (h (:link m))])
           media)]]))

(defroutes main-routes
  (GET "/" [] (index (insta/get-by-tag "forhenne")))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main []
  (ring/run-jetty #'main-routes {:port 8080}))
