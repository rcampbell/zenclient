(ns zenclient.creating
  "creating an encoding job"
  (:use zenclient.core
	[clojure.contrib.string :only (blank? lower-case)]))

(defn create-job!
  "Creates a new encoding job.

   input string

   A URI from which to download the input video.

   options are key/value pairs:

   :test boolean

   Marks this as a test job. All jobs created under integration mode are test jobs.

   :download-connections int

   Specifies the number of connections used to download the input file.

   :region string

   Specifies the region in which processing.

   :outputs [m]

   Desired output files. Create with fn output.
  "
  [input & options]
  (let [job (merge {:api-key api-key :input input}
		   (apply array-map options))]
    (api-post "/jobs" job)))

(def job-id :id)
(defn output-ids [{outputs :outputs}] (map :id outputs))


;; Output Settings

(letfn [(options-map [& options] (apply array-map options))]
  (def output options-map)
  (def watermark options-map)
  (def thumbnails options-map))

