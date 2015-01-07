(ns instabot.logging
  (:require [dire.core :refer [with-pre-hook!]]
            [instabot.insta :as insta]
            [instabot.spaning :as spaning]
            [instabot.views :as views]
            [instabot.media :as media]
            [instabot.users :as users]
            [clojure.tools.logging :as log]
            ))

(with-pre-hook! #'insta/get-media-blob
  (fn [tagname]
    (log/info "get media blob" tagname)))

(with-pre-hook! #'insta/get-by-pagination-url
  (fn [media]
    (log/info "get by pagination url")))

(with-pre-hook! #'insta/get-user-data
  (fn [id]
    (log/info "get user data for id: " id)))

(with-pre-hook! #'insta/parse-user-data
  (fn [blob]
    (log/info "parse user data")))

(with-pre-hook! #'insta/save-users-and-media
  (fn [media users]
    (log/info "save users and media")))

(with-pre-hook! #'insta/fetch-and-save-a-tag
  (fn [tag stop-date]
    (log/info "fetch and save tag:" tag "with stop-date: " stop-date)))

