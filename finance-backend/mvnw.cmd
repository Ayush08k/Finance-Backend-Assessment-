@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET DP0=%~dp0
@SET MAVEN_PROJECTBASEDIR=%DP0%
@SET MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
@SET MAVEN_WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"
@IF EXIST %MAVEN_WRAPPER_JAR% goto runWithJava
@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
@ECHO Downloading %DOWNLOAD_URL% to %MAVEN_WRAPPER_JAR%
@powershell -Command "&{"^
"$webclient = new-object System.Net.WebClient;"^
"$webclient.DownloadFile('%DOWNLOAD_URL%', '%MAVEN_WRAPPER_JAR%')"^
"}"
:runWithJava
@IF NOT "%JAVA_HOME%"=="" (SET JAVA_EXE=%JAVA_HOME%/bin/java.exe)
@IF "%JAVA_EXE%"=="" (SET JAVA_EXE=java)
@%JAVA_EXE% -classpath %MAVEN_WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %MAVEN_WRAPPER_PROPERTIES% %*
