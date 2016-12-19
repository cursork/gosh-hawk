(ns gosh-hawk.experimental
  "Experimental API for GoshawkDB. Everything in here may change or be removed
  without notice."
  (:refer-clojure :exclude [update])
  (:require [gosh-hawk.api :as api]))

(defn update-object
  "Update a single object by ID. Automatically wraps the function given in a
  transaction and passes in the object.

  Accepts a function which takes the object to be acted upon as an argument."
  [conn id f]
  (api/run-transaction
    conn
    (fn [txn]
      (let [obj (api/get txn id)]
        (f obj)))))

