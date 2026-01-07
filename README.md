# android-xml-formatter

Formats XML files according to Android Studio's default formatting rules. By default, also re-orders the attributes to specify the `android:id`, `android:layout_width` and `android:layout_height` first. This can be turned off with a command line option.

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building

To build the project and create an executable JAR:

```bash
mvn clean package
```

This creates two JAR files in the `target/` directory:
- `android-xml-formatter-1.1-SNAPSHOT.jar` - Basic JAR
- `android-xml-formatter-1.1-SNAPSHOT-jar-with-dependencies.jar` - Executable JAR with all dependencies

## Running Tests

To run all unit tests:

```bash
mvn test
```

To run tests with code coverage report:

```bash
mvn clean test jacoco:report
```

The coverage report will be available at `target/site/jacoco/index.html`.

### Test Structure

The project includes 55 tests organized into three test classes:

| Test Class | Tests | Description |
|------------|-------|-------------|
| `AndroidXmlOutputterTest` | 31 | Unit tests for the core XML formatting logic |
| `MainTest` | 13 | Tests for CLI argument parsing and file processing |
| `IntegrationTest` | 11 | End-to-end tests comparing formatted output against expected files |

**Integration Tests**

Integration tests use input/expected file pairs located in `src/test/resources/integration/`:

```
src/test/resources/integration/
├── input/                    # Input XML files
│   ├── default_options.xml
│   ├── custom_indention.xml
│   ├── custom_attribute_indention.xml
│   ├── custom_attribute_order.xml
│   ├── attribute_sort.xml
│   ├── custom_namespace_order.xml
│   ├── namespace_sort.xml
│   └── combined_options.xml
└── expected/                 # Expected output files
    └── (corresponding files)
```

Each integration test formats an input file with specific options and compares the result against the expected output file.

## Code Formatting

This project uses [Spotless](https://github.com/diffplug/spotless) with Google Java Format for code formatting.

To check if code is properly formatted:

```bash
mvn spotless:check
```

To automatically format all Java files:

```bash
mvn spotless:apply
```

## Usage

To view available command line options:

```bash
java -jar target/android-xml-formatter-1.1-SNAPSHOT-jar-with-dependencies.jar --help
```

### Command Line Options

| Option | Description |
|--------|-------------|
| `--indention <n>` | Set indentation level (default: 4) |
| `--attribute-indention <n>` | Set attribute indentation level (default: 4) |
| `--attribute-order <list>` | Comma-separated list of attribute names to prioritize (default: `id,layout_width,layout_height`) |
| `--attribute-sort` | Sort attributes alphabetically |
| `--namespace-order <list>` | Comma-separated list of namespaces to prioritize (default: `android`) |
| `--namespace-sort` | Sort namespaces alphabetically |

### Examples

Format a single file:

```bash
java -jar android-xml-formatter.jar layout.xml
```

Format multiple files:

```bash
java -jar android-xml-formatter.jar res/layout/*.xml
```

Format with custom indentation:

```bash
java -jar android-xml-formatter.jar --indention 2 layout.xml
```

Sort attributes alphabetically:

```bash
java -jar android-xml-formatter.jar --attribute-sort --attribute-order "" layout.xml
```

## CI Integration

Can be used as a style check on a CI server by executing the formatter and then checking for differences:

```bash
# Format all XML files
java -jar android-xml-formatter.jar res/layout/*.xml

# Check if any files were modified
git diff --exit-code
```

## Dependencies

- [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) - Command line parsing
- [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/) - String utilities
- [JDOM2](http://www.jdom.org/) - XML processing

## License

See LICENSE file for details.
