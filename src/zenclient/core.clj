(ns zenclient.core
  (:use [clojure.walk :only (postwalk)]
	[clojure.string :only (join blank? lower-case)]
	[clojure.contrib.string :only (replace-char)]
	[clojure.contrib.json :only (read-json json-str)]
	[clojure.contrib.condition :only (raise)]
	[clojure.contrib.http.agent :only (http-agent string success? status)])
  (:import [org.joda.time.format DateTimeFormat]))

(def ^{:private true} api "https://app.zencoder.com/api")

; (def *api-key* "c46d1828001d4969a03b45d60846649f")

(letfn [(rename [a b k] (keyword (replace-char a b (name k))))
	(swap [a b]
	      (letfn [(f [[k v]] (if (keyword? k) [(rename a b k) v] [k v]))]
		(fn [m] (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))]
  (def dash->underscore (swap \- \_))
  (def underscore->dash (swap \_ \-)))

(defn- debug [s] (println s) s)

(let [parse (comp read-json debug string)
      handle (fn [agent] (let [body (parse agent)]
			   (if (success? agent)
			     body
			     (let [{errors :errors} body]
			       (raise :message (join " " errors)
				      :status (status agent)
				      :errors errors)))))
      headers {"Accept" "application/json"
	       "Content-Type" "application/json"}]
  (defn api-get [path]
    (let [uri (str api path)]
      (handle (http-agent uri :headers headers))))
  (defn api-post
    ([path] (api-post path {}))
    ([path body]
       (let [uri (str api path)
	     json-body (json-str (dash->underscore body))]
	 (handle (http-agent uri :method "POST" :headers headers :body json-body)))))
  (defn api-delete [path]
    (let [uri (str api path)]
      (handle (http-agent uri :method "DELETE")))))

(defn ci= [l r] (.equalsIgnoreCase l r))

(defn lazy-loader [f]
  (fn
    ([k] ((f) k))
    ([k src] (if (instance? java.util.Map src) (src k) ((f src) k)))))


;; Creating an Encoding Job

(defn create-job! [input & options]
  (let [job (merge {:api-key *api-key* :input input}
		   (apply array-map options))]
    (api-post "/jobs" job)))

(def job-id :id)
(defn output-ids [{outputs :outputs}] (map :id outputs))

(letfn [(options-map [& options] (apply array-map options))]
  (def output options-map)
  (def watermark options-map)
  (def thumbnails options-map)
  (def access-control options-map))


;; Getting Job Progress

(defn output-file-progress 
  "gets the current status of an output file"
  [output-id]
  (api-get (format "/outputs/%s/progress?api_key=%s" output-id *api-key*)))
      
(let [select (lazy-loader output-file-progress)]
  (defn state [src] (select :state src))
  (letfn [(state= [v] (fn [src] (ci= v (state src))))]    
    (def queued? (state= "queued"))
    (def processing? (state= "processing"))
    (def failed? (state= "failed"))
    (def finished? (state= "finished"))
    (def cancelled? (state= "cancelled")))
  (defn current-event [src] (select :current_event src))
  (letfn [(event= [v] (fn [src] (ci= v (current-event src))))]
    (def inspecting? (event= "Inspecting"))
    (def downloading? (event= "Downloading"))
    (def transcoding? (event= "Transc(oding"))
    (def uploading? (event= "Uploading")))
  (defn progress [src]
    "returns the percent complete of the current event"
    (if-let [percent (select :progress src)]
      (Float/valueOf percent) nil)))


;; Working With Jobs

(defn list-jobs []
  (api-get (format "/jobs?api_key=%s" *api-key*)))

(defn job-details 
  "retrieve the details of a single job"
  [job-id]
  (api-get (format "/jobs/%s?api_key=%s" job-id *api-key*)))

(defn test? [{{test :test} :job}] test)

(let [pattern "yyyy-MM-dd'T'HH:mm:ssZ"
      formatter (DateTimeFormat/forPattern pattern)
      parse #(.parseDateTime formatter %)
      timestamp (fn [k] (fn [{job :job}] (parse (job k))))]
  (def created-at (timestamp :created_at))
  (def finished-at (timestamp :finished_at))
  (def updated-at (timestamp :updated_at))
  (def submitted-at (timestamp :submitted_at)))

(defn resubmit-job!
  "resubmit an unfinished job for processing"
  [job-id]
  (api-get (format "/jobs/%s/resubmit?api_key=%s" job-id *api-key*)))

(defn cancel-job!
  "cancels a job that has not yet finished processing"
  [job-id]
  (api-get (format "/jobs/%s/cancel?api_key=%s" job-id *api-key*)))
  
(defn delete-job!
  "deletes a job that has not yet finished processing"
  [job-id]
  (api-delete (format "/jobs/%s?api_key=%s" job-id *api-key*)))


;; Working With Accounts

(defn create-account! [email & options]
  (let [opts (apply array-map options)]
    (api-post "/account" (merge {:email email :terms-of-service "1"} opts))))

(def api-key :api_key)
(def password :password)

(defn account-details []
  ((api-get (format "/account?api_key=%s" *api-key*))))

(let [select (lazy-loader account-details)]
  (def account-state (partial select :account_state))
  (letfn [(state= [v] (fn
			([] (ci= v (account-state)))
			([m] (ci= v (account-state m)))))]
    (def account-active? (state= "active"))
    (def account-stopped? (state= "stopped"))
    (def account-suspended? (state= "suspended"))
    (def account-cancelled? (state= "cancelled")))
  (def plan (partial select :plan))
  (def minutes-used (partial select :minutes_used))
  (def minutes-included (partial select :minutes_used))
  (def billing-state (partial select :billing_state))
  (letfn [(state= [v] (fn
			([] (ci= v (billing-state)))
			([m] (ci= v (billing-state m)))))]
    (def billing-active? (state= "active"))
    (def billing-past-due? (state= "past due"))
    (def billing-cancelled? (state= "cancelled")))
  (def privacy-mode? (partial select :privacy_mode))
  (def integration-mode? (partial select :integration_mode))
  (def live-mode? (complement integration-mode?)))

(defn integration-mode!
  "turns on integration mode" []
  (api-get "/account/integration"))

(defn live-mode!
  "turns off integration mode" []
  (api-get "/account/live"))

