(ns clerk
  (use [reval.core :as reval]
       [clojure.string :only [trim-newline]]
       [clojure.tools.logging :only [info error]])
  (import [java.lang ProcessBuilder]
          [java.io BufferedReader InputStreamReader])
  (:require [clojure.java.io :as io])
  (:gen-class))

(def read-from-reader
  (fn [rdr]
    (let [builder (StringBuilder.)]
      (loop [line (.readLine rdr)]
        (if line
          (do
            (.append builder line)
            (.append builder (System/getProperty "line.separator"))
            (recur (.readLine rdr)))
          (.toString builder))))))

(defn exec
  [command & args]
  (let [arguments (take-while string? args)
        vars (apply hash-map (drop-while string? args))
        process (ProcessBuilder. (apply conj [command] arguments))
        env (.environment process)]
    (when vars
      (doseq [[var value] (map identity vars)]
        (.put env (name var) (trim-newline value))))
    (println "run " command arguments "in env "(.toString env))
    (def p (.start process))
    {
     :out (read-from-reader (BufferedReader. (InputStreamReader. (.getInputStream p))))
     :err (read-from-reader (BufferedReader. (InputStreamReader. (.getErrorStream p))))
    }
    ))
