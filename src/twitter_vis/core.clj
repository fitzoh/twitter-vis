(ns twitter-vis.core
  (:require [clojure.data.json :as json]
            [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
                        [langohr.basic     :as lb])
  (:import
   (com.twitter.hbc ClientBuilder)
   (com.twitter.hbc.core Client Constants )

   (com.twitter.hbc.core.endpoint StatusesFilterEndpoint Location Location$Coordinate)
   (com.twitter.hbc.core.processor StringDelimitedProcessor)
           (com.twitter.hbc.httpclient.auth Authentication OAuth1)
           (java.util.concurrent BlockingQueue LinkedBlockingQueue)))


(defn make-location [lat-1 long-1 lat-2 long-2]
  (let [coord-1 (Location$Coordinate. lat-1 long-1 )
        coord-2 (Location$Coordinate. lat-2 long-2)]
    (Location. coord-1 coord-2)))

(def ohio (make-location -84.49 38.24 -80.31 41.59))

(defn -main []
  (def queue (LinkedBlockingQueue. 10000))
  (def endpoint (StatusesFilterEndpoint.))
  (. endpoint locations [ohio])
  (def consumer-key "" )
  (def consumer-secret "")
  (def token "")
  (def secret "")
  (def auth (OAuth1. consumer-key consumer-secret token secret))
  (def client (-> (ClientBuilder.)
                  (. hosts (. Constants STREAM_HOST))
                  (. endpoint endpoint)
                  (. authentication auth)
                  (. processor (StringDelimitedProcessor. queue))
                  (. build)))
  (. client connect)

  (def conn (rmq/connect))
  (def ch (lch/open conn))
  (def queue-name "twitter")
  (lq/declare ch queue-name :auto-delete false :exclusive false)
  (loop []
    (let [data (json/read-str (. queue take))
          geo (get data "geo" nil)]
      (lb/publish ch "" queue-name (json/write-str geo )))
    (recur))
   (.addShutdownHook ( Runtime/getRuntime) (Thread.  #(. client stop))))
