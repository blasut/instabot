(defproject instabot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [instagram-api "0.1.8"]
                 [environ "1.0.0"]
                 [clj-time "0.8.0"]
                 [clj-http "1.0.1"]
                 [com.novemberain/monger "2.0.1"]
                 [ring "1.3.2"]
                 [compojure "1.3.1"]
                 [hiccup "1.0.5"]]
  :profiles {:dev {:plugins [[cider/cider-nrepl "0.8.2"]]}}
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler instabot.core/app})




