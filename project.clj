(defproject thosmos/domain-spec "0.1.1"
  :description "A minimal abstraction for defining domain models and even domain model specs themselves - quite meta ..."
  :url "https://github.com/thosmos/domain-spec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"}}

  :plugins [[lein-tools-deps "0.4.3"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]})
