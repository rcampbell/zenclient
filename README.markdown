# Zenclient

[Zenclient](https://github.com/rcampbell/zenclient) is a simple [Clojure](http://clojure.org/)  for the [Zencoder](http://zencoder.com/) [API](http://zencoder.com/docs/api/).

## Basic Usage

    user> (use 'zenclient.core)
    nil
    user> (create-account! "foo@bar.com")
    {:api-key "8333b48957189f68c2fc57364e75df16", :password "NEkDJZBWyzNj"}
    user> (def job (create-job! "http://bit.ly/fzkUTT"))
    #'user/job
    user> (job id)
    991057
    user> (->> job details finished?)
    true
    user> (->> job details finished-at)
    #<DateTime 2011-01-12T11:09:20.000+01:00>
    user> (clojure.java.browse/browse-url (->> job outputs first url))
    "https://zencoder-live.s3.amazonaws.com..."

## Advanced Usage

    user> (account-active?)
    true
    user> (integration-mode?)
    true
    user> (minutes-used)
    0
    user> (def job (create-job! "http://bit.ly/fzkUTT"
                                :region "us"
                                :download-connections 10
                                :outputs [(+output :label "iPhone" :width 480 :height 320)
                                          (+output :label "Web" :width 1280 :height 720)]
                                :watermark (+watermark :x 20 :y 20)
                                :thumbnails (+thumbnails :number 3)))
    #'user/job
    user> (->> job details outputs second progress state)
    "finished"
    user> (->> job details outputs second progress finished?)
    true
    user> (->> job details outputs second finished-at)
    #<DateTime 2011-01-12T13:38:35.000+01:00>
    user> (->> job details outputs first label)
    "iPhone"
    user> (map id (list-jobs))
    (991936 991085 991057)			  
    user> (map (juxt id state) (list-jobs))
    ([991936 "finished"] [991085 "finished"] [991057 "failed"])
    user> (resubmit-job! 991057)
    true

## Installation



## License

Copyright (C) 2010 Robert Campbell

Distributed under the Eclipse Public License, the same as Clojure.
