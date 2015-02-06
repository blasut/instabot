(ns instabot.views
  (:require [hiccup.page :as page]
            [hiccup.core :refer [h]]
            [hiccup.form :as form]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(defn common [title & body]
  (page/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content
            "width=device-width, initial-scale=1, maximum-scale=1"}]
    (page/include-css "/css/styles.css")
    (page/include-js "/js/jquery-2.1.3.min.js")
    (page/include-js "/js/jquery.infinitescroll.min.js")
    (page/include-js "/js/scripts.js")
    [:title title]]
   [:body
    [:div {:id "header"}
     [:h1 {:class "container"} [:a {:href "/"} "Instabot"]]]
    [:div {:id "content" :class "container"} body]]))


;; HELPERS


(defn media-route [m]
  (str "/media/" (:_id m)))

(defn user-media-route [u]
  (str "/users/" (:_id u) "/media"))

(defn tags-route [t]
  (str "/tags/" t "/pages/1"))

(defn show-tags [m]
  [:div {:class "tags"}
   [:p "Tags:"]
   (map (fn [t] [:a {:class "tag" :href (tags-route t)} t]) (:tags m))])

(defn username-link [id username]
  [:a {:class "user" :href (str "/users/" id)} username])

; INDEX: Search for tag.
; Perhaps show available tags here?
(defn index [tags]
  (common "instabot"
          [:div {:class "index"}
           [:p
            [:a {:href "/spaningar"} "Spaningar"]]
           (form/form-to 
            [:post "/tags"]
            (form/label "tagname" "Tagname:")
            (form/text-field "tagname")
            (form/submit-button {:class "submit"} "Sök"))
           [:p 
            [:p "alla taggar som finns i systemet:"]
            (map (fn [t] [:a {:class "tag" :href (tags-route t)} t]) tags)]]))



(defn a-single-media [m]
  [:li {:class "media"}
   [:img {:src (get-in m [:images :standard_resolution :url])}]
   [:div {:class "metadata"} 
    (username-link (get-in m [:user :id]) (get-in m [:user :username]))
    [:span {:class "created-date"} (:created_date m)]
    [:a {:class "see-more" :href (media-route m)} "Se mer"]
    [:p "Likes: " (get-in m [:likes :count])]
    [:p "Comments: " (get-in m [:comments :count])]
    [:span {:class "tags"} (show-tags m)]]])

(defn show-media [media]
  [:div
   [:ul {:class "medias"} (map
                             (fn [m] (a-single-media m))
                             media)]])

(defn show-pagination [next-page url media-count]
  (let [prev-page (- next-page 2)]
    [:ul {:class "navigation"}
     [:li
      (if (not= prev-page 0)
        [:a {:href (str url "/pages/" prev-page)} "Prev page"])
      (if (not= media-count 0)
        [:a {:href   (str url "/pages/" next-page)} "Next page"])
      ]]))

(defn tag [tagname media-count media next-page]
  (common (str "Tag: " tagname)
          [:div
           [:h1 tagname]
           [:p "Total number of media: " media-count]
           [:p "Number of media this page: " (count media)]
           (show-media media)
           (show-pagination next-page (str "/tags/" tagname) (count media))]))

(defn media [m]
  (common "media"
          [:div
           [:h1 "Media"]
           [:ul {:class "medias"} (a-single-media m)]
           [:ul (map 
                 (fn [c] [:div {:class "comment"} 
                          [:p 
                           (username-link (get-in c [:from :id]) (get-in c [:from :username]))
                           [:span (:created_time c)]]
                          [:img {:class "profile-picture" :src (get-in c [:from :profile_picture])}]
                          [:p (:text c)]]) 
                 (get-in m [:comments :data]))]]))

(defn user [u]
 (common "User" 
         [:div 
          [:p (:username u)]
          [:p (:bio u)]
          [:p (:website u)]
          [:p (:full_name u)]
          [:p [:img {:src (:profile_picture u)}]]
          [:p "Media: " (get-in u [:counts :media])]
          [:p "Followed by: " (get-in u [:counts :followed_by])]
          [:p "Follows: " (get-in u [:counts :follows])]
          [:p [:a {:href (user-media-route u)} "The users media"]]]))
 

; GET /user/:id/media
; Show all media related to the user.


(defn user-media [user media]
  (common "Users media"
          [:div "User: " (:username user)
           [:p "Total number of media: " (count media)]
           (show-media media)]))

(defn location-media-route [l]
  (str "/location/" (:_id l) "/media"))

(defn location [location media-count media next-page]
  (common "Location media"
   [:div
    [:p "Lat: " (:lat location)]
    [:p "Lng: " (:lng location)]
    [:p "Total number of media: " media-count]
    [:ul
     (show-media media)]
    (show-pagination next-page (location-media-route location) (count media))]))

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

(defn single-spaning [s]
  [:li 
   [:p {:class "title"} "Title: " (:title s)]
   [:p "Type: " (:type s)]
   [:p "Tag: "
    [:a {:href (tags-route (:tagname s))} (:tagname s)]]
   [:p "Start date: " (:start_date s)]
   [:p "End date: -"]
   [:p "Latitud: " (:lat s)]
   [:p "Longitud: " (:lng s)]
   [:p "Distance: " (:dst s)]
   (if (= "Location" (:type s))
     [:p [:a {:class "see-media" :href (str "/location/" (:_id s) "/media/pages/1")} "See data"]]
     [:p [:a {:class "see-media" :href (tags-route (:tagname s))} "See data"]])
   [:p [:a {:class "remove" :href (str "/spaningar/" (:_id s) "/destroy")} "Remove get"]]])

(defn spaningar [spaningar]
  (common "Spaningar"
          [:div {:class "spaningar-container"}
           [:p 
            [:a {:href "/spaningar/new" :class "new-spaning"} "Create a new get >"]]
           [:h3 "Current gets: "]
           [:ul {:class "spaningar"}
            (map #(single-spaning %) spaningar)]]))

(defn spaningar-new []
  (common "Create spaning"
          [:div {:class "spaning new"} "Create new spaning."
           (form/form-to 
            [:post "/spaningar"]

            (form/label {:class "hashtag location"}  "type" "Title:")
            (form/text-field {:class "hashtag location"} "title")

            (form/label {:class "type-of-spaning"}  "type" "Type:")
            (form/drop-down {:class "type-of-spaning"} "type" ["Hashtag" "Location"])

            (form/label {:class "hashtag"} "tagname" "Hashtag (without the #):")
            (form/text-field {:class "hashtag"} "tagname")
            
            (form/label {:class "location"} "lat" "Latitud:")
            (form/text-field {:class "location"} "lat")

            (form/label {:class "location"} "lng" "Longitud:")
            (form/text-field {:class "location"} "lng")

            (form/label {:class "location"} "dst" "Distance (Max 5000m):")
            (form/text-field {:class "location"} "dst")

            (form/label {:class "hashtag location"} "start_date" "Start date [YYYY-MM-DD]:")
            (form/text-field {:class "hashtag location"} "start_date")

            (form/label "end_date" "End date:")
            (form/text-field {:disabled true} "end_date")


            (form/submit-button {:class "submit"} "Submit"))
           ]))

(defn spaning [s]
  (common "Spaningar"
          [:div
           [:p "Nu haru skapat en spaning "]
           [:p 
            [:a {:href "/spaningar/new"} "Skapa ny"]]
           [:p 
            [:a {:href "/spaningar"} "Alla"]]]))

(defn spaning-deleted [s]
  (common "Spaningar"
          [:div "Hej"
           [:p "Borttaggen"]
           [:p s]
           [:p 
            [:a {:href "/spaningar/new"} "Skapa ny"]]
           [:p 
            [:a {:href "/spaningar"} "Alla"]]]))
