# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1-SNAPSHOT] - 2025-01-07

### Changed

- Minimum Java version changed from 8 to 11
- Upgraded JDOM from 1.1.3 to JDOM2 2.0.6.1
- Upgraded Apache Commons CLI from 1.4 to 1.9.0
- Upgraded Apache Commons Lang from 3.12.0 to 3.17.0
- Refactored `AndroidXmlOutputter` to use composition instead of inheritance (internal change)

### Added

- Unit test suite with 86% code coverage
  - `AndroidXmlOutputterTest` - 31 tests for XML formatting
  - `MainTest` - 13 tests for CLI functionality
- JaCoCo for code coverage reporting
- Spotless Maven plugin with Google Java Format for code formatting
- Comprehensive README with build, test, and usage instructions
- Sample XML test resources

### Updated Plugins

- Maven Assembly Plugin: 3.1.1 → 3.7.1
- Maven Surefire Plugin: 3.2.5 → 3.5.2
- JaCoCo Maven Plugin: 0.8.11 → 0.8.12
- Spotless Maven Plugin: 2.43.0 → 2.44.0
- Google Java Format: 1.19.2 → 1.25.2

## [1.0.0] - Initial Release

### Added

- Initial implementation of Android XML formatter
- Command-line interface with configurable options:
  - `--indention` - Set indentation level
  - `--attribute-indention` - Set attribute indentation level
  - `--attribute-order` - Configure attribute ordering priority
  - `--attribute-sort` - Enable alphabetical attribute sorting
  - `--namespace-order` - Configure namespace ordering priority
  - `--namespace-sort` - Enable alphabetical namespace sorting
- Default attribute ordering: `id`, `layout_width`, `layout_height` first
- Default namespace ordering: `android` namespace first
