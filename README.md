# RuntimeProfiler

![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/7orivorian/RuntimeProfiler/total?style=for-the-badge&link=https%3A%2F%2Fgithub.com%2F7orivorian%2FRuntimeProfiler%2Freleases%2Flatest)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/7orivorian/RuntimeProfiler?style=for-the-badge)
![Maven Central Version](https://img.shields.io/maven-central/v/dev.7ori/runtimeprofiler?style=for-the-badge&link=https%3A%2F%2Fcentral.sonatype.com&link=https%3A%2F%2Fcentral.sonatype.com%2Fartifact%2Fdev.7ori%2Fruntimeprofiler)

Performance monitoring for Java applications through execution time tracking and data export.

# Features

- **Easy integration & usage.**
    - Seamlessly add profiling capabilities to your Java projects with only a
      few lines of code.
- **Context switching:**
    - Compare performance between different code sections.
- **Detailed data export:**
    - Output profiling data for in-depth analysis, reporting, &
      graphing.

# Importing

### Maven

```xml
<dependency>
    <groupId>dev.7ori</groupId>
    <artifactId>runtimeprofiler</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```gradle
dependencies {
    implementation 'dev.7ori:runtimeprofiler:2.0.0'
}
```

### Other

Download a `.jar` file from
[releases](https://github.com/7orivorian/RuntimeProfiler/releases/tag/2.0.0).

# Building

* Clone this repository
* Run `mvn package`

Packaged jar file can be found in the `./target/` directory.

# Usage Example

```java
public class Main {

    public static void main(String[] args) {
        Profiler profiler = new Profiler("MyProfiler");
        profiler.start();

        profiler.push("Section_A");

        profiler.push("Sub-Section_1"); // Included in the runtime of Section_A
        // Code to be profiled
        profiler.swap("Sub-Section_2"); // Included in the runtime of Section_A
        // Code to be profiled
        profiler.pop();

        profiler.swap("Section_B");
        // Code to be profiled
        profiler.pop();

        profiler.stop();

        OutputWriter.HTML.writeToPath(profiler, new File("<output_dir>").toPath());
    }
}
```

# License

[This project is licensed under MIT.](./LICENSE)

### MIT License Summary:

The MIT License is a permissive open-source license that allows you to use,
modify, and distribute the software for both personal and commercial purposes.
You are not required to share your changes, but you must include the original
copyright notice and disclaimer in your distribution. The software is provided
"as is," without any warranties or conditions.
