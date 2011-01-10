(ns zenclient.creating
  "creating an encoding job"
  (:use zenclient.core
	[clojure.contrib.string :only (blank? lower-case)]))

(defn create-job! [input & options]
  (let [job (merge {:api-key api-key :input input}
		   (apply array-map options))]
    (api-post "/jobs" job)))

(def job-id :id)
(defn output-ids [{outputs :outputs}] (map :id outputs))


;; Output Settings

(letfn [(options-map [& options] (apply array-map options))]
  (def output options-map)
  (def watermark options-map)
  (def thumbnails options-map)
  (def access-control options-map))

