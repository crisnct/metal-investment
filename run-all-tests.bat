@echo off
echo =============================================================================
echo COMPREHENSIVE API TESTING WITH JUNIT 5 AND MOCKITO
echo =============================================================================
echo.

echo This script will run all unit tests for the Metal Investment API.
echo Tests include controllers, services, and integration tests.
echo.

echo Available test options:
echo 1. Run All Tests
echo 2. Run Controller Tests Only
echo 3. Run Service Tests Only
echo 4. Run Integration Tests Only
echo 5. Run Specific Test Class
echo 6. Exit
echo.

set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" (
    echo.
    echo Running All Tests...
    mvn test
    goto end
)

if "%choice%"=="2" (
    echo.
    echo Running Controller Tests...
    mvn test -Dtest="*ControllerTest"
    goto end
)

if "%choice%"=="3" (
    echo.
    echo Running Service Tests...
    mvn test -Dtest="*ServiceTest"
    goto end
)

if "%choice%"=="4" (
    echo.
    echo Running Integration Tests...
    mvn test -Dtest="*IntegrationTest"
    goto end
)

if "%choice%"=="5" (
    echo.
    echo Available test classes:
    echo - PublicApiControllerTest
    echo - ProtectedApiControllerTest
    echo - PurchaseServiceTest
    echo - AlertServiceTest
    echo - ApiIntegrationTest
    echo - EmailServiceTest
    echo - SimpleEmailTest
    echo.
    set /p testclass="Enter test class name: "
    echo.
    echo Running %testclass%...
    mvn test -Dtest=%testclass%
    goto end
)

if "%choice%"=="6" (
    echo.
    echo Exiting...
    goto end
)

echo Invalid choice. Please run the script again.
goto end

:end
echo.
echo Test execution completed.
pause
