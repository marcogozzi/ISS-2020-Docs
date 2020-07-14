cd project/project-sprint3
call gradle -b build_ctxwaiter.gradle distZip
powershell -command "Expand-Archive -Force './build/distributions/project-sprint3-1.0.zip' '.'"
copy sysRules.pl "project-sprint3-1.0/bin"
copy tearoom.pl "project-sprint3-1.0/bin"
copy teaRoomExplored.bin "project-sprint3-1.0/bin"
copy tearoomKB-project-sprint3.pl "project-sprint3-1.0/bin"
copy startProject.bat "project-sprint3-1.0/bin"
ROBOCOPY /E project-sprint3-1.0 "../../projectExe"

cd ../robot-web-sprint3
call gradlew build jar
mkdir webExecutable
cd build/libs/
copy robotWeb2020-1.0.jar "../../webExecutable/"
cd ../..
copy clientConfig.json "webExecutable/"
copy mqttConfig.json "webExecutable/"
copy pageConfig.json "webExecutable/"
copy startWeb.bat "webExecutable/"
ROBOCOPY /E webExecutable "../../webExecutable"

cd ../../project/client-demo-web
call gradle -b build_ctxclient.gradle distZip
powershell -command "Expand-Archive -Force './build/distributions/client-demo-web-1.0.zip' '.'"
copy "./sysRules.pl" "client-demo-web-1.0/bin"
copy tearoomclientsimulationweb1.pl "client-demo-web-1.0/bin/"
copy tearoomclientsimulationweb2.pl "client-demo-web-1.0/bin/"
copy tearoomclientsimulationweb3.pl "client-demo-web-1.0/bin/"
copy startClientDemo1.bat "client-demo-web-1.0/bin/"
copy startClientDemo2.bat "client-demo-web-1.0/bin/"
copy startClientDemo3.bat "client-demo-web-1.0/bin/"
copy startClientDemo.bat "client-demo-web-1.0/bin/"
ROBOCOPY /E client-demo-web-1.0 "../../clientdemo"
