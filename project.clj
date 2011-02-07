(defproject zenclient "1.2-SNAPSHOT"
  :description "A simple Clojure DSL for the Zencoder API"
  :url "https://github.com/rcampbell/zenclient"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}  
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [joda-time/joda-time "1.6.2"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
		     [lein-clojars "0.6.0"]]
  :aot [zenclient.java]
  :jar-exclusions [#"\.DS_Store"]
  :jvm-opts ["-XX:PermSize=128M"
	     "-XX:MaxPermSize=256M"
	     "-noverify"
	     ~(format "-javaagent:%s/jrebel.jar"
		      (System/getenv "JREBEL_HOME"))])