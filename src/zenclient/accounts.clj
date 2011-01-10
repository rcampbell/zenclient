(ns zenclient.accounts
  "working with accounts"
  (:use zenclient.core
	[clojure.contrib.condition :only (handler-case)]))

(declare *api-key*)

;; Create an Account

(defn create-account! 
  "Creates a new account under the Test (Free) plan.

   options are key/value pairs:

   :password string

   If you don't specify a password, one will be generated for you.

   :newsletter boolean

   Subscription to the newsletter is on by default.

   :affiliate-code string

   If you have been assigned an affiliate code, you may pass it here
   to note the account.

   :terms-of-service boolean
 
   You must specify agreement with the terms of service.
  "
  [email & options]
  (let [opts (apply array-map options)]
    (api-post "/account" (merge {:email email :terms-of-service "1"} opts))))

(def api-key :api_key)
(def password :password)
     

;; Account Details

(defn account-details []
  ((api-get (format "/account?api_key=%s" *api-key*))))

(let [select (lazy-loader account-details)]
  (def account-state (partial select :account_state))
  (letfn [(state= [v] (fn
			([] (ci= v (account-state)))
			([m] (ci= v (account-state m)))))]
    (def account-active? (state= "active"))
    (def account-stopped? (state= "stopped"))
    (def account-suspended? (state= "suspended"))
    (def account-cancelled? (state= "cancelled")))
  (def plan (partial select :plan))
  (def minutes-used (partial select :minutes_used))
  (def minutes-included (partial select :minutes_used))
  (def billing-state (partial select :billing_state))
  (letfn [(state= [v] (fn
			([] (ci= v (billing-state)))
			([m] (ci= v (billing-state m)))))]
    (def billing-active? (state= "active"))
    (def billing-past-due? (state= "past due"))
    (def billing-cancelled? (state= "cancelled")))
  (def privacy-mode? (partial select :privacy_mode))
  (def integration-mode? (partial select :integration_mode))
  (def live-mode? (complement integration-mode?)))


;; Account Integration Mode

(defn integration-mode!
  "turns on integration mode" []
  (api-get "/account/integration"))

(defn live-mode!
  "turns off integration mode" []
  (api-get "/account/live"))