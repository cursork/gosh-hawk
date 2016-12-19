(ns gosh-hawk.bank
  (:require [gosh-hawk.api :refer :all]))

(defn create-bank
  [conn]
  (with-transaction conn (root tx :rooty-mcrootface)))

(defn add-account
  [conn bank-obj name]
  (with-transaction conn
    (let [b       (get-obj tx bank-obj)
          accs    (references b)
          acc-no  (count accs)
          new-acc (create-object
                    tx
                    (to-utf8-byte-buffer
                      {:name name
                       :account-number acc-no
                       :balance 0})
                    [])]
      (obj-set b (empty-byte-buffer) (conj (vec accs) new-acc))
      acc-no)))

(defn get-account
  [conn bank-obj acc-num]
  (with-transaction conn
    (let [b    (get-obj tx bank-obj)
          accs (references b)
          acc  (nth accs acc-num)]
      (-> acc
          value
          from-utf8-byte-buffer))))

;(defn transfer-from
;  [conn bank src dest amount]
;  (with-transaction conn
;    (let [b (get-obj tx bank-obj)
;          t {:time (.getTime (Date.))
;             :amount amount}
;          tobj 

(comment
  (def conn (.connect @cf certs "localhost"))
  (def banque (create-bank conn))
  (add-account conn banque "Blahblah")
  (get-account conn banque 0)
         )
