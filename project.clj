(defproject zenclient "1.2-SNAPSHOT"
  :description "A simple Clojure DSL for the Zencoder API"
  :url "https://github.com/rcampbell/zenclient"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}  
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.3"]
                 [slingshot "0.10.2"]
                 [clj-http "0.3.5"]
		 [joda-time/joda-time "2.1"]]
  :dev-dependencies [[lein-clojars "0.6.0"]]
  :aot [zenclient.java]
  :jar-exclusions [#"\.DS_Store"]
  :jvm-opts ["-XX:PermSize=128M"
	     "-XX:MaxPermSize=256M"
	     "-noverify"
	     ~(format "-javaagent:%s/jrebel.jar"
		      (System/getenv "JREBEL_HOME"))])
