(ns data-collector.core-test
  (:require [clojure.test :refer :all]
            [data-collector.core :refer :all]))

(def should-throw (atom false))

;; Silly generator function.
(defn generator-fn [prefix]
  (if (true? @should-throw)
    (throw (Exception. "you asked me to throw an exception" ))
    (str prefix "-" (System/currentTimeMillis))))

(defn- take-first [s number-of-characters]
  (apply str (take number-of-characters s)))

(defn- take-but [s number-of-characters]
  (apply str (reverse (take number-of-characters (reverse s)))))

(deftest test-basic-collection
  (testing "Testing basic collector operation."
    (let [test-collector (create-collector 50 generator-fn "PRE#####")
          _              (Thread/sleep 60)
          val1           (get-data test-collector)
          _              (Thread/sleep 60)
          val2           (get-data test-collector)
          ;; this should set the throw flag to true
          _              (swap! should-throw false?)
          ;; From this point on all calls to get data should get the same value
          _              (Thread/sleep 60)
          val3           (get-data test-collector)
          _              (Thread/sleep 60)
          val4           (get-data test-collector)
          ;; this should set the throw flag to false again
          _              (swap! should-throw false?)
          ;; From this point on all calls to get data should get different values
          _              (Thread/sleep 60)
          val5           (get-data test-collector)
          _              (Thread/sleep 60)
          val6           (get-data test-collector)]
      ;; Make sure we are updating values
      (is (not (= val1 val2)))
      ;; Also, make sure the query function is taking the arguments 
      (is (= "PRE#####" (take-first val1 8)))
      ;; Once we throw exceptions we keep the same old values
      (is (= val3 val4))
      ;; And if we recover we start getting fresh data again
      (is (not (= val4 val6)))
      (is (true? (shutdown-collector test-collector)))
      (is (nil? (get-data test-collector))))))
