# RuntimeProfiler

[7orivorian's](https://github.com/7orivorian) RuntimeProfiler enables
performance monitoring for Java applications through execution time tracking and
data export.

# Features

- **Easy integration & usage.**
    - Seamlessly add profiling capabilities to your Java projects with only a
      few lines of code.
- **Context switching:**
    - Compare performance between different code sections.
- **Detailed data export:**
    - Write profiling data to a CSV file for in-depth analysis, reporting, &
      graphing.

# Importing

### Maven

* Include JitPack in your maven build file

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

* Add RuntimeProfiler as a dependency

```xml

<dependency>
    <groupId>com.github.7orivorian</groupId>
    <artifactId>RuntimeProfiler</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

* Add JitPack to your root `build.gradle` at the end of repositories

```gradle
repositories {
    maven {
        url 'https://jitpack.io'
    }
}
```

* Add the dependency

```gradle
dependencies {
    implementation 'com.github.7orivorian:RuntimeProfiler:1.0.0'
}
```

### Other

Download a `.jar` file from
[releases](https://github.com/7orivorian/RuntimeProfiler/releases/tag/1.0.0).

# Building

* Clone this repository
* Run `mvn package`

Packaged jar file can be found in the `./target/` directory.

# Usage Example

```java
public class Main {

    public static void main(String[] args) throws IOException {
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

        DataWriter.writeToDir(profiler, new File("output_directory").toPath());
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