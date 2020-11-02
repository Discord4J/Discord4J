# Support and compatibility

The following table describes our current available versions, their support status, Discord API versions and compatibility with other projects like Spring Boot.

| Discord4J                                                   | Support          | Gateway/API | Intents                           | Reactor           | Spring Boot   |
|-------------------------------------------------------------|------------------|-------------|-----------------------------------|-------------------|---------------|
| [v3.2.x](https://github.com/Discord4J/Discord4J/tree/master)| In development   | v8          | Mandatory, [non-privileged as default](https://discord.com/developers/docs/topics/gateway#privileged-intents) | 3.4 (2020)        | 2.3 and above |
| [v3.1.x](https://github.com/Discord4J/Discord4J/tree/3.1.x) | Current          | v6          | Optional, no intent default       | 3.3 (Dysprosium)  | 2.2 and above |
| [v3.0.x](https://github.com/Discord4J/Discord4J/tree/3.0.x) | Maintenance only | v6          | No intents support                | 3.2 (Californium) | 2.1           |

All supported versions have JDK 8 baseline. Spring Boot compatibility is shown a general guideline as other versions might work through shading techniques for transitive libraries like Reactor and Netty.

"Maintenance only" support means we'll try to address critical issues only but no new features will be added to that branch.

# Versioning

Discord4J uses a `Generation.Major.Minor` version scheme so the following guidelines apply:

- Upgrades from a `x.y.z` to `x.y.z+1` version don't affect public API and behavior changes are not expected unless they mean to fix a major issue.
- Upgrades from a `x.y` to `x.y+1` generally need a migration guide as they can have public API changes. We will try to offer a migration period of at least 1 major version whenever possible.

Classes and methods marked as Beta or Experimental are not affected by this policy.
