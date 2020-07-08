#!/bin/bash

cd project/project-sprint2
gradle -b build_ctxwaiter.gradle distZip
unzip build/distributions/* -d .
cp sysRules.pl project-sprint2-1.0/bin
cp tearoom.pl project-sprint2-1.0/bin
cp teaRoomExplored.bin project-sprint2-1.0/bin
cp tearoomKB-project-sprint2.pl project-sprint2-1.0/bin 

cd ../robot-web-sprint2
./gradlew build jar
mkdir webExecutable
cp build/libs/rob* webExecutable
cp clientConfig.json webExecutable
cp mqttConfig.json webExecutable
cp pageConfig.json webExecutable

cd ../../req-analysis/tearoomClientDemo/
gradle -b build_ctxclient.gradle distZip
unzip build/distributions/* -d .
cp sysRules.pl tearoomClientDemo-1.0/bin
cp tearoomclientsimulation.pl tearoomClientDemo-1.0/bin
cp -r tearoomClientDemo-1.0 ../../clientdemo

cd ../../
#unzip basicrobot.zip -d .
#unzip wEnv.zip -d .
