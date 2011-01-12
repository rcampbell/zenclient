# Zenclient

[Zenclient](https://github.com/rcampbell/zenclient) is a simple [Clojure](http://clojure.org/) DSL for the [Zencoder](http://zencoder.com/) [API](http://zencoder.com/docs/api/).

## Basic Usage

    user> (use 'zenclient.core)
    nil
    user> (create-account! "foo@bar.com") ; use your own e-mail address
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

    user> (create-account! "foo@bar.com" :password "foobar" :newsletter false)
    {:api-key "cfe16172146139fce0ffda2565ba3035", :password "foobar"}
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

### [Leiningen](https://github.com/technomancy/leiningen) or [Cake](https://github.com/ninjudd/cake)

    [zenclient "1.1"]

### [Maven](http://maven.apache.org/) or [Ivy](http://ant.apache.org/ivy/)

    <repository>
      <id>clojars.org</id>
      <url>http://clojars.org/repo</url>
    </repository>

    <dependency>
      <groupId>zenclient</groupId>
      <artifactId>zenclient</artifactId>
      <version>1.1</version>
    </dependency>

## Notes

If you already have an account with an API key, you can set it using the `set-api-key!` fn:

    user> (set-api-key! "b46d1828001d4369a03b41d60846649f")
    #'zenclient.core/*api-key*

## TODO

1. DateTime functions throw NPEs if that state has not yet been reached, like `finished-at` during "processing". These should instead return `nil`. 

## License

Copyright (C) 2010 Robert Campbell

Distributed under the Eclipse Public License, the same as Clojure.
