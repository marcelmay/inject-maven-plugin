Maven Injection Plugin
==================================

The plugin *inject-maven-plugin* lets you inject Maven properties into your compiled code as part of your build process.

[![Maven Central](https://img.shields.io/maven-central/v/de.m3y.maven/inject-maven-plugin.svg)](http://search.maven.org/#search%7Cga%7C1%7Cde.m3y.maven.inject-maven-plugin)

What is it good for?
--------------------

You can use the plugin to inject any properties, such as build information like build time or SCM version (provided as Maven properties by [buildnumber-maven-plugin]).
The plugin hooks into the Maven lifecycle process-classes - so after compilation but before packaging into e.g. JAR.

Note:
* It is possible to inject into Java constants, but be aware of that the Java compiler inlines values during compilation.
* It is possible to inject into Java class attribute fields, but be aware the Java compiler inlines initialization into constructor,
  resulting in multiple initializations with last initialization wining.
* Preferred injection is into accessor methods, resulting in the least amount of surprise by compiler/JVM initialization specifics :-)
* The plugin uses [ByteBuddy](https://github.com/raphw/byte-buddy) byte code injection library
* Check injection, e.g. using the Java Class File Disassembler (javap) on the target class:  
  ```javap -p -constants -c -s -cp ./target/it/inject/target/classes de.m3y.maven.inject.it.ExampleInjection```

Check out the [plugin website][site] including [usage][site_usage] for details.

[site]: http://marcelmay.github.io/inject-maven-plugin/
[site_usage]: https://marcelmay.github.io/inject-maven-plugin/usage.html
[repo-snapshot]: https://oss.sonatype.org/content/repositories/snapshots/de/m3y/maven/inject-maven-plugin/
[buildnumber-maven-plugin]: https://github.com/mojohaus/buildnumber-maven-plugin

Requirements
-----------
* Maven 3.9 or higher
* Java 8 or higher

Development
-----------

* Build the plugin

  `mvn clean install`

  Make sure you got [Maven 3.9+][maven_download] or higher.

* Build the site

  `mvn clean site site:stage scm-publish:publish-scm -Psite`

* Release

  ```
  mvn release:prepare -Prelease
  mvn release:perform -Prelease
  ```

* Deploy snapshot

  `mvn clean deploy -Prelease`

  Note: The release profile contains the snapshot repository for distribution management

[maven_download]: http://maven.apache.org
