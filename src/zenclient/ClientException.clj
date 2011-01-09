(ns zenclient
  (:use [clojure.string :only (join)]))

(gen-class
 :name zenclient.ClientException
 :extends RuntimeException
 :state state
 :init init
 :methods [(status [] Integer) (errors [] java.util.List)]
 :constructors {[Integer java.util.Collection] [String]})

(defn -init [status errors]
  [[(join " " errors)] {:status status :errors errors}])

(defn status [this] (:status (.state this)))

(defn errors [this] (:errors (.state this)))
  