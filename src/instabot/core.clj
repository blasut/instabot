(ns instabot.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as ring]
            [ring.middleware.params :as ring-params]
            [instabot.insta :as insta]
            [instabot.views :as views]))
;
; TODO:
; SPANINGAR: 
;
; GET /spaningar
; Show all 'spaningar'
;
; GET /spaningar/:id
; Show the 'spaning', including edit button
; 
; GET /spaningar/new
; Show the form for creating a new spaning.
; 
; POST /spaningar/
; Create a new spaning
;
; GET /spaningar/:id/edit
; Show the edit form for the 'spaning'
;
; PUT /spaningar/:id
; Save the updated spaning
;
; DELETE /spaningar/:id
; Delete the spaning.
;


(defroutes main-routes
  (GET "/" [] (index))
  (GET "/tag/:tagname" [tagname] (views/tag tagname (insta/get-by-tag tagname)))
  (POST "/tag" [tagname] (views/tag tagname (insta/get-by-tag tagname)))
  (GET "/media/:id" [id] (views/media (insta/get-media-by-id id)))
  (GET "/user/:id" [id] (views/user (insta/get-user-by-id id)))
  (GET "/user/:id/media" [id] (views/user-media (insta/get-user-by-id id) (insta/get-media-by-user-id id)))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (ring-params/wrap-params main-routes))

(defn -main []
  (ring/run-jetty #'app {:port 8080}))

