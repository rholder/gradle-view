## What is this?
The Gradle View IntelliJ IDEA plugin shows a split tree rollup of the dependencies for each Gradle configuration in use
by a project. Dependency wrangling isn't easy in a large Java project. This IntelliJ IDEA plugin was built to add a little more sanity
to the tedious task of analyzing dependencies by rolling up the entire dependency graph into a pair of more easily
digestible trees derived from a serialized version of Gradle's dependency graph. The `Dependency List` provides a
lexicographically sorted set of all the dependencies for each configuration in your project and all of its sub-projects.
The `Dependency Hierarchy` shows a nested view of each Gradle configuration in your project and all of its sub-projects.
Grey dependencies in the tree indicate it was included by a previous dependency that was added before (and can be safely
omitted if it is explicitly being included).

![Gradle View](http://plugins.jetbrains.com/files/7150/screenshot_14710.png)

## Features
 * Built on the [Gradle Tooling API 5.6.2](https://docs.gradle.org/5.6.2/userguide/embedding.html), but should work on other recent versions
 * Visual highlighting to indicate dependencies in use and replacement versions
 * Lexicographically sorted listing for all Gradle configurations
 * Load any project's Gradle dependencies, not just the one currently open inside IntelliJ
 * Toggle the showing of replaced dependencies

## Installation
The latest version of the Gradle View plugin is available on the
[JetBrains Plugin Repository](http://plugins.jetbrains.com/). The first time you interact with Gradle View, you may
need to download the embedded version of Gradle in case you don't already have a cached copy available on your
workstation. This should be seamless, and the tool window title and log will indicate a download of this is in progress.

The IntelliJ plugin zip and a standalone jar release are available [here](https://github.com/rholder/gradle-view/releases).
The standalone jar can be run without IntelliJ. You may also build and install the plugin from source (see below).

## Building from source
This repository has everything needed to set up the development environment to build this plugin from source.

The Gradle View plugin itself uses a [Gradle](http://gradle.org)-based build system. In the instructions
below, [`./gradlew`](http://vimeo.com/34436402) is invoked from the root of the source tree and serves as
a cross-platform, self-contained bootstrap mechanism for the build. The prerequisites are
[Git](https://help.github.com/articles/set-up-git) and JDK 1.8+ for using the Gradle Wrapper bootstrap.

More information about the IntelliJ IDEA plugin development process can be found
[here](https://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/gradle_guide.html#overview-of-the-intellij-idea-gradle-plugin),
but the following are some bare bones testing and development instructions:

#### Check out sources
Check out this repository locally with:
```bash
git clone git://github.com/rholder/gradle-view.git
```

#### Adjust Gradle build variables
Review the contents of the `gradle.properties` file in the root directory and update them if desired.

#### Compile and build plugin distribution zip
Run the following to create an installable plugin zip in `build/distributions/gradle-view-*.zip`:
```bash
./gradlew clean distPlugin
```
[Install the plugin from disk](https://www.jetbrains.com/help/idea/managing-plugins.html) with `Settings/Preferences` -> `Plugins` -> `(Gear Icon)` -> `Install Plugin from Disk`.

#### Compile and build a standalone jar
A standalone Swing version that doesn't depend on IntelliJ can be built with:
```bash
./gradlew clean distStandalone
```
This jar can then be run with:
```bash
java -jar build/distributions/gradle-view-standalone.jar
```
NOTE: This is not the same as the IntelliJ platform plugin zip. It cannot be installed in the IDE.

## License
The Gradle View plugin is released under version 2.0 of the
[Apache License](http://www.apache.org/licenses/LICENSE-2.0). See LICENSE file for more details.
