(ns gosh-hawk.api
  "Clojure API for GoshawkDB. This namespace doesn't diverge too greatly from
  the GoshawkDB Java client which it is based upon. See gosh-hawk.experimental
  for functions that attempt to simplify the API for common actions. At some
  point common useful idioms will likely be collected under here."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec :as s])
  (:import [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]
           [io.goshawkdb.client Certs Connection ConnectionFactory GoshawkObjRef
            ,                   Transaction TransactionFunction]))

(s/def ::tx    #(instance? Transaction %))
(s/def ::tx-fn #(instance? TransactionFunction %))
(s/def ::obj   #(instance? GoshawkObjRef %))
(s/def ::conn  #(instance? Connection))
(s/def ::conn-factory #(instance? ConnectionFactory))

(defn connection-factory
  "Create a new connection factory. Generally used internally only"
  ([] (ConnectionFactory.))
  ([event-loop-group] (ConnectionFactory. event-loop-group)))

(s/fdef connection-factory
  :ret ::conn-factory)

(def cf (delay (connection-factory)))

(defn read-certs
  [fname]
  (doto (Certs.)
    (.parseClientPEM (io/reader fname))))

(defn connect
  ([client-cert-pem host]         (connect @cf client-cert-pem host 7894))
  ([client-cert-pem host port]    (connect @cf client-cert-pem host port))
  ([cf client-cert-pem host port] (.connect cf client-cert-pem host port)))

(defn tx-result
  [tr]
  {:aborted? (.isAborted tr)
   :success? (.isSuccessful tr)
   :cause    (.cause tr)
   :result   (.result tr)
   :txnid    (.txnid tr)})

(defn run-transaction
  "Runs a transaction against the connection. Takes a function which accepts
  the current transaction as its only argument. N.B. this is a normal Clojure
  function rather than a TransactionFunction from the Java API documentation."
  [conn f]
  (-> conn
      (.runTransaction
        (reify TransactionFunction
          (apply [^TransactionFunction this ^Transaction tx] (f tx))))
      tx-result))

(defmacro with-transaction
  "See run-transaction. Just a convenience macro wrapping that function. Binds
  `tx` as the current transaction in body."
  [conn & body]
  `(let [res# (run-transaction ~conn (fn [~'tx] ~@body))]
     (if (:success? res#)
       (:result res#)
       (throw (:cause res#)))))

(defn roots
  "Get all the roots for the given transaction"
  [tx]
  (.getRoots tx))

(defn root
  "Get one of the roots for the given transaction"
  [tx root-name]
  (get (roots tx) (name root-name)))

(defn to-utf8-byte-buffer
  "Convenience function to transform a Clojure object in to the 'printed'
  representation as a ByteBuffer"
  [o]
  (-> o
      pr-str
      (.getBytes StandardCharsets/UTF_8)
      ByteBuffer/wrap))

(defn from-utf8-byte-buffer
  "Convenience function to transform a ByteBuffer into the read form as a
  Clojure object"
  [bb]
  (some->> bb
           (.decode StandardCharsets/UTF_8)
           str
           edn/read-string))

(defn empty-byte-buffer
  []
  (ByteBuffer/allocate 0))

(defn get-obj
  [tx obj-ref]
  (.getObject tx obj-ref))

(defn obj-set
  "Set the bytes and references for the provided nodein the graph."
  [o bb refs]
  (.set o bb (into-array GoshawkObjRef refs)))

(defn references
  [o]
  (seq (.getReferences o)))

(defn create-object
  [tx bb refs]
  (.createObject tx bb (into-array GoshawkObjRef refs)))

(defn value
  [obj-ref]
  (.getValue obj-ref))

#_
(def certs (doto (Certs.)
             (.parseClientPEM (io/reader "/home/neil/Play/gosh-hawk/cert.pem"))))
#_
(def c (.connect @cf certs "localhost"))

#_
(with-transaction c (roots tx))

#_
(with-transaction c
  (let [r (root tx "rooty-mcrootface")]
    (obj-set r (to-utf8-byte-buffer {:foo 456}))))

#_
(with-transaction c
  (let [r (root tx "rooty-mcrootface")]
    (from-utf8-byte-buffer (value r))))

