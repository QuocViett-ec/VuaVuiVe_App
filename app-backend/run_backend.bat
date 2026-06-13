@echo off
title VuaVuiVe Backend Server
echo Dang khoi dong Backend Server (Cong 3000)...
echo.
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
call apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
pause
