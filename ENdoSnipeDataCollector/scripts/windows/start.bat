@echo off

set CURRENT=%~dp0
set JAVA="%JAVA_HOME%\bin\java"
set PROP=%CURRENT%..\conf\collector.properties
set LOG4J=%CURRENT%..\conf\log4j.properties

%JAVA% -Dcollector.property=%PROP% -Dlog4j.configuration=%LOG4J% -cp "%CURRENT%..\lib\*" jp.co.acroquest.endosnipe.collector.Bootstrap start