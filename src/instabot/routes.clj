(ns instabot.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [instabot.insta :as insta]
            [instabot.spaning :as spaning]
            [instabot.views :as views]
            [instabot.media :as media]
            [instabot.users :as users]
            [instabot.logging :as logging]))

(defn next-page [page]
  (+ page 1))

(defn find-param [request key]
  (get-in request [:params key]))

(defn root [request]
  (views/index (media/get-tag-list)))

(defn tags-show [request]
  (let [tagname (find-param request :tagname)
        page (Integer. (find-param request :page))]
    (views/tag tagname
               (media/get-count-by-tag tagname)
               (media/get-by-tag tagname page)
               (next-page page))))

(defn media-show [request]
  (let [id (find-param request :id)]
    (views/media (media/get-by-id id))))

(defn users-show [request]
  (let [id (find-param request :id)]
    (views/user (users/get-by-id id))))

(defn users-media [request]
  (let [id (find-param request :id)]
    (views/user-media (users/get-by-id id) (media/get-media-by-user id))))

(defn location-media [request]
  (let [id (find-param request :id)
        location (spaning/find-one id)
        page (Integer. (find-param request :page))]
    (views/location
     location
     (media/get-count-by-location location)
     (media/get-by-location location page)
     (next-page page))))

(defn spaningar-index [request]
  (views/spaningar (spaning/all)))

(defn spaningar-new [request]
  (views/spaningar-new))

(defn spaningar-create [request]
 (views/spaning (spaning/create (:params request))))

(defn spaningar-destroy [request]
  (let [id (find-param request :id)]
    (views/spaning-deleted (spaning/delete id))))

(defroutes main-routes
  (GET "/"                                  [] root)

  (GET "/tags/:tagname/pages/:page"         [] tags-show)

  (GET "/media/:id"                         [] media-show)
  (GET "/users/:id"                         [] users-show)
  (GET "/users/:id/media"                   [] users-media)

  (GET "/location/:id/media/pages/:page"    [] location-media)

  (GET "/spaningar"                         [] spaningar-index)
  (GET "/spaningar/new"                     [] spaningar-new)
  (POST "/spaningar"                        [] spaningar-create)
  (GET "/spaningar/:id/destroy"             [] spaningar-destroy)

  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))
