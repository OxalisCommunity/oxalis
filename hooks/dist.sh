#!/bin/sh

rm -rf dist

version=$(ls oxalis-api/target/ | grep jar | head -1 | sed "s:oxalis\-api\-::" | sed "s:\.jar::")

mkdir -p dist/jars

cp **/target/*.jar dist/jars/
cp */**/target/*.jar dist/jars/

mv dist/jars/oxalis-standalone.jar dist/oxalis-standalone-$version.jar
cp oxalis-dist/**/target/*.war dist/
cp oxalis-dist/**/target/*.zip dist/
cp oxalis-dist/**/target/*.tar.gz dist/

for file in $(ls dist | grep "\-distro"); do
    mv dist/$file dist/$(echo $file | sed "s:\-distro::")
done

for file in $(ls dist | grep "\-full"); do
    mv dist/$file dist/$(echo $file | sed "s:\-full::")
done

zip -j -9 dist/oxalis-jars-$version.zip dist/jars/*.jar
tar -zcvf dist/oxalis-jars-$version.tar.gz -C dist/jars $(ls dist/jars)

rm -rf dist/jars