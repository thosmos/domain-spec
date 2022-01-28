(ns domain-spec.literals
  (:require [datomic.api :as d]))


;; copied from com.cognitect.vase.literals - https://github.com/cognitect-labs/pedestal.vase

;; Schema literals
;; ---------------
(def accepted-schema-toggles #{:unique :identity :index :fulltext :component :noHistory})
(def accepted-kinds          #{:keyword :string :boolean :long :bigint :float :double :bigdec :ref :instant :uuid :uri :bytes})
(def accepted-cards          #{:one :many})

(def schema-tx-usage
  "#domain-spec/schema-tx[[ _attribute-name_ _cardinality_ _type_ _toggles_* _docstring_ ]* ]")

(defmacro schema-problem
  [flavor actual]
  `(str "#riverdb/schema-tx must look like this:\n\n"
     schema-tx-usage
     "\n\n"
     ~flavor
     "\n\n"
     "I got:\n\n"
     (pr-str ~actual)))

(defmacro schema-assert
  [f flavor emit]
  `(when-not ~f
     (throw (AssertionError. (schema-problem ~flavor ~emit)))))

(defn parse-schema-vec
  [s-vec]
  (schema-assert (every? keyword? (butlast s-vec))
    "All of _attribute-name_, _cardinality_, _type_, and _toggles_ must be Clojure keywords."
    s-vec)
  (let [doc-string            (last s-vec)
        ;[ident card kind & _] (take 3 s-vec)
        [ident card kind & _] (take 3 s-vec)
        opt-toggles           (take-while keyword? (drop 3 s-vec))]
    (schema-assert (string? doc-string) "The last thing in the vector must be a docstring." s-vec)
    (schema-assert (every? #(contains? accepted-schema-toggles %) opt-toggles)
      (str "Short schema toggles must be taken from " accepted-schema-toggles) opt-toggles)
    (schema-assert (contains? accepted-kinds kind) (str "The value type must be one of " accepted-kinds) kind)
    (schema-assert (contains? accepted-cards card) (str "The cardinality must be one of " accepted-cards) card)
    (merge {:db/id                 (d/tempid :db.part/db)
            :db/ident              ident
            :db/valueType          (keyword "db.type" (name kind))
            :db/cardinality        (keyword "db.cardinality" (name card))
            :db.install/_attribute :db.part/db
            :db/doc                doc-string}
      (reduce (fn [m opt]
                (merge m (case opt
                           :unique     {:db/unique :db.unique/value}
                           :identity   {:db/unique :db.unique/identity}
                           :index      {:db/index true}
                           :fulltext   {:db/fulltext true
                                        :db/index    true}
                           :component  {:db/isComponent true}
                           :noHistory {:db/noHistory true}
                           nil)))
        {}
        opt-toggles))))

(defn parse-schema-vec->spec
  [s-vec]
  (schema-assert (every? keyword? (butlast s-vec))
    "All of _attribute-name_, _cardinality_, _type_, and _toggles_ must be Clojure keywords."
    s-vec)
  (let [doc-string            (last s-vec)
        ;[ident card kind & _] (take 3 s-vec)
        [ident card kind & _] (take 3 s-vec)
        opt-toggles           (take-while keyword? (drop 3 s-vec))]
    (schema-assert (string? doc-string) "The last thing in the vector must be a docstring." s-vec)
    (schema-assert (every? #(contains? accepted-schema-toggles %) opt-toggles)
      (str "Short schema toggles must be taken from " accepted-schema-toggles) opt-toggles)
    (schema-assert (contains? accepted-kinds kind) (str "The value type must be one of " accepted-kinds) kind)
    (schema-assert (contains? accepted-cards card) (str "The cardinality must be one of " accepted-cards) card)
    (let [entity-ns (keyword "entity.ns" (namespace ident))
          attr-map {:attr/key ident
                    :attr/doc doc-string
                    :attr/type kind
                    :attr/cardinality card}
          attr-map (if opt-toggles
                     (merge
                       attr-map
                       {:attr/toggles (vec opt-toggles)}
                       (reduce (fn [m opt]
                                 (merge m (case opt
                                            :unique {:attr/unique true}
                                            :identity {:attr/identity true}
                                            nil)))
                         {}
                         opt-toggles))
                     attr-map)]
      {:entity/ns    entity-ns
       :entity/attrs [attr-map]})))

(defn parse-schema-vec-ds
  [s-vec]
  (let [s-vec (parse-schema-vec s-vec)
        ident (:db/ident s-vec)
        type (:db/valueType s-vec)
        keys  [:db/unique
               :db/cardinality
               :db/index
               :db/fulltext
               :db/doc
               :db/isComponent]

        keys (if (= type :db.type/ref)
               (conj keys :db/valueType)
               keys)
        spec (select-keys s-vec keys)]
    [ident spec]))


(defn schema-tx [form]
  (schema-assert (vector? form) "The top level must be a vector." form)
  (schema-assert (every? vector? form) "The top level vector must only contain other vectors" form)
  (mapv parse-schema-vec form))

(defn schema-tx->spec [form]
  (schema-assert (vector? form) "The top level must be a vector." form)
  (schema-assert (every? vector? form) "The top level vector must only contain other vectors" form)
  (mapv parse-schema-vec->spec form))

(defn schema-tx-ds [form]
  (schema-assert (vector? form) "The top level must be a vector." form)
  (schema-assert (every? vector? form) "The top level vector must only contain other vectors" form)
  (into {} (map parse-schema-vec-ds form)))

(defn squuid [arg]
  (d/squuid))