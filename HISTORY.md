##2.0.1 - 2014-09-22
* Fixed a Windows path bug with absolute file names [#5](https://github.com/rholder/gradle-view/issues/5)

##2.0.0 - 2014-09-10
* Major rewrite of the Gradle Tooling API integration using 1.12 and custom model serialization
* Added handling of multi-module project dependency graphs for all configurations
* Added separate scrolling log window

##1.0.1 - 2013-07-21
* Adjusted color for highlighting on the Darcula theme
* Added toggle to switch between display of actual/replaced versions for [#3](https://github.com/rholder/gradle-view/issues/3)
* Fixed handling of configurations with "No dependencies" for [#2](https://github.com/rholder/gradle-view/issues/2)
* Code cleanup and preparation for the dependencyInsight task once it's available in the Gradle Tooling API

##1.0.0 - 2012-01-03
* Initial stable release