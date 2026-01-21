# android-xml-formatter

[![GitHub check runs](https://img.shields.io/github/check-runs/ByteHamster/android-xml-formatter/main)](https://github.com/ByteHamster/android-xml-formatter/actions/workflows/checks.yml?query=branch%3Amain)
[![License: GPL v3](https://img.shields.io/github/license/ByteHamster/android-xml-formatter)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub Release](https://img.shields.io/github/v/release/ByteHamster/android-xml-formatter)](https://github.com/ByteHamster/android-xml-formatter/releases)

Formats XML files according to Android Studio's default formatting rules. By default, also re-orders the attributes to specify the `android:id`, `android:layout_width` and `android:layout_height` first. This can be turned off with a command line option.

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building

- `mvn clean package` to create an executable JAR file with all dependencies
  at `target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar`.
- `mvn verify` runs all tests and code format checks.

## Usage

To view available command line options:

```bash
java -jar target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar --help
```

| Option                      | Description                                                                |
|-----------------------------|----------------------------------------------------------------------------|
| `--indention <n>`           | Set indentation spaces (default: 4)                                        |
| `--attribute-indention <n>` | Set attribute indentation spaces (default: 4)                              |
| `--attribute-order <list>`  | Comma-separated attribute order (default: `id,layout_width,layout_height`) |
| `--attribute-sort`          | Sort attributes alphabetically                                             |
| `--namespace-order <list>`  | Comma-separated namespace order (default: `android`)                       |
| `--namespace-sort`          | Sort namespaces alphabetically                                             |

### Example

Format an XML file:

```bash
java -jar target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar path/to/layout.xml
```

Format multiple files:

```bash
java -jar target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar file1.xml file2.xml file3.xml
```

## Contributing

This project uses [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven)
with Eclipse JDT formatter for Java code formatting, configured to match the
[AOSP (Android Open Source Project) code style](https://source.android.com/docs/setup/contribute/code-style).

To verify that all Java code follows the formatting rules, run `mvn spotless:check`.
To automatically format all Java code, run `mvn spotless:apply`.

## CI Integration

This project can be used as a style check on a CI server by executing the formatter and then printing the diff:

```bash
java -jar android-xml-formatter.jar *.xml
git diff --exit-code
```

## License

See [LICENSE](LICENSE) file for details.
