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
    [:title title]]
   [:body
    [:div {:id "header"}
     [:h1 {:class "container"} [:a {:href "/"} "Instabot"]]]
    [:div {:id "content" :class "container"} body]]))


; INDEX: Search for tag.
; Perhaps show available tags here?
(defn index [tags]
  (common "instabot"
          [:div {:class "index"}
           (form/form-to 
            [:post "/tags"]
            (form/label "tagname" "Tagname:")
            (form/text-field "tagname")
            (form/submit-button "Submit"))
           [:p
            [:a {:href "/spaningar"} "Spaningar"]]
           [:p 
            [:p "alla taggar:"]
            (map (fn [t] [:a {:class "tag" :href (str "/tags/" t)} t]) tags)]]))

(defn media-route [m]
  (str "/media/" (:_id m)))

(defn user-media-route [u]
  (str "/users/" (:_id u) "/media"))

(defn show-tags [m]
  [:div {:class "tags"}
   [:p "Tags:"]
   (map (fn [t] [:a {:class "tag" :href (str "/tags/" t)} t]) (:tags m))])

(defn username-link [id username]
  [:a {:class "user" :href (str "/users/" id)} username])

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

(defn tag [tagname media]
  (common (str "Tag: " (str tagname))
          [:div
           [:h1 tagname]
           [:p "Total number of media: " (count media)]
           [:ul {:class "medias"} (map
                 (fn [m] (a-single-media m))
                 media)]]))

(defn media [m]
  (common "media"
          [:div
           [:h1 "Media"]
           [:ul {:class "medias"} (a-single-media m)]
           [:ul (map 
                 (fn [c] [:div {:class "comment"} 
                          [:p 
                           (username-link (get-in c [:from :id]) (get-in c [:from :username]))
                           [:span (format-date (:created_time c))]]
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
           [:ul {:class "medias"} (map
                 (fn [m] (a-single-media m))
                 media)]]))

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
   [:p (:tagname s)]
   [:p "Start date: " (:start_date s)]
   [:p "End date: " (:end_date s)]
   [:p [:a {:href (str "/spaningar/" (:_id s) "/destroy")} "Ta bort"]]])

(defn spaningar [spaningar]
  (common "Spaningar"
          [:div "Hej"
           [:p 
            [:a {:href "/spaningar/new"} "Skapa ny"]]
           [:p "Nuvarande spaningar: "]
           [:ul
            (map #(single-spaning %) spaningar)]]))

(defn spaningar-new []
  (common "Create spaning"
          [:div {:class "spaning new"} "Create new spaning."
           (form/form-to 
            [:post "/spaningar"]

            (form/label "tagname" "Tagname (without the #):")
            (form/text-field "tagname")

            (form/label "start_date" "Start date:")
            (form/text-field "start_date")

            (form/label "end_date" "End date:")
            (form/text-field "end_date")

            (form/submit-button "Submit"))
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
