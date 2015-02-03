(ns instabot.twitter
  (:require [clojure.data.json :as json]
            [environ.core :refer [env]]
            [http.async.client :as ac])
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.streaming])
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)))

(def app-consumer-key         (:app-consumer-key env))
(def app-consumer-secret      (:app-consumer-secret env))
(def user-access-token        (:user-access-token env))
(def user-access-token-secret (:user-access-token-secret env))


(def my-creds (make-oauth-creds app-consumer-key
                                app-consumer-secret
                                user-access-token
                                user-access-token-secret))


; supply a callback that only prints the text of the status
(def ^:dynamic 
  *custom-streaming-callback* 
  (AsyncStreamingCallback. bodypart-print
                           (comp println response-return-everything)
                           exception-print))

(def toots (statuses-filter :params {:track "Borat"}
         :oauth-creds my-creds
         :callbacks *custom-streaming-callback*))


