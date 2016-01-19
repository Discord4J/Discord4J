#!/usr/bin/env bash
export JAVA_HOME="`/usr/libexec/java_home -v '1.8*'`" #Using 1.8 Java
echo "Enter the version number to use"
read
DIR=${REPLY//./-} #Replaces the periods with dashes
mvn javadoc:javadoc #Generate javadoc
git stash #Stash any non committed things
git checkout gh-pages #Switch to gh-pages branch
rm ./latest.txt
echo "$DIR" >> ./latest.txt
echo "$REPLY;" >> ./versions.txt
mkdir ./docs
mkdir "./docs/$DIR"
mv -v ./target/site/apidocs/* "./docs/$DIR" #Move javadocs to repo
git add . #Add all files to commit
git commit -a -m "Add javadoc $REPLY" #Commit
git push #Push changes
git checkout - #Checkout last branch
git stash apply #Revert to stashed changes
