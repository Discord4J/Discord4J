Discord4J v3 uses a BOM or Bill of Materials as a way to coordinate dependencies that work well together, therefore providing faster and safer ways to migrate across versions.

# Usage

To check available versions make sure you go to the [BOM](https://github.com/Discord4J/BOM) repo and then replace `3.0.x` below with the adequate version.

## Maven

Maven supports BOM through the `dependencyManagement` entry. You should first add this snippet to your **pom.xml** file:

```xml
<dependencyManagement> 
    <dependencies>
        <dependency>
            <groupId>com.discord4j</groupId>
            <artifactId>bom</artifactId>
            <version>3.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then you are able to add dependencies just like normal ones, but without specifying the `<version>` attribute.

```xml
<dependencies>
    <dependency>
        <groupId>com.discord4j</groupId>
        <artifactId>discord4j-core</artifactId>         
    </dependency>
    <dependency>
        <groupId>com.discord4j</groupId>
        <artifactId>stores-caffeine</artifactId>
    </dependency>
</dependencies>
```

## Gradle

Add the following to your **build.gradle** file to import the BOM, making sure replacing the version with the one you wish to use:

```groovy
dependencies {
    implementation platform("com.discord4j:bom:3.x.x")
}
```

Finally, add the dependency as normal, without needing to specify the version:

```groovy
dependencies {
    compile "com.discord4j:discord4j-core"
    compile "com.discord4j:stores-caffeine"
}
```

# Development builds

You can also use SNAPSHOT and development builds from Jitpack with this BOM. You just have to also make sure you include the proper repository to pull the right dependencies:

## Maven

```xml
<repositories>
    <repository>
        <id>snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Gradle

```groovy
repositories {
    // This repository is for SNAPSHOTs
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    // This repository is for pulling dependencies from Jitpack
    maven { url 'https://jitpack.io' }
    // This repository is for releases
    mavenCentral()
}
```