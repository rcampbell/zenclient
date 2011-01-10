(ns zenclient.core
  (:use [clojure.walk :only (postwalk)]
	[clojure.string :only (join)]
	[clojure.contrib.string :only (replace-char)]
	[clojure.contrib.json :only (read-json json-str)]
	[clojure.contrib.condition :only (raise)]
	[clojure.contrib.http.agent :only (http-agent string success? status)]))

(def ^{:private true} api "https://app.zencoder.com/api")

; (def *api-key* "c46d1828001d4969a03b45d60846649f")

(letfn [(rename [a b k] (keyword (replace-char a b (name k))))
	(swap [a b]
	      (letfn [(f [[k v]] (if (keyword? k) [(rename a b k) v] [k v]))]
		(fn [m] (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))]
  (def dash->underscore (swap \- \_))
  (def underscore->dash (swap \_ \-)))

(defn- debug [s] (println s) s)

(let [parse (comp read-json debug string)
      handle (fn [agent] (let [body (parse agent)]
			   (if (success? agent)
			     body
			     (let [{errors :errors} body]
			       (raise :message (join " " errors)
				      :status (status agent)
				      :errors errors)))))
      headers {"Accept" "application/json"
	       "Content-Type" "application/json"}]
  (defn api-get [path]
    (let [uri (str api path)]
      (handle (http-agent uri :headers headers))))
  (defn api-post
    ([path] (api-post path {}))
    ([path body]
       (let [uri (str api path)
	     json-body (json-str (dash->underscore body))]
	 (handle (http-agent uri :method "POST" :headers headers :body json-body)))))
  (defn api-delete [path]
    (let [uri (str api path)]
      (handle (http-agent uri :method "DELETE")))))

(defn ci= [l r] (.equalsIgnoreCase l r))

(defn lazy-loader [f]
  (fn
    ([k] ((f) k))
    ([k src] (if (instance? java.util.Map src) (src k) ((f src) k)))))







