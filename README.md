# thosmos/domain-spec

[![Clojars Project](https://img.shields.io/clojars/v/thosmos/domain-spec.svg)](https://clojars.org/thosmos/domain-spec)

A minimal abstraction for defining domain models and even domain model specs themselves - quite meta ...

## Usage

* Define a spec.edn something like: 

```clojure
[{:db/id         "person",
  :db/ident      :thosmos.entity.spec/person
  :entity/name   "person",
  :entity/ns     :thosmos.entity.ns/person,
  :entity/attrs  [#:attr{:name        "id",
                         :key         :person/id,
                         :cardinality :one,
                         :type        :long,
                         :identity    true}
                  #:attr{:name        "name",
                         :key         :person/name,
                         :cardinality :one,
                         :type        :string}
                  #:attr{:name "books"
                         :key   :person/books
                         :cardinality :many,
                         :type  :ref
                         :ref :thosmos.entity.spec/book}]}
 {:db/id         "book",
  :db/ident      :thosmos.entity.spec/book,
  :entity/name   "book",
  :entity/ns     :thosmos.entity.ns/book,
  :entity/attrs  [#:attr{:name        "id",
                         :key         :book/id,
                         :cardinality :one,
                         :type        :long,
                         :identity    true}
                  #:attr{:name        "name",
                         :key         :book/name,
                         :cardinality :one,
                         :type        :string}]}]
```

then use the various functions to convert into terse, datomic, or datascript schema

## License

Copyright © 2018 Thomas Spellman

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
