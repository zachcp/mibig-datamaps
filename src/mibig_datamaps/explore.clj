(ns mibig-datamaps.explore
  (:require [datamaps.core :as dm]
            [datamaps.facts :as df]
            [clojure.java.io :as io]
            [cheshire.core :refer [parse-stream]]))


(defn location [m]
  (->> (:locations m)
       (filter :primary)
       first))


(defn user->demographics [m]
  (let [loc (location m)]
    {:demo/firstname (:firstname m)
     :demo/lastname (:lastname m)
     :demo/city (:city loc)
     :demo/state (:state loc)
     :demo/zip (:zip loc)}))

(map user->demographics test-users)


(def test-users
  [{:firstname "Dan"
    :lastname "Joli"
    :cats ["islay" "zorro" "lily"]
    :dogs ["ginny"]
    :locations [{:city "Annapolis"
                 :state "MD"
                 :zip 21409
                 :primary true}
                {:city "Baltimore"
                 :state "MD"
                 :zip 21224}]}


   {:firstname "Casey"
    :lastname "Joli"
    :cats ["islay" "zorro" "lily"]
    :dogs ["ginny"]
    :locations [{:city "Salisbury"
                 :state "MD"
                 :zip 21256
                 :primary nil}
                {:city "Annapolis"
                 :state "MD"
                 :zip 21409
                 :primary true}]}])



(def demo-fields
  [:demo/firstname :demo/lastname :demo/city :demo/state :demo/zip])

(->> test-users
     dm/facts
     (dm/q '[:find ?f ?l ?c ?s ?z
             :where
             [?e :firstname ?f]
             [?e :lastname ?l]
             [?e :locations ?loc]
             [?loc :city ?c]
             [?loc :state ?s]
             [?loc :primary true]
             [?loc :zip ?z]])
     (map (partial zipmap demo-fields)))



;; load up some arbitrary maps

(def test-users
  [{:firstname "Dan"
    :lastname "Joli"
    :cats ["islay" "zorro" "lily"]
    :dogs ["ginny"]
    :location {:city "Annapolis"
               :state "MD"
               :neighborhood {:name "Baltimore"
                              :zip 21224}}}
   {:firstname "Casey"
    :lastname "Joli"
    :cats ["islay" "zorro" "lily"]
    :dogs ["ginny"]
    :location {:city "Salisbury"
               :state "MD"
               :neighborhood {:name "Cape"
                              :zip 21409}}}
   {:firstname "Mike"
    :lastname "Joli"
    :dogs ["penny" "stokely"]
    :location {:city "Annapolis"
               :state "MD"
               :neighborhood {:name "West Annapolis"
                              :zip 21401}}}
   {:firstname "Katie"
    :lastname "Joli"
    :dogs ["penny" "stokely"]
    :location {:city "Annapolis"
               :state "MD"
               :neighborhood {:name "West Annapolis"
                              :zip 21401}}}])

;; convert them into facts

(def facts (dm/facts test-users))

;; now we have a queryable set of facts!!

(dm/q '[:find [?f ...]
        :where
        [?e :firstname ?f]
        [?e :location ?l]
        [?l :neighborhood ?n]
        [?n :zip 21401]] facts) ;;=> ["Mike" "Katie"]
