(ns zenclient.progress
  "getting job progress"
  (:use zenclient.core))

(defn output-file-progress 
  "gets the current status of an output file"
  [output-id]
  (api-get (format "/outputs/%s/progress?api_key=%s" output-id api-key)))
      
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