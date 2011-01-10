(ns zenclient.jobs
  "working with jobs"
  (:use zenclient.core)
  (:import [org.joda.time.format DateTimeFormat]))

(declare *api-key*)

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



;; TODO -- handle response status codes!!!!

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