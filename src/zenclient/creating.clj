(ns zenclient.creating
  "creating an encoding job"
  (:use zenclient.core
	[clojure.contrib.string :only (blank? lower-case)]))

(def audio-codec #{:mp3 :aac :vorbis :wma})
(def video-codec #{:h264 :theora :vp6 :vp8 :mpeg4 :wmv})
(def compatible {:h264 #{:aac :mp3} :theora #{:vorbis} :vp6 #{:aac :mp3} :vp8 #{:vorbis}})

(defn- starts-with-any [prefixes string]
  (some #(.startsWith string %) prefixes))

(defn- 

(defn create-job!
  "Creates a new encoding job.

   options are key/value pairs:

   :test boolean

   Marks this as a test job. All jobs created under integration mode are test jobs.

   :download-connections int

   Specifies the number of connections used to download the input file.

   :region string

   Specifies the region in which processing 
  "
  [input & options]
  (let [opts (apply array-map options)]
     (api-post "/jobs" (merge {:api-key api-key :input :input} opts))))
