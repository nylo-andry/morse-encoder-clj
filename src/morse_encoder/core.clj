(ns morse-encoder.core
  (:require
   [clojure.string :refer [upper-case join]]
   [dynne.sampled-sound :refer :all]))

(def alphabet {\A ".-" \B "-..." \C "-.-." \D "-.." \E "." \F"..-."
               \G "--." \H "...." \I ".." \J ".---" \K "-.-" \L ".-.."
               \M "--" \N "-." \O "---" \P ".--." \Q "--.-" \R ".-."
               \S "..." \T "-" \U "..-" \V "...-" \W ".--" \X "-..-"
               \Y "-.--" \Z "--.." \space "/" \0 "-----" \1 ".----"
               \2 "..---" \3 "...--" \4 "....-" \5 "....." \6 "-...."
               \7 "--..." \8 "-...." \9 "----." \. ".-.-.-" \, "--..--"
               \: "---..." \? "..--.." \' ".----." \- "-....-" })

(def dit (sinusoid 0.1 440))
(def dah (sinusoid 0.3 440))
(def signal-gap (silence 0.1 1))
(def short-gap (silence 0.3 1))
(def medium-gap (silence 0.6 1))

(defn get-signal-sound
  [symbol]
  (case symbol
    \. dit
    \- dah
    \/ medium-gap))

(defn break-to-chars
  [message]
  (-> (upper-case message)
       (char-array)
       (seq)))

(defn to-morse
  [message]
  (let [characters (break-to-chars message)]
    (pmap #(alphabet %) characters)))

(defn build-letter-sound-signals
  [morse-letter]
  (let [signals (break-to-chars morse-letter)
        sounds (map get-signal-sound signals)]
    (if (> (count sounds) 1)
      (reduce #(append (append %1 signal-gap) %2) sounds)
      (first sounds))))

(def memoized-sounds (memoize build-letter-sound-signals))

(defn build-words-sound-signals
  [morse-letters]
  (map memoized-sounds morse-letters))

(defn append-sound-signals
  [sounds]
  (reduce #(append %1 (append short-gap %2)) sounds))


(defn to-sound-file
  [morse-letters filename]
  (-> (build-words-sound-signals morse-letters)
      (append-sound-signals)
      (#(save %1 filename 2200))))
