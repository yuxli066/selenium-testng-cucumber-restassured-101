@echo off
:: Usage: run-tests.bat [profile] [suite]
:: Example: run-tests.bat default testng.xml

set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=default

set SUITE=%2
if "%SUITE%"=="" set SUITE=testng.xml

echo Running Maven tests with profile: %PROFILE% and suite: %SUITE%
mvn clean test -P %PROFILE% -DsuiteXmlFile=%SUITE%
