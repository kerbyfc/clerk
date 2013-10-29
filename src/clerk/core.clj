(ns clerk.core
  (use [clojure.string :only [trim-newline]]
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
        builder (ProcessBuilder. (apply conj [command] arguments))
        env (.environment builder)]
    (when vars
      (doseq [[var value] (map identity vars)]
        (.put env (name var) (trim-newline value))))
    (println "run " command arguments (if vars (str "with env rewrites: " vars)))
    (.redirectErrorStream builder true)
    (.inheritIO builder)
    (def process (.start builder))
    {
     :out (BufferedReader. (InputStreamReader. (.getInputStream process)))
     :err (BufferedReader. (InputStreamReader. (.getErrorStream process)))
    }
    ))

(defmacro read-process
  [proc data]
  `(read-from-reader (~data ~proc)))




