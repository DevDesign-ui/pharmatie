@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script (Windows)
@REM ----------------------------------------------------------------------------

@echo off
set MAVEN_CMD_LINE_ARGS=%*

@REM Find the project base directory.
set MAVEN_PROJECTBASEDIR=%~dp0
if exist "%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config" goto readJvmConfig
goto endReadJvmConfig
:readJvmConfig
for /f %%a in ("%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config") do set JVM_CONFIG=%%a
:endReadJvmConfig

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPS="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

if exist %WRAPPER_JAR% goto runWrapper
if exist %WRAPPER_PROPS% (
    for /f "tokens=1,* delims==" %%a in (%WRAPPER_PROPS%) do (
        if "%%a"=="wrapperUrl" set WRAPPER_URL=%%b
    )
)
if "%WRAPPER_URL%"=="" (
    echo ERROR: Cannot locate maven-wrapper.jar and wrapperUrl is not set.
    exit /b 1
)
echo Downloading Maven Wrapper from %WRAPPER_URL% ...
powershell -Command "Invoke-WebRequest -Uri %WRAPPER_URL% -OutFile %WRAPPER_JAR%"
:runWrapper

"%JAVA_HOME%\bin\java.exe" -classpath %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%
