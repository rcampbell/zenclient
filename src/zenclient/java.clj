(ns zenclient.java
  (:use zenclient.core))

(defmacro defn+key [name params & body]
  `(defn ~name ~params
     (binding [zenclient.core/*api-key* (.apiKey ~'this)]
       ~@body)))

(gen-class :name zenclient.java.Zenclient
	   :constructors {[String] []}
	   :init init
	   :state apiKey
	   :methods [[eval [String] Object]
		     [createJob [String] java.util.Map]
		     [createJob [String String] java.util.Map]
		     [id [Object] long]
		     [state [Object] String]
		     [notification [String] java.util.Map]
		     [job [java.util.Map] java.util.Map]
		     [isJobProcessing [Object] boolean]
		     [isJobFinished [Object] boolean]
		     [isJobFailed [Object] boolean]
		     [output [java.util.Map] java.util.Map]
		     [label [java.util.Map] String]
		     [url [java.util.Map] String]])


(defn -init [api-key]
  (use 'zenclient.core)
  [[] api-key])

(defn+key -eval [this sexpr] (eval (read-string sexpr)))

(defn -createJob
  ([this input] (-createJob this input ""))
  ([this input options]
     (binding [zenclient.core/*api-key* (.apiKey this)]
       (let [opts (read-string (str "(" options ")"))]
	 (apply create-job! input opts)))))

(defn+key -id [this any] (id any))
(defn+key -state [this any] (state any))
(defn+key -notification [this raw] (notification raw))
(defn+key -job [this notification] (job notification))
(defn+key -isJobProcessing [this job] (->> job processing?))
(defn+key -isJobFinished [this job] (->> job finished?))
(defn+key -isJobFailed [this job] (->> job failed?))
(defn+key -output [this notification] (:output notification))
(defn+key -label [this output] (label output))
(defn+key -url [this output] (url output))