@echo off
echo ===== Local Maven Build Script =====
echo Building JARs locally to avoid Portainer Maven issues...

echo.
echo [1/3] Building Backend Service...
cd backend-service
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Backend service build failed
    pause
    exit /b 1
)
echo Backend JAR created: target\*.jar

echo.
echo [2/3] Building TCP Server Service...
cd ..\tcp-server-service
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: TCP server build failed
    pause
    exit /b 1
)
echo TCP Server JAR created: target\*.jar

echo.
echo [3/3] Building CMA-CGM Integration...
cd ..\cma-cgm
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: CMA-CGM integration build failed
    pause
    exit /b 1
)
echo CMA-CGM JAR created: target\*.jar

cd ..
echo.
echo ===== All JARs built successfully! =====
echo You can now deploy to Portainer using the pre-built JARs.
echo.
pause
