(ns data-collector.model
  (:require [clojure.spec.alpha :as s]))

(s/def ::collector (s/keys :req [::state ::channel]))
(s/def ::collector-command (s/keys :req [::command]))
