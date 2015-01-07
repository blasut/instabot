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
                 [hiccup "1.0.5"]
                 [schejulure "1.0.1"]
                 [throttler "1.0.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.9"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]]
  :profiles {:dev {:dependencies [[midje "1.6.0" :exclusions [org.clojure/clojure]]]
                   :plugins [[cider/cider-nrepl "0.8.2"]
                             [lein-midje "3.1.3"]
                             [lein-ring "0.8.13"]]}
             :uberjar {:aot :all}}
  :main instabot.core
  :uberjar-name "instabot-standalone.jar"
  :aot [instabot.core]
  :ring {:handler instabot.core/app})




