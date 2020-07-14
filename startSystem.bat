cd projectExe/bin/
call "cmd /c start project-sprint3.bat"

cd ../../webExecutable
call "cmd /c start startWeb.bat"

Echo "Open localhost:7001 then press Enter"
pause

cd ../clientdemo/bin
call "cmd /c start startClientDemo.bat"

exit
