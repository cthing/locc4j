# ![C Thing Software](https://www.cthing.com/branding/CThingSoftware-57x60.png "C Thing Software") locc4j

A Java library for counting lines of source code. Supports over 250 languages using a high performance
counting algorithm.

## Features
* Fast line counting (see [Counting Performance](Performance.md))
* Over 250 languages supported
* Counts code lines, comment lines and blank lines
* Highly accurate counting that handles multi-line comments, nested comments and embedded languages
* Ability to associate custom file extensions with languages and remove unwanted associations
* File system traversal using glob pattern matching and honoring Git ignore files
* Pure Java implementation

## Supported Languages
The complete list of supported languages can be found in the [languages.json](conf/languages.json) file.
The file includes a description of each language, a link to detailed information about the language,
the file extensions associated with the language, and additional information to describe the language
syntax for counting purposes.

To programmatically obtain the list of supported languages and the file extensions to which they
are associated, call `Language.getExtensions`.

To request support for a language, create an issue and provide the following information:
* Description of the language
* Link to the official website for the language. If there is no official website for the language,
  provide a link to a Wikipedia or similar page describing the details of the language.
* Links to example files for the language

## Usage
The library is available from [Maven Central](https://repo.maven.apache.org/maven2/org/cthing/locc4j/) using
the following Maven dependency:
```xml
<dependency>
  <groupId>org.cthing</groupId>
  <artifactId>locc4j</artifactId>
  <version>1.0.0</version>
</dependency>
```
or the following Gradle dependency:
```kotlin
implementation("org.cthing:locc4j:1.0.0")
```

### Counter Results

All counting APIs return results using a map of language to line counts (i.e. `Map<Language, Stats>`).
A map is used to accommodate languages that can embed other languages. The library detects these embedded
languages and counts their lines using the counter corresponding to that language. For example, while an
HTML file contains markup, it may also contain lines of JavaScript and CSS. This means that the counter
results for an HTML file might contain counts for three languages (HTML, JavaScript and CSS).

The [Stats](src/main/java/org/cthing/locc4j/Stats.java) class provides the actual line counts. The counts
are:
* **Code lines** - Number of lines of source code, markup tags, or textual content
* **Comment lines** - Number of lines consisting solely of comments. If a code line has a trailing comment,
    that line is considered a code line not a comment line.
* **Blank lines** - Number of lines consisting entirely of whitespace

### Counting a String or Character Array
The following code counts lines within a string or character array.
```java
final Counter counter = new Counter(Language.Markdown);
final Map<Language, Stats> stats = counter.count("# Title\n\nHello World");
```

### Counting an Input Stream
The following code counts lines from a character input stream.
```java
final InputStream ins = getClass().getResourceAsStream("/data/program.py");
final Counter counter = new Counter(Language.Python);
final Map<Language, Stats> stats = counter.count(ins);
```

### Counting a File
The following code counts lines from a file. The file's primary language is determined by first examining its
name, then extension, and finally any shebang (i.e. `#!`) that may be present at the start of the file.
```java
final FileCounter counter = new FileCounter();
final Map<Language, Stats> stats = counter.count("/tmp/program.cpp");
```

### Counting a File System Tree
The following code counts all files under the specified directory tree. By default, hidden files and directories
are excluded.
```java
final CountingTreeWalker walker = new CountingTreeWalker(Path.of("/home/myusername/foo"));
final Map<Path, Map<Language, Stats>> counts = walker.count();
```

The following code excludes files based on any encountered Git ignore files.
```java
final CountingTreeWalker walker = new CountingTreeWalker(Path.of("/home/myusername/foo")).respectGitignore(true);
final Map<Path, Map<Language, Stats>> counts = walker.count();
```

The following code counts only C++ source files using a glob pattern match.
```java
final CountingTreeWalker walker = new CountingTreeWalker(Path.of("/home/myusername/foo"), "*.cpp");
final Map<Path, Map<Language, Stats>> counts = walker.count();
```

The following code does the same thing using a language match.
```java
final CountingTreeWalker walker = new CountingTreeWalker(Path.of("/home/myusername/foo"), Language.Java);
final Map<Path, Map<Language, Stats>> counts = walker.count();
```

See the Javadoc for the `CountingTreeWalker` for details on the glob syntax and other options.

The `StatsUtils` class provides methods to calculate various metrics based on the results of a
file tree walk. For example, the following code calculates the line counts for all languages
encountered on a tree walk.
```java
final CountingTreeWalker walker = new CountingTreeWalker(Path.of("/home/myusername/foo"));
final Map<Path, Map<Language, Stats>> fileStats = walker.count();
final Map<Language, Stats> languageStats = StatsUtils.byLanguage(fileStats);
```

### Finding a Language
The library's file-based APIs automatically determine the primary language of a file. The `Language`
enum provides methods to manually determine a language.

| Method              | Description                                                                                                       |
|---------------------|-------------------------------------------------------------------------------------------------------------------|
| `fromFile`          | Determines a file's language by first looking up the file name, then the file extension, and finally any shebang. |
| `fromMime`          | Provides the language associated with a MIME type                                                                 |
| `fromFileExtension` | Provides the language associated with the specified file extension                                                |
| `fromId`            | Provides the language associated with the specified `Language` enum value                                         |
| `fromName`          | Provides the language associated with the specified language name                                                 |
| `fromShebang`       | Provides the language associated with the specified file's interpreter or environment shebang                     |

### Custom File Extension Associations
The library has a built-in association of common file extensions to languages. These associations can be
augmented, changed or removed. Call `Language.addExtension` to add a new association or change an existing
one. Call `Language.removeExtension` to remove an association. To restore the default associations, call
`Language.resetExtensions`.

## Accuracy
The library does not perform complete parsing of each language. As described in the
[Counting Performance](Performance.md) document, this would severely impact performance and would be
impractical to implement. The counting algorithm used by the library makes a balanced tradeoff between
performance and accuracy. While the algorithm can accommodate nested comments and embedded languages,
inevitably there are language constructs that will be incorrectly counted. Please report these inaccuracies
by creating an issue and providing the following information:
* The name of the language
* A test file demonstrating the problem
* The line counts reported by the library and the expected line counts

## Acknowledgements
The counting algorithm and the initial set of language data used by this library are based on the
[tokei project](https://github.com/XAMPPRocky/tokei) using the
[MIT License](https://github.com/XAMPPRocky/tokei/blob/master/LICENCE-MIT), which is reproduced below.

> MIT License (MIT)
> 
> Copyright (c) 2016 Erin Power
> 
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in
> all copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
> THE SOFTWARE.

## Building
The library is compiled for Java 17. If a Java 17 toolchain is not available, one will be downloaded.

Gradle is used to build the library:
```bash
./gradlew build
```
The Javadoc for the library can be generated by running:
```bash
./gradlew javadoc
```

A `buildSrc` Gradle plugin is used to generate the `Language` enum class from the [languages.json](conf/languages.json)
data file and the [Language.ftl](buildSrc/src/main/resources/org/cthing/locc4j/Language.java.ftl) FreeMarker template.

## Releasing
This project is released on the [Maven Central repository](https://central.sonatype.com/artifact/org.cthing/filevisitor).
Perform the following steps to create a release.

- Commit all changes for the release
- In the `build.gradle.kts` file
    - Ensure that `baseVersion` is set to the version for the release. The project follows [semantic versioning](https://semver.org/).
    - Set `isSnapshot` to `false`
- Commit the changes
- Wait until CI builds the release candidate
- Run the command `mkrelease locc4j <version>`
- In a browser go to the [Maven Central Repository Manager](https://s01.oss.sonatype.org/)
- Log in
- Use the `Staging Upload` to upload the generated artifact bundle `locc4j-bundle-<version>.jar`
- Click on `Staging Repositories`
- Once it is enabled, press `Release` to release the artifacts to Maven Central
- Log out
- Wait for the new release to be available on Maven Central
- In a browser, go to the project on GitHub
- Generate a release with the tag `<version>`
- In the build.gradle.kts file
    - Increment the `baseVersion` patch number
    - Set `isSnapshot` to `true`
- Update the `CHANGELOG.md` with the changes in the release and prepare for next release changes
- Update the `Usage` section in the `README.md` with the latest artifact release version
- Commit these changes
