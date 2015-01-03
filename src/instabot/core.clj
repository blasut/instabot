(ns instabot.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as ring]
            [ring.middleware.params :as ring-params]
            [hiccup.page :as page]
            [hiccup.core :refer [h]]
            [hiccup.form :as form]
            [instabot.insta :as insta]))
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

(defn common [title & body]
  (page/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content
            "width=device-width, initial-scale=1, maximum-scale=1"}]
    (page/include-css "/css/styles.css")
    [:title title]]
   [:body
    [:div {:id "header"}
     [:h1 {:class "container"} "Instabot"]]
    [:div {:id "content" :class "container"} body]]))


; INDEX: Search for tag.
; Perhaps show available tags here?
(defn index []
  (common "instabot"
          [:div 
           (form/form-to 
            [:post "/tag"]
            (form/label "tagname" "Tagname:")
            (form/text-field "tagname")
            (form/submit-button "Submit"))]))

(defn media-route [m]
  (str "/media/" (:_id m)))

(defn show-tags-for-media [m]
  [:div {:class "tags"}
   [:p "Tags:"]
   (map (fn [t] [:a {:class "tag" :href (str "/tag/" t)} t]) (:tags m))
   ])

(defn a-single-media [m]
  [:li {:class "media"}
   [:img {:src (get-in m [:images :standard_resolution :url])}]
   [:div {:class "metadata"} 
    [:a {:class "user" :href (str "/user/" (get-in m [:user :id]))} (get-in m [:user :username])]
    [:span {:class "created-date"} (clj-time.coerce/from-long (read-string (str (:created_time m) "000")))]
    [:a {:class "see-more" :href (media-route m)} "Se mer"]
    [:span {:class "tags"} (show-tags-for-media m)]
    ]
   ]
  )

(defn tag [tagname media]
  (println tagname)
  (common (str "Tag: " (str tagname))
          [:div
           [:h1 tagname]
           [:ul {:class "medias"} (map
                 (fn [m] (a-single-media m))
                 media)]]))

(defn media [id])
 
; POST /tags/:tag_name
; Show the media with the tagname.
;
; GET /media/:id
; Show all data associated with the media.
;
; GET /user/:id
; Show all data associated with the user
;
; GET /user/:id/media
; Show all media related to the user.


(defroutes main-routes
  (GET "/" [] (index))
  (GET "/tag/:tagname" [tagname] (tag tagname (insta/get-by-tag tagname)))
  (POST "/tag" [tagname] (tag tagname (insta/get-by-tag tagname)))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (ring-params/wrap-params main-routes))

(defn -main []
  (ring/run-jetty #'app {:port 8080}))

