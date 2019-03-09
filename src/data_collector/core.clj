(ns data-collector.core
  (:require [clojure.core.async :as async :refer [go
                                                  timeout
                                                  chan
                                                  >! >!! <! <!!
                                                  buffer
                                                  close!
                                                  thread
                                                  alts! alts!!]]))

(defn- safe-call [{fun     :fn
                   args    :args
                   default :default}]
  (try (apply fun args)
       (catch Exception e
           default)))

;; Comms channel receives messages with the following form
;; {:command cmd}
;; returns a map of {:state state-atom :channel comms-channel}
(defn create-collector
  "Creates a collector that queries query-fn using query-args as its arguments every x milliseconds where x is a random number between 0 and max-delay"
  [max-delay query-fn & query-args]
  (let [collector      (atom nil)
        comms-channel (chan 1)]
    (go
      (loop []
        (let [delay     (rand-int max-delay)
              scheduler (timeout delay)]
          (reset! collector (safe-call {:fn query-fn
                                        :args query-args
                                        :default @collector}))
          (let [[v ch] (alts! [scheduler
                               comms-channel])]
            (if (= ch comms-channel)
              (let [cmd (:command v)]
                (cond
                  (= cmd "bye")
                  (do (println "Received" cmd ", shutting down collector")
                      (reset! collector nil))
                  :else
                  (do (printf "After receiving %s, Collector awkwardly waves back at you and gets back to work.\n" cmd)
                      (recur))))
              (recur))))))
    {:state   collector
     :channel comms-channel}))

(defn get-data [collector]
  (deref (:state collector)))

(defn- send-command [collector command]
  (let [comms-channel (:channel collector)
        _             (println "sending " command )]
    (>!! comms-channel command)))

(defn shutdown-collector [collector]
  (send-command collector {:command "bye"}))

(defn refresh-collector [collector]
  (send-command collector {:command "status"}))

