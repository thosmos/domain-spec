(ns domain-spec.core
  (:require [clojure.tools.logging :as log :refer [debug info warn error]]
            [domain-spec.literals :as literals]
            [clojure.java.io :as io]
            [datomic.api :as d]
            [io.rkn.conformity :as c]
            [datascript.core :as ds])
  (:import java.util.Date))

(def uri "datomic:mem://test-domain-spec")

(defn cx
  ([] (cx uri))
  ([uri]
   (d/create-database uri)
   (d/connect uri)))

(defn load-schemas [filename]
  (clojure.edn/read-string
    {:readers {'domain-spec/schema-tx #'literals/schema-tx}}
    (slurp (io/file filename))))

(defn load-schemas-ds [filename]
  (clojure.edn/read-string
    {:readers {'domain-spec/schema-tx-ds #'literals/schema-tx-ds}}
    (slurp (io/file filename))))

(defn specs->db-schema-terse [specs]
  (vec
    (apply concat
      (for [{:keys [entity/attrs entity/pks entity/name]} specs]
        (vec
          (for [{:keys [attr/key attr/cardinality attr/type attr/primary attr/unique attr/identity attr/required
                        attr/doc attr/component attr/noHistory]} attrs]
            (vec
              (concat
                [key cardinality type]

                (when identity
                  [:identity])

                (when component
                  [:component])

                (when noHistory
                  [:noHistory])

                ;(when primary?
                ;  [:index])

                (when (and
                        (not= type :string)
                        (not= type :ref)
                        (not identity))
                        ;(not primary?)
                  [:index])

                (when unique
                  [:unique])

                [(or doc "")]))))))))

(defn spec-specs []
  (c/read-resource "spec-spec.edn"))

(defn new-specs-ds []
  (->
    (spec-specs)
    specs->db-schema-terse
    literals/schema-tx-ds
    ds/create-conn))

(defn sort-specs [specs]
  (let [specs (for [spec specs]
                (let [attrs (vec (sort-by :attr/name (:entity/attrs spec)))]
                  (assoc spec :entity/attrs attrs)))
        specs (vec (sort-by :entity/name specs))]
    specs))

(defn get-specs
  "pull all entity and associated attribute specs from a datascript datasource"
  [specs-ds]
  (sort-specs
    (ds/q '[:find [(pull ?e [*]) ...]
            :where [?e :entity/ns]]
      @specs-ds)))

(defn get-schema [db attr]
  (d/q '[:find [(pull ?e [:db/ident * {:db/unique [*]} {:db/cardinality [:db/ident]} {:db/valueType [:db/ident :fressian/tag]}]) ...]
         :in $ ?attr
         :where
         [?e :db/ident ?attr]]
    db attr))

(defn get-schemas [db]
  (let [system-ns #{"db" "db.type" "db.install" "db.part"
                    "db.lang" "fressian" "db.unique" "db.excise"
                    "db.cardinality" "db.fn" "db.sys" "db.alter" "db.bootstrap"
                    "conformity"}]
    (d/q '[:find [(pull ?e [:db/ident * {:db/unique [*]} {:db/cardinality [:db/ident]} {:db/valueType [:db/ident :fressian/tag]}]) ...]
           :in $ ?system-ns
           :where
           [?e :db/ident ?ident]
           [(namespace ?ident) ?ns]
           [((comp not contains?) ?system-ns ?ns)]]
      db system-ns)))

(defn datomic->terse-schema [db]
  (debug "datomic->simple-schema")
  (let [schemas (get-schemas db)]
    (for [{:keys [db/ident db/unique db/valueType db/cardinality db/doc db/index db/fulltext db/noHistory db/isComponent] :as sch} schemas]
      (let [type   (keyword (name (:db/ident valueType)))
            card   (-> cardinality :db/ident name keyword)
            result [ident card type]]

        (cond-> result

          unique
          (conj (-> unique :db/ident name keyword))

          index
          (conj :index)

          fulltext
          (conj :fulltext)

          noHistory
          (conj :noHistory)

          isComponent
          (conj :component)

          doc
          (conj doc))))))





(comment
  (in-ns 'domain-spec.core)
  (update-cx uri)

  (c/read-resource "spec-schema.edn")

  (def norms-map (c/read-resource "spec-examples.edn"))
  (c/ensure-conforms @cx norms-map)

  (defn empty-model-cx []
    (->
      (c/read-resource "spec-schema.edn")
      literals/schema-tx-ds
      ds/create-conn))


  (def model
    [{;:db/id       "sitevisit spec"
      :entity/name "Site Visit"
      :entity/ns   :entity.ns/sitevisit
      :entity/attrs
                   [{:attr/name        "SiteVisit ID"
                     :attr/key         :sitevisit/SiteVisitID
                     :attr/db.type     :db.type/string
                     :attr/required?   true
                     :attr/cardinality :one
                     :attr/toggles     [:index]
                     :attr/description "a general ID field unique to this site visit"}
                    {:attr/name        "Date"
                     :attr/key         :sitevisit/SiteVisitDate
                     :attr/db.type     :db.type/instant
                     :attr/required?   true
                     :attr/cardinality :one
                     :attr/toggles     [:index]
                     :attr/description "the date of this site visit"}]}])




  (def model-db
    "A DataScript database value, holding a representation of our Domain Model."
    (ds/db-with
      (ds/db (empty-model-cx))
      ;; Composing DataScript transactions is as simple as that: concat
      model)))

