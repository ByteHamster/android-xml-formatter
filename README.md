# android-xml-formatter

Formats XML files according to Android Studio's default formatting rules. By default, also re-orders the attributes to specify the `android:id`, `android:layout_width` and `android:layout_height` first. This can be turned off with a command line option.

## Requirements

- Java 8 or higher
- Maven 3.6 or higher

## Building

### Build the project

```bash
mvn clean package
```

This creates an executable JAR file with all dependencies at:
```
target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run tests and verification

```bash
mvn verify
```

This runs all tests and code format checks.

## Usage

To view available command line options:

```bash
java -jar target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar --help
```

### Command Line Options

| Option | Description |
|--------|-------------|
| `--indention <n>` | Set indentation spaces (default: 4) |
| `--attribute-indention <n>` | Set attribute indentation spaces (default: 4) |
| `--attribute-order <list>` | Comma-separated attribute order (default: `id,layout_width,layout_height`) |
| `--attribute-sort` | Sort attributes alphabetically |
| `--namespace-order <list>` | Comma-separated namespace order (default: `android`) |
| `--namespace-sort` | Sort namespaces alphabetically |

### Example

Format an XML file:

```bash
java -jar target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar path/to/layout.xml
```

Format multiple files:

```bash
java -jar target/android-xml-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar file1.xml file2.xml file3.xml
```

## Code Formatting

This project uses [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven) with Eclipse JDT formatter for Java code formatting, configured to match the [AOSP (Android Open Source Project) code style](https://source.android.com/docs/setup/contribute/code-style).

### Configuration

- **Formatter**: Eclipse JDT
- **Style**: AOSP (Android Open Source Project) - see `settings/eclipse-aosp-style.xml`
- **Import Order**: AOSP style (android, androidx, com.android, dalvik, libcore, com, org, java, javax)
- **Check Phase**: Runs automatically during `mvn verify`

### Check code formatting

To verify that all Java code follows the formatting rules:

```bash
mvn spotless:check
```

### Apply code formatting

To automatically format all Java code:

```bash
mvn spotless:apply
```

### Formatting Rules

The AOSP style enforces:
- 4-space indentation
- 100 character line length limit
- Opening braces on same line
- AOSP import ordering with blank lines between groups
- Proper spacing around operators and keywords
- Preserved line breaks in method chains (won't join manually wrapped lines)

> **Note:** Always run `mvn spotless:apply` before committing to ensure consistent formatting.

## CI Integration

Can be used as a style check on a CI server by executing the formatter and then printing the diff:

```bash
# Check XML formatting
java -jar android-xml-formatter.jar *.xml
git diff --exit-code

# Check Java code formatting
mvn spotless:check
```

## License

See [LICENSE](LICENSE) file for details.
