# [domain-spec](https://github.com/thosmos/domain-spec)

## [Unreleased]
### Planned

### Changed
### Removed
- Deprecated spec-schemas.edn
### Fixed
### Added

## [0.1.1] - 2019-02-02
### Changed
- definitely breaking changes ...
- use specs to define specs ... so meta
- Removed "is" prefix and "?" suffix from attr spec keys: identity, unique, primary, required, fulltext, derived, component
- changed `attr/copund-key` to `attr/compoundKey` and similar other removals of "-"

## 0.1.0 - 2018-09-02
### Added
- intitial checkin ...
- basic ability to convert between domain-spec and datomic(-terse) schema formats
- used as the basis for [riverdb.org graphql server](https://gitlab.com/riverdb/riverdb-graphql) auto-generated GraphQL queries

[Unreleased]: https://github.com/thosmos/domain-spec/compare/0.1.1...HEAD
[0.1.1]: https://github.com/thosmos/domain-spec/compare/0.1.0...0.1.1

#### Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).