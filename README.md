Maven Injection Plugin
==================================

The plugin *inject-maven-plugin* lets you inject Maven properties into your compiled code.

What is it good for?
--------------------

You can use the plugin to inject any properties, such as build information like build time or SCM version (provided as Maven properties by [buildnumber-maven-plugin]).

Note:
* It is possible to inject into Java constants, but be aware the the Java compiler inlines values during compilation.
* It is possible to inject into Java class attribute fields, but be aware the the Java compiler inlines initialization into constructor,
  resulting in multiple initializations with last initialization wining.
* Preferred injection is into accessor methods, resulting in least amount of surprise by compiler/JVM initialization specifics :-)

Check out the [plugin web site][site] for details.

[site]: http://marcelmay.github.io/inject-maven-plugin/
[repo-snapshot]: https://oss.sonatype.org/content/repositories/snapshots/de/m3y/maven/inject-maven-plugin/
[buildnumber-maven-plugin]: https://github.com/mojohaus/buildnumber-maven-plugin

Development
-----------

* Build the plugin

    mvn clean install

  Make sure you got [Maven 3.0.3+][maven_download] or higher.

* Build the site

    mvn clean install integration-test site -Psite

* Release

    mvn release:prepare -Prelease

    mvn release:perform -Prelease

* Deploy snapshot

    mvn clean deploy -Prelease

  Note: The release profile contains the snapshot repository for distribution management

[maven_download]: http://maven.apache.org
