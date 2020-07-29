## Installation
[Jitpack](https://jitpack.io/) is an easy to use package repository that builds directly from GitHub repository contents, allowing faster development cycles. We only recommend using Jitpack if you want to keep up with the latest incubating features for Discord4J or wish to test a particular fix.

If you're looking to install versions from Jitpack, be aware of **groupId** changes:

| Stable | Jitpack |
| ------------- | ------------- |
| com.discord4j | com.discord4j.discord4j |

### Maven
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.discord4j.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>master-SNAPSHOT</version>
  </dependency>
</dependencies>
```
### Gradle
```groovy
repositories {
  maven { url 'https://jitpack.io' }
}

dependencies {
  implementation 'com.discord4j.discord4j:discord4j-core:master-SNAPSHOT'
}
```
### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j.discord4j" % "discord4j-core" % "master-SNAPSHOT"
)

resolvers += "jitpack.io" at "https://jitpack.io"
```

## Using a Specific Version
Instead of `master-SNAPSHOT` you can also use a direct commit hash to pin your dependency, even under development builds. The commit hash can be obtained from the [list of GitHub commits](https://github.com/Discord4J/Discord4J/commits/v3):

![Commit Hash](https://i.imgur.com/wd7XxOd.png)