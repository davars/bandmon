(ns bandmon.core
  (:gen-class)
  (:use clojure.java.shell)
  (:require [clojure.contrib.sql :as sql]))

(def settings
     {:db {:classname    "org.postgresql.Driver"
	   :subprotocol  "postgresql"
	   :subname      "//localhost:5432/bandmon"
	   :user         "postgres"
	   :password     "<password>"}
      :curl-cmd "/usr/bin/curl"
      :my-ip-url "http://www.whatismyip.com/automation/n09230945.asp"
      :ping-cmd "/bin/ping"
      ; http://www.measurementlab.net/measurement-lab-tools
      :pathload-cmd "/path/to/pathload2"
      :retries 3})

(def db (:db settings))

(defn public-ip []
  {:host_ip
   (:out (sh (:curl-cmd settings) (:my-ip-url settings)))})

(defn ping-results [output]
  (map read-string
       (lazy-cat (rest (re-find
			#"(\d*) packets transmitted, (\d*) received" 
			output))
		 (rest (re-find
			#"mdev = ([^/]*)/([^/]*)/([^/]*)/([^ ]*)"
			output)))))

(defn ping-test []
  (let [results (-> (sh (:ping-cmd settings)
			"-n" "-c" "20" "-i" "0.2" "8.8.8.8")
		    :out
		    ping-results)
	[sent rcvd min avg max mdev] results]
    {:ping_sent sent
     :ping_rcvd rcvd
     :ping_min min
     :ping_avg avg
     :ping_max max
     :ping_mdev mdev}))

(defn extract-single [result]
  (let [meas (read-string
	      (last 
	       (re-find 
		#"Available bandwidth is at (least|most) ([^ ]*) \(Mbps\)\n" 
		result)))]
    (if (.contains result "most")
      [nil meas]
      [meas nil])))

(defn extract-range [result]
  (sort 
   (map read-string 
	(rest (re-find
	       #"Available bandwidth range : ([^ ]*) - ([^ ]*) \(Mbps\).\n"
	       result)))))

(defn extract-result [prefix output]
  (let [pattern (re-pattern (str prefix "[^\n]*\n([^\n]*\n)"))
	result-line (->> output
			 (re-find pattern)
			 last)]
    (if (.contains result-line "range")
      (extract-range result-line)
      (extract-single result-line))))

(defn upstream [output]
  (extract-result "Upstream Measurement" output))

(defn downstream [output]
  (extract-result "Downstream Measurement" output))

(defn test-server [output]
  (last 
   (re-find 
    #"Connected to MLab Server: ([\d\.]*)"
    output)))

(defn now []
  (java.sql.Timestamp. 
   (System/currentTimeMillis)))

(defn bandwidth-test []
  (let [start (now)
	process (sh (:pathload-cmd settings))
	result (:out process)
	end (now)
	up-res (upstream result)
	down-res (downstream result)]
    {:start_time start
     :end_time end
     :mlab_ip (test-server result)
     :upstream_lo (first up-res)
     :upstream_hi (second up-res)
     :downstream_lo (first down-res)
     :downstream_hi (second down-res)}))

(defn record-result [result]
  (sql/with-connection db 
    (sql/insert-records
     :test_results
     result)))

(defn record-exception [e]
  (sql/with-connection db 
    (sql/insert-records
     :exceptions
     {:thrown_at (now)
      :ex_type (.getCanonicalName (class e))
      :message (.getMessage e)
      :stack_trace (let [sw (java.io.StringWriter.)]
		     (.printStackTrace e (java.io.PrintWriter. sw))
		     (str sw))})))

(defn attempt [thunk]
  (try
   (thunk)
   (catch Exception e 
     (record-exception e)
     nil)))

(defn try-n [n thunk]
  (if-let [result (attempt thunk)]
    result
    (if (> n 1)
      (recur (dec n) thunk)
      nil)))
  
(defn -main []
  (let [start (now)
	run (fn [f] (try-n (:retries settings) f))
    	results (doall (map run [bandwidth-test ping-test public-ip]))
    	end (now)]
    (record-result 
     (apply 
      merge 
      {:start_time start :end_time end} results))))

(defn init-db []
  (sql/with-connection db
    (sql/do-commands
     "
CREATE TABLE test_results
(
  id serial NOT NULL,
  host_ip text,
  start_time timestamp with time zone NOT NULL,
  end_time timestamp with time zone NOT NULL,
  upstream_lo double precision,
  upstream_hi double precision,
  downstream_lo double precision,
  downstream_hi double precision,
  mlab_ip text,
  ping_sent integer,
  ping_rcvd integer,
  ping_min double precision,
  ping_avg double precision,
  ping_max double precision,
  ping_mdev double precision,
  CONSTRAINT pk_test_results PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
)"
"ALTER TABLE test_results OWNER TO postgres")))
