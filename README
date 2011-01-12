# zenclient

Zenclient is a simple [Clojure](http://clojure.org/) wrapper for the [Zencoder](http://zencoder.com/) [API](http://zencoder.com/docs/api/).

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



## Installation



## License

Copyright (C) 2010 Robert Campbell

Distributed under the Eclipse Public License, the same as Clojure.
