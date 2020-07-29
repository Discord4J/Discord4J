Discord4J is under continuous development on its main `master` branch. Regular releases establish a point of stability within the project, but as new features are implemented you might want to try the latest changes or some important bug fixes that can help, even before a stable release. Therefore, we publish SNAPSHOT versions on each commit, giving you the option of choosing a stable version, or the latest one in a given branch.

To work with SNAPSHOTs, you only need to add the required repository and then set the version to use. The `VERSION` depends on the next version to be released, adding `-SNAPSHOT` as suffix. For example, for the latest development towards `3.1.1` version, you'll use `3.1.1-SNAPSHOT`.

### Using latest snapshots in Gradle
```groovy
repositories {
  maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:VERSION'
}
```

### Using latest snapshots in Maven
```xml
<repositories>
    <repository>
        <id>oss.sonatype.org-snapshot</id>
        <url>http://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.discord4j</groupId>
        <artifactId>discord4j-core</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```