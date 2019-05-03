(ns data-collector.core
  (:require [data-collector.model :as m]
            [clojure.spec.alpha :as s]
            [clojure.core.async :as async :refer [go
                                                  timeout
                                                  chan
                                                  >! >!! <! <!!
                                                  buffer
                                                  close!
                                                  thread
                                                  alts! alts!!]]))

(defn- safe-call
  "call function defined by :fn in the argument map with the arguments defined in the :args key and returns its value.

  If calling the function throws an exception, it returns the value stored under :default"
  [{fun     :fn
                   args    :args
                   default :default}]
  (try (apply fun args)
       (catch Exception e
           default)))

(s/fdef create-collector
  :args (s/cat :max-delay number? :query-fn fn? :query-args (s/* any?))
  :ret ::m/collector)
(defn create-collector
  "Creates a collector that queries query-fn using query-args as its arguments every x milliseconds where x is a random number between 0 and max-delay"
  [max-delay query-fn & query-args]
  (let [data          (atom nil)
        comms-channel (chan 1)]
    (go
      (loop []
        (let [delay     (rand-int max-delay)
              scheduler (timeout delay)]
          (reset! data (safe-call {:fn query-fn
                                   :args query-args
                                   :default @data}))
          (let [[v ch] (alts! [scheduler
                               comms-channel])]
            (if (= ch comms-channel)
              (let [cmd (::m/command v)]
                (cond
                  (= cmd "bye")
                  (do (println "Received" cmd ", shutting down collector")
                      (reset! data nil))
                  :else
                  (do (printf "After receiving %s, Collector awkwardly waves back at you and gets back to work.\n" cmd)
                      (recur))))
              (recur))))))
    {::m/state   data
     ::m/channel comms-channel}))

(s/fdef get-data :args (s/cat :collector ::m/collector))
(defn get-data [collector]
  (deref (::m/state collector)))


(defn- send-command [collector command]
  (let [comms-channel (::m/channel collector)
        _             (println "sending " command )]
    (>!! comms-channel command)))

(s/fdef shutdown-collector
  :args (s/cat :collector ::m/collector))
(defn shutdown-collector [collector]
  (send-command collector {::m/command "bye"}))

(s/fdef refresh-collector
  :args (s/cat :collector ::m/collector))
(defn refresh-collector [collector]
  (send-command collector {::m/command "status"}))
