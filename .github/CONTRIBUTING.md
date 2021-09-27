# Contributing to Discord4J
Firstly, we appreciate your interest in contributing to Discord4J!

In order to maintain the high-quality standards of the project, we have a few rules to follow when contributing.


## Issues
The issue tracker is **not** meant for support. Ask in our [Discord server](https://discord.gg/d4j) or 
[Create a discussion](https://github.com/Discord4J/Discord4J/discussions) instead.

Make sure to select and follow the appropriate issue template.

## Pull Requests

### Development workflow

Discord4J development is done over multiple branches, starting our oldest supported branch first and then **merging**
changes forward. For this reason, it is likely that your PR should target that older maintenance branch instead of 
`master`.

As a general rule, if our stable releases are from `3.2.x` branch, changes should always target that. This allows
us to avoid backports and instead merge changes forward applying all needed changes to newer branches up to `master`.

On the other hand, if the change only affects `3.3.x` line, changes should target that branch, or `master` if it's the 
latest. For more information about our versions check our [docs](https://docs.discord4j.com/versions)

For more background around this workflow check this issue: [Link](https://github.com/reactor/reactor-core/issues/1225).

### Wanted Changes
* Bug Fixes
* Feature Implementations
* Improvements to documentation

### Discouraged Changes
* Cosmetic Changes
* Large changes to the project's structure

A few things to remember when opening a pull request:
* All code submitted to Discord4J will be subject to the project's license, [GNU LGPLv3](../LICENSE.txt).
* Abide by the style requirements described below.
* Use descriptive commit messages.
* Follow the template!

If you're unsure of whether a change is welcome, feel free to ask first in our [Discord server](https://discord.gg/d4j).

### Style Requirements
* Make sure your IDE is respecting the [.editorconfig](../.editorconfig)
* This project uses space indents.
* JavaDocs should be edited appropriately for your changes.
* All new files should have the same LGPL 3 boilerplate header found in existing files.

Finally, thanks again for contributing to Discord4J. When your changes are accepted, make sure to ask about the 
Contributor role in our [Discord server](https://discord.gg/d4j).
