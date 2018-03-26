# Contributing to Discord4J
Firstly, thanks for considering helping me out on this project! As stated in the readme, "No one is perfect at 
programming and I am no exception." So I welcome any Pull Requests and Bug Reports. That being said, I have some 
standards which I enforce in order to keep this project organized, maintainable and high-quality. 

## Issues vs Pull Requests
I value issues just as much as I do pull requests. So if you are trying to decide on whether you should create an issue 
or create a pull request, ask yourself if it is worth making a pull request for. For example, making a pull request for 
fixing a single typo may not be better than just submitting an issue. That being said, a pull request fixing very many 
typos may be better for me than an issue. This is all subjective but you should take this into consideration.

## Issues
If there is a problem with a feature please tell me, just please make sure to follow the [template](ISSUE_TEMPLATE.md)
in order to allow me to more easily identify the issue and fix it. Questions and suggestions are also welcome to be
posted as an issue, however I'd rather talk about it in a better medium than Github issues so I would prefer if you talk
to me about it on the [Discord4J server](https://discord.gg/NxGAeCY) (don't worry, I won't bite).

## Pull Requests
So, you are thinking about sending a pull request, awesome! Before starting on your pull request, you should read up on
the [pull request template](PULL_REQUEST_TEMPLATE.md) Just a few things to note when submitting a pull 
request:

1. All your code will be subject to the project's licence, in this case [GNU LGPLv3](LICENSE.txt)
2. You cannot modify this project's style (i.e. indentation style, bracket style, naming, etc). I'll understand if you
do this by accident, but I'll expect you to fix it before I merge the pull request. (That being said, rewrites of 
certain systems are fine, I will just need to review it before deciding whether to merge it as with all other pull 
requests)
3. Merge to the right branch! Always merge to the `dev` branch. 

### Style Requirements
* Make sure your IDE is respecting the [.editorconfig](https://github.com/Discord4J/Discord4J/blob/master/.editorconfig)
* This project uses [1tbs](https://en.wikipedia.org/wiki/Indent_style#Variant:_1TBS), with the exception that single 
line if statements do not need brackets.
* All instanced objects should have getters and setters (where reasonable)
* All classes and methods should be documented with javadocs
* All new files should have the same LGPL 3 boilerplate header found in existing files.
