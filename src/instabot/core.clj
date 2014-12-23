(ns instabot.core
  (:require [clj-http.client :as client]
            [environ.core :refer [env]])
  (:use
    instagram.oauth
    instagram.callbacks
    instagram.callbacks.handlers
    instagram.api.endpoint)
  (:import
    (instagram.callbacks.protocols SyncSingleCallback)))

(def client-id (:client-id env))
(def client-secret (:client-secret env))
(def redirect-uri (:redirect-uri env))

(def ^:dynamic *creds* (make-oauth-creds client-id
                                         client-secret
                                         redirect-uri))
;(def tagged (get-tagged-medias :oauth *creds* :params {:tag_name "cat"}))

(defn get-media-blob [tagname]
  (get-tagged-medias :oauth *creds* :params {:tag_name tagname}))

(defn get-by-pagination-url [media]
  (let [url (get (get (get media :body) "pagination") "next_url")]
    (client/get url)))
  
(defn get-next-media [tagname & [media]]
  (if media
    (get-by-pagination-url media)
    (get-media-blob tagname)))

(defn pagination? [media]
  (not (nil? (get (get media :body) "pagination"))))

(defn parse-content [media]
  (get (get media :body) "data"))

; First the a get-tagged-media to get the pagination link
; Then continue till there are no pagination links
; Så länge det finns pagination länkar, fortsätt att loopa
(defn get-all-tagged-media [tagname]
  (println "lets do this")
  (loop [result []
         old_media nil]
    (println "We are in the loop right now")
    (let [media (get-next-media tagname old_media)]
      (if (pagination? media) ; What happens when there are no more media?
        (recur (conj result (parse-content media)) media)
        result))))

; För att få ut users från datan: (get (second (second (first (get (get tagged :body) "data")))) "from")
; För att få ut id från datan: (get (get (second (second (first (get (get tagged :body) "data")))) "from") "id")

; Problemet med get-tagged-medias är att den returnar x antal media + en pagination. Det kan vara sjukt många poster som har gjorts på hashtagen. Detta löses genom att räkna ut antal sidor och dela upp så det blir mindre än 5.000 requests per timme.

; Dock går det inte att räkna ut detta innan ut vi måste ha en counter som ser till att antal requests inte blir för många per timme.


;; Search funkar bara i 7dagar max.
;; /media/search
;; Search for media in a given area. The default time span is set to 5 days. The time span must not exceed 7 days. Defaults time stamps cover the last 5 days. Can return mix of image and video types.



;; För att göra denna app krävs följande API endpoints hos instagram:


;; Limits:
;; Unauthenticated Calls	5,000 / hour per application
;; 
;; ENDPOINT	UNSIGNED CALLS (PER TOKEN)	SIGNED CALLS (PER TOKEN)
;; POST /media/media-id/likes	30 / hour	100 / hour
;; POST /media/media-id/comments	15 / hour	60 / hour
;; POST /users/user-id/relationships	20 / hour	60 / hour
;;

;; Verkar som denna endpoint är starten: http://instagram.com/developer/endpoints/tags/#get_tags_media_recent


;; Och sen: http://instagram.com/developer/endpoints/users/#get_users
;; Går den att använda med endast ett klient id?


