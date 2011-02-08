(ns zenclient.java
  (:refer-clojure :exclude [alias])
  (:use zenclient.core))

(defmacro alias [name var]
  `(defn ~name [~'this] (~var (.state ~'this))))


;; Job

(gen-class :name zenclient.java.Job
	   :prefix "-job-"
	   :constructors {[java.util.Map] []}
	   :init init
	   :state state
	   :methods [[getId        [] long]
		     [getState     [] String]
		     [isPending    [] boolean]
		     [isWaiting    [] boolean]
		     [isProcessing [] boolean]
		     [isFinished   [] boolean]
		     [isFailed     [] boolean]
		     [isCancelled  [] boolean]])

(defn -job-init [m] [[] m])

(alias -job-getId        :id)
(alias -job-getState     :state)

(alias -job-isPending    pending?)
(alias -job-isWaiting    waiting?)
(alias -job-isProcessing processing?)
(alias -job-isFinished   finished?)
(alias -job-isFailed     failed?)
(alias -job-isCancelled  cancelled?)


;; Output

(gen-class :name zenclient.java.Output
	   :prefix "-output-"
	   :constructors {[java.util.Map] []}
	   :init init
	   :state state
	   :methods [[getId        [] long]
		     [getState     [] String]
		     [getLabel     [] String]
		     [getUrl       [] String]
		     [isReady      [] boolean]
		     [isWaiting    [] boolean]
		     [isAssigning  [] boolean]
		     [isProcessing [] boolean]
		     [isFinished   [] boolean]
		     [isFailed     [] boolean]
		     [isCancelled  [] boolean]])

(defn -output-init [m] [[] m])

(alias -output-getId    :id)
(alias -output-getState :state)
(alias -output-getLabel :label)
(alias -output-getUrl   :url)

(alias -output-isReady      ready?)
(alias -output-isWaiting    waiting?)
(alias -output-isAssigning  assigning?)
(alias -output-isProcessing processing?)
(alias -output-isFinished   finished?)
(alias -output-isFailed     failed?)
(alias -output-isCancelled  cancelled?)


;; Notification

(gen-class :name zenclient.java.Notification
	   :prefix "-notification-"
	   :constructors {[java.util.Map] []}
	   :init init
	   :state state
	   :methods [[getJob    [] zenclient.java.Job]
		     [getOutput [] zenclient.java.Output]])

(defn -notification-init [{:keys [job output]}]
  [[] {:job    (zenclient.java.Job. job)
       :output (zenclient.java.Output. output)}])

(alias -notification-getJob    :job)
(alias -notification-getOutput :output)


;; Core Client

(defmacro defn+key [name params & body]
  `(defn ~name ~params
     (binding [zenclient.core/*api-key* (.apiKey ~'this)]
       ~@body)))

(gen-class :name zenclient.java.Zen
	   :prefix "-core-"
	   :constructors {[String] []}
	   :init init
	   :state apiKey
	   :methods [[eval [String] Object]
		     [createJob [String] java.util.Map]
		     [createJob [String String] java.util.Map]
		     [notification [String] zenclient.java.Notification]])


(defn -core-init [api-key]
  (use 'zenclient.core)
  [[] api-key])

(defn+key -eval [this sexpr] (eval (read-string sexpr)))

(defn -core-createJob
  ([this input] (-createJob this input ""))
  ([this input options]
     (binding [zenclient.core/*api-key* (.apiKey this)]
       (let [opts (read-string (str "(" options ")"))]
	 (apply create-job! input opts)))))

(defn -core-notification [_ json]
  (let [m (notification json)]
    (zenclient.java.Notification. m)))
