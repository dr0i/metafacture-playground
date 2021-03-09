(ns metafacture-playground.events
  (:require
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [metafacture-playground.db :as db]))

;; Editing input fields

(defn edit-value
  [db [_ field-name new-value]]
  (assoc-in db [:input-fields field-name] new-value))

(re-frame/reg-event-db
 :edit-input-value
 edit-value)

(defn load-sample
  [db _]
  (reduce
   (fn [db [k sample-v]]
     (assoc-in db [:input-fields k] sample-v))
   db
   db/sample-fields))

(re-frame/reg-event-db
  :load-sample
  load-sample)

(defn clear-all
  [db _]
  (let [fields [:data :flux :fix]]
    (reduce
     (fn [db field-to-empty]
       (assoc-in db [:input-fields field-to-empty] ""))
     db
     fields)))

(re-frame/reg-event-db
 :clear-all
 clear-all)

;; Processing

(defn process-response
  [db [_ response]]
  (-> db
      (assoc-in [:result :loading?] false)
      (assoc-in [:result :content] response)))

(re-frame/reg-event-db                   
 :process-response             
  process-response)

(defn bad-response
  [db [_ response]]
  (-> db
      (assoc-in [:result :loading?] false)
      (assoc-in [:result :content] "Bad response")))

(re-frame/reg-event-db
 :bad-response
 bad-response)

(defn process
  [{:keys [db]} [_ data flux fix]]
  {:http-xhrio {:method          :get
                :uri             "process"
                :params {:data data
                         :flux flux
                         :fix fix}
                :format (ajax/json-request-format)
                :response-format (ajax/text-response-format)
                :on-success      [:process-response]
                :on-failure      [:bad-response]}
   :db  (assoc-in db [:result :loading?] true)})

(re-frame/reg-event-fx
 :process
 process)

;; Initialize-db

(defn initialize-db
  [_ _]
  db/default-db)

(re-frame/reg-event-db
 ::initialize-db
 initialize-db)