# ISS-2020-Docs
Repo that contains files to be submitted in ISS-2020 course

On Linux-based machines, run buildAll.sh to build latest Project, WebApp and a Client demo

# Prerequisites
Working Java installation
A running MQTT/Mosquitto broker on tcp://localhost:1883

Get virtual environment and basicrobot from github.com/anatali/iss2020LabBo
Build both and execute them

```sh
$ node wEnv/node/WEnv/server/src/main 8999
$ bash basicRobot/bin/basicrobot
```

To build:
 - Windows: from root folder double click on buildAll.bat
 
 To run:
  - Linux: from root folder run

```sh
$ cd project/project-sprint3/project-sprint3-1.0/bin/ && bash project-sprint3
$ cd project/robot-web-sprint3/webExecutable/ && java -jar robotWeb2020-1.0.jar
$ cd clientdemo/bin/ && bash tearoomClientDemo
```

 - Windows: from root folder double click on startSystem.bat
