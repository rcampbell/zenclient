(ns zenclient.core
  (:require [clojure.contrib.http.agent :as http])
  (:use [clojure.walk :only (postwalk)]
	[clojure.string :only (join)]
	[clojure.contrib.def :only (defunbound-)]
	[clojure.contrib.string :only (replace-char)]
	[clojure.contrib.json :only (read-json json-str)]
	[clojure.contrib.condition :only (raise)])
  (:import [java.util Map]
	   [org.joda.time.format DateTimeFormat]))

(defunbound- *api-key* "dynamically bound via fn set-api-key! or create-account!")

(defn set-api-key! [key]
  (def ^{:private true} *api-key* key))

(def ^{:private true} api "https://app.zencoder.com/api")

(def ^{:private true} headers {"Accept" "application/json"
			       "Content-Type" "application/json"})

(letfn [(rename [a b k] (keyword (replace-char a b (name k))))
	(swap [a b]
	      (letfn [(f [[k v]] (if (keyword? k) [(rename a b k) v] [k v]))]
		(fn [m] (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))]
  (def ^{:private true} dash->underscore (swap \- \_))
  (def ^{:private true} underscore->dash (swap \_ \-)))

(let [parse (comp underscore->dash read-json http/string)
      handle (fn [agent] (let [body (parse agent)]
			   (if (http/success? agent)
			     body
			     (let [{errors :errors} body]
			       (raise :message (join "; " errors)
				      :status (http/status agent)
				      :errors errors)))))]
  (defn- api-get [path]
    (let [uri (str api path)]
      (handle (http/http-agent uri :headers headers))))
  (defn- api-post
    ([path] (api-post path {}))
    ([path body]
       (let [uri (str api path)
	     json-body (json-str (dash->underscore body))]
	 (handle (http/http-agent uri
				  :method "POST"
				  :headers headers
				  :body json-body))))))

(defn- ci= [l r] (.equalsIgnoreCase l r))

(defn- lazy-loader [loader]
  (fn
    ([parser] (parser (loader)))
    ([parser src]
       (if (instance? Map src)
	 (parser src)
	 (parser (loader src))))))

(doseq [k [:id
	   :api-key
	   :password
	   :pass-through
	   :output-media-files
	   :thumbnails
	   :watermark
	  ; :format ns conflict
	   :frame-rate
	   :duration-in-ms	   
	   :url
	   :error-message
	   :error-class
	   :video-codec
	   :audio-codec
	   :video-bitrate-in-kbps
	   :audio-bitrate-in-kbps
	   :audio-sample-rate	   
	   :height
	   :width
	   :file-size-bytes	   
	   :channels
	   :label
	   ]]
  (intern *ns* (symbol (name k)) k))


;; Creating an Encoding Job

(defn create-job! [input & options]
  (let [job (merge {:api-key *api-key* :input input}
		   (apply array-map options))]
    (api-post "/jobs" job)))

(letfn [(options-map [& options] (apply array-map options))]
  (def +output options-map)
  (def +watermark options-map)
  (def +thumbnails options-map)
  (def +access-control options-map))


;; Getting Job Progress

(defmulti progress
  "retrieves the current progress of an output file"
  class)
(defmethod progress Map [output]
  (progress (output id)))
(defmethod progress :default [output-id]
  (api-get (format "/outputs/%s/progress?api_key=%s" (str output-id) *api-key*)))
      
(defn state [src] (:state src))
(letfn [(state= [v] (fn [src] (ci= v (state src))))]
  (def ready? (state= "ready"))
  (def pending? (state= "pending"))
  (def waiting? (state= "waiting"))
  (def assigning? (state= "assigning"))
  (def processing? (state= "processing"))
  (def finished? (state= "finished")) 
  (def failed? (state= "failed"))    
  (def cancelled? (state= "cancelled"))
  (def queued? (state= "queued")))
(defn current-event [src] (:current-event src))
(letfn [(event= [v] (fn [src] (ci= v (current-event src))))]
  (def inspecting? (event= "Inspecting"))
  (def downloading? (event= "Downloading"))
  (def transcoding? (event= "Transcoding"))
  (def uploading? (event= "Uploading")))
(defn event-progress
  "returns the percent complete of the current event"
  [src]
  (if-let [percent (:progress src)]
    (Float/valueOf percent) nil))


;; Working With Jobs

(defn list-jobs []
  (map :job (api-get (format "/jobs?api_key=%s" *api-key*))))

(defmulti details class)
(defmethod details Map [job]
  (details (job id)))
(defmethod details :default [job-id]
  (:job (api-get (format "/jobs/%s?api_key=%s" (str job-id) *api-key*))))

(def test? :test)
(defn input [m] (or (:input m) (:input-media-file m)))
(defn outputs [m] (or (:outputs m) (:output-media-files m)))

(let [pattern "yyyy-MM-dd'T'HH:mm:ssZ"
      formatter (DateTimeFormat/forPattern pattern)
      timestamp #(.parseDateTime formatter %)]
  (def created-at (comp timestamp :created-at))
  (def finished-at (comp timestamp :finished-at))
  (def updated-at (comp timestamp :updated-at))
  (def submitted-at (comp timestamp :submitted-at)))

(defn resubmit-job! [job-id]
  (let [uri (format "%s/jobs/%s/resubmit?api_key=%s" api (str job-id) *api-key*)]
    (http/success? (http/http-agent uri))))

(defn cancel-job! [job-id]
  (let [uri (format "%s/jobs/%s/cancel?api_key=%s" api (str job-id) *api-key*)]
    (http/success? (http/http-agent uri))))
  
(defn delete-job! [job-id]
  (let [uri (format "%s/jobs/%s?api_key=%s" api (str job-id) *api-key*)]
    (http/success? (http/http-agent uri :method "DELETE"))))


;; Working With Accounts

(defn create-account! [email & options]
  (let [opts (apply array-map options)
	account (merge {:email email :terms-of-service "1"} opts)
	response (api-post "/account" account)]
    (def *api-key* (response api-key))
    response))

(defn account-details []
  (api-get (format "/account?api_key=%s" *api-key*)))

(let [select (lazy-loader account-details)]
  (def account-state (partial select :account-state))
  (letfn [(state= [v] (fn
			([] (ci= v (account-state)))
			([m] (ci= v (account-state m)))))]
    (def account-active? (state= "active"))
    (def account-stopped? (state= "stopped"))
    (def account-suspended? (state= "suspended"))
    (def account-cancelled? (state= "cancelled")))
  (def plan (partial select :plan))
  (def minutes-used (partial select :minutes-used))
  (def minutes-included (partial select :minutes-used))
  (def billing-state (partial select :billing-state))
  (letfn [(state= [v] (fn
			([] (ci= v (billing-state)))
			([m] (ci= v (billing-state m)))))]
    (def billing-active? (state= "active"))
    (def billing-past-due? (state= "past due"))
    (def billing-cancelled? (state= "cancelled")))
  (def privacy-mode? (partial select :privacy-mode))
  (def integration-mode? (partial select :integration-mode))
  (def live-mode? (complement integration-mode?)))

(defn integration-mode!
  "turns on integration mode" []
  (api-get "/account/integration"))

(defn live-mode!
  "turns off integration mode" []
  (api-get "/account/live"))
