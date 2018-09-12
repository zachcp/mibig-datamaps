(ns mibig-datamaps.core
  (:gen-class)
  (:require [datamaps.core :as dm]
            [datamaps.facts :as df]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [cheshire.core :refer [parse-stream]]))


;; load in the data with datamaps
(defn isjson? [f]
  "check to see if a file is json"
  (let [name (.getName f)]
    (if (.endsWith name ".json")
      true
      false)))

(def jsonfiles
  (->> (clojure.java.io/file "data")
       (file-seq)
       (filter isjson?)
       (filter #(> (.length %) 0))))


(comment
  (def mibigdata
    (into [] (map #(parse-stream (clojure.java.io/reader %) true) jsonfiles)))


  ; create a fact database
  (def mibigdata-dm  (dm/facts mibigdata))

  ; get compounds of Mibig Acccession
  (dm/q '[:find ?mibig ?c
          :where
          [?e    :compounds ?cmps]
          [?e    :mibig_accession ?mibig]
          [?cmps :compound ?c]]
        mibigdata-dm)

  ;; get new mibig data install
  (def mibigmols
    (into []
      (dm/q '[:find ?mibig ?c ?accession
              :where
              [?e    :mibig_accession ?mibig]
              [?e    :compounds ?cmps]
              [?cmps :compound ?c]
              [?e    :loci ?loci]
              [?loci  :nucl_acc ?nucl_acc]
              [?nucl_acc :Accession ?accession]]

            mibigdata-dm)))


  ;(spit "/Users/zach/Downloads/mibigdata.edn" (do mibigmols))
  (with-open [writer (io/writer "/Users/zach/Downloads/mibigdata.csv")]
    (csv/write-csv writer mibigmols))


  ; get compounds of Mibig Acccession
  (dm/q '[:find ?c
          :where [?e    :mibig_accession "BGC0001803"]
          [?e    :compounds ?cmps]
          [?cmps :compound ?c]]
        mibigdata-dm)

  ; get counts of each Mibig class
  (dm/q '[:find  ?bs (count ?bs)
          :where
          [?e   :mibig_accession ?mb]
          [?e   :biosyn_class ?bs]
          [?e   :compounds ?cmps]
          [?cmps :compound ?c]]
        mibigdata-dm)

  ; get Mibig ID and Compound names of Polyketides
  (dm/q '[:find ?mb ?c
          :where
          [?e   :mibig_accession ?mb]
          [?e   :biosyn_class "Polyketide"]
          [?e   :compounds ?cmps]
          [?cmps :compound ?c]]
        mibigdata-dm)

  ; get Mibig ID and Compound names of Terpenes
  (dm/q '[:find ?mb ?c
          :where
          [?e   :mibig_accession ?mb]
          [?e   :biosyn_class "Terpene"]
          [?e   :compounds ?cmps]
          [?cmps :compound ?c]]
        mibigdata-dm)

  ; get all PKS subclasses
  (->
    (dm/q '[:find ?pksub
            :where
            [?e    :Polyketide  ?pk]
            [?pk   :pk_subclass ?pksub]]

          mibigdata-dm)
    flatten
    set)

  ; get all NRPS subclasses
  (->
    (dm/q '[:find ?nrpsub
            :where
            [?e    :NRP  ?nrp]
            [?nrp  :subclass ?nrpsub]]
          mibigdata-dm)
    flatten
    set)

  ; get all Molecules that have Tyrocine-encoding Adenylation Domains
  (->
    (dm/q '[:find ?mb ?c
            :where
            [?e       :mibig_accession ?mb]
            [?e       :compounds ?cmps]
            [?cmps    :compound ?c]
            [?e       :NRP  ?nrp]
            [?nrp     :nrps_genes ?nrpgene]
            [?nrpgene :nrps_module ?nrpsmod]
            [?nrpsmod :a_substr_spec ?adom]
            [?adom    :prot_adom_spec "Tyrosine"]]
          mibigdata-dm)
    set)

  ; All Contributers
  (->
    (dm/q '[:find ?name ?institute
            :where
            [?person :submitter_name ?name]
            [?person :submitter_institution ?institute]]
          mibigdata-dm)
    set)

  ; Contributers by Frequency
  (->
    (dm/q '[:find (count ?name) ?name ?institute
            :where
            [?person :submitter_name ?name]
            [?person :submitter_institution ?institute]]
          mibigdata-dm)
    sort)

  ; Misspelling the Rockefeller University
  (->
    (dm/q '[:find  ?institute (count ?institute)
            :where
            [?person :submitter_name "Sean Brady"]
            [?person :submitter_institution ?institute]]
          mibigdata-dm)
    sort)

  ; Look for missing Biosynthesis
  ; This doesn't work. can't negate
  ; https://github.com/tonsky/datascript/wiki/Tips-&-tricks
  (dm/q '[:find ?mb
          :in $
          :where
          [?e :mibig_accession ?mb]
          [(get-else $ ?e :NRP nil) ?u]
          [(nil? ?u)]]
        mibigdata-dm)

  ; Doesn't work datomic style
  ; http://docs.datomic.com/query.html
  (dm/q '[:find ?mb
          :where
          [?e :mibig_accession ?mb]
          [(missing? $ ?e :NRP)]]
        mibigdata-dm))

  ; Try the Datomic API to
  ;(df/fact-partition mibigdata-dm)
  ;(df/attr-partition mibigdata-dm)

