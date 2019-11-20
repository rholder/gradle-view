## 4.0.0 - 2019-11-20
* Upgrade internal tooling API to use Gradle 5.6.2 which adds Java 11 support
* Gradle wrapper is now at Gradle 5.6.2
* Java bytecode compatibility bumped up to 1.8 to support later versions of Gradle Tooling API
* Switch to using the org.jetbrains.intellij plugin for more modern plugin development
* Removed old gradle-one-jar plugin in favor of shadow

## 3.0.0 - 2017-11-21
* Upgrade internal tooling API to use Gradle 3.5.1
* Add dependency resolution compatibility fixes to support Gradle 2.x, 3.x, and 4.x
* Java bytecode compatibility bumped up to 1.7 to support later versions of Gradle Tooling API

## 2.1.0 - 2015-04-27
* Upgrade internal tooling API to use Gradle 2.3 #11
* Removed painful light red on blue background selection color #6

## 2.0.2 - 2014-10-06
* Fixed some wonky Windows JAVA_HOME path behavior #5

## 2.0.1 - 2014-09-22
* Fixed a Windows path bug with absolute file names [#5](https://github.com/rholder/gradle-view/issues/5)

## 2.0.0 - 2014-09-10
* Major rewrite of the Gradle Tooling API integration using 1.12 and custom model serialization
* Added handling of multi-module project dependency graphs for all configurations
* Added separate scrolling log window

## 1.0.1 - 2013-07-21
* Adjusted color for highlighting on the Darcula theme
* Added toggle to switch between display of actual/replaced versions for [#3](https://github.com/rholder/gradle-view/issues/3)
* Fixed handling of configurations with "No dependencies" for [#2](https://github.com/rholder/gradle-view/issues/2)
* Code cleanup and preparation for the dependencyInsight task once it's available in the Gradle Tooling API

## 1.0.0 - 2012-01-03
* Initial stable release