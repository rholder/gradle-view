##What is this?
The Gradle View IntelliJ IDEA plugin shows a split tree rollup of the dependencies for each Gradle configuration in use
by a project.

##What is this?
Dependency wrangling isn't easy in a large Java project. This IntelliJ IDEA plugin was built to add a little more sanity
to the tedious task of analyzing dependencies by rolling up the entire dependency graph into a pair of more easily
digestible trees derived from a serialized version of Gradle's dependency graph. The `Dependency List` provides a
lexicographically sorted set of all the dependencies for each configuration in your project and all of its sub-projects.
The `Dependency Hierarchy` shows a nested view of each Gradle configuration in your project and all of its sub-projects.
Grey dependencies in the tree indicate it was included by a previous dependency that was added before (and can be safely
omitted if it is explicitly being included).

![Gradle View](http://plugins.jetbrains.com/files/7150/screenshot_14710.png)

##Features
 * Built on the [Gradle Tooling API 1.12](http://www.gradle.org/docs/1.12/userguide/embedding.html), but should work on most 1.x and 2.x versions
 * Visual highlighting to indicate dependencies in use and replacement versions
 * Lexicographically sorted listing for all Gradle configurations
 * Load any project's Gradle dependencies, not just the one currently open inside IntelliJ
 * Toggle the showing of replaced dependencies

##Installation
The latest version of the Gradle View plugin is available on the
[JetBrains Plugin Repository](http://plugins.jetbrains.com/). The first time you interact with Gradle View, you may
need to download the embedded version of Gradle 1.12 in case you don't already have a cached copy available on your
workstation. This should be seamless, and the tool window title and log will indicate a download of this is in progress.
You may also build and install the plugin from source (see below).

##Building from source
The Gradle View plugin uses a [Gradle](http://gradle.org)-based build system. In the instructions
below, [`./gradlew`](http://vimeo.com/34436402) is invoked from the root of the source tree and serves as
a cross-platform, self-contained bootstrap mechanism for the build. The prerequisites are
[Git](https://help.github.com/articles/set-up-git) and JDK 1.6+ for using the Gradle Wrapper bootstrap.
You'll also need the IntelliJ IDEA SDK installed in your IntelliJ environment. See
[Configuring IntelliJ IDEA SDK](http://confluence.jetbrains.net/display/IDEADEV/Getting+Started+with+Plugin+Development)
for details.

### check out sources
`git clone git://github.com/rholder/gradle-view.git`

### add Gradle build variables
Create a custom `gradle.properties` file in the root directory and add a tooling version to use, as in:

    gradleToolingApiVersion=1.12
    ideaPluginJdkName=IDEA IU-135.690

The `ideaPluginJdkName` should be the version you have referenced in your IntelliJ IDE.

### compile, test and build plugin distribution zip
`./gradlew distPlugin`

##License
The Gradle View plugin is released under version 2.0 of the
[Apache License](http://www.apache.org/licenses/LICENSE-2.0). See LICENSE file for more details.
