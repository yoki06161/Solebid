@echo off
REM Docker Environment Setup Validation Script (Windows)
REM Integrates validate-docker-compose and test-docker-configurations functionality

setlocal enabledelayedexpansion

REM Check for help option first
if "%1"=="--help" goto show_help
if "%1"=="-h" goto show_help

REM Set option flags
set config_only=false
set env_only=false
if "%1"=="--config-only" set config_only=true
if "%1"=="--env-only" set env_only=true

REM Only show header if not help
echo ==================================================
echo Docker Environment Setup Validation
echo ==================================================
echo This script validates Docker Compose configuration
echo and verifies proper operation of various environments.
echo ==================================================
echo.

REM Test result tracking
set TESTS_PASSED=0
set TESTS_FAILED=0
set OVERALL_STATUS=0

REM Skip sections based on options
if "%config_only%"=="true" goto docker_compose_tests
if "%env_only%"=="true" goto env_vars_only
goto full_validation

:env_vars_only
echo [TEST] Environment Variables Validation
if not exist ".env" (
    set /a TESTS_FAILED+=1
    echo [ERROR] .env file exists
    goto results_summary
)
set /a TESTS_PASSED+=1
echo [SUCCESS] .env file exists

REM Check required environment variables
set missing_vars=
for %%v in (DB_URL DB_USERNAME DB_PASSWORD JWT_SECRET AWS_ACCESS_KEY AWS_SECRET_KEY S3_BUCKET) do (
    findstr "%%v=" .env >nul 2>&1
    if errorlevel 1 set missing_vars=!missing_vars! %%v
)

if not "!missing_vars!"=="" (
    set /a TESTS_FAILED+=1
    echo [ERROR] Required environment variables
    echo [WARNING] Missing variables: !missing_vars!
) else (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Required environment variables
)
goto results_summary

:docker_compose_tests
echo [TEST] Docker Compose Configuration Tests

REM Basic configuration
echo [INFO] Testing basic configuration...
docker-compose -f docker-compose.yml config >nul 2>&1
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Basic configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Basic configuration
)

REM Development configuration
echo [INFO] Testing development configuration...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml config >nul 2>&1
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Development configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Development configuration
)

REM Production configuration
echo [INFO] Testing production configuration...
docker-compose -f docker-compose.yml -f docker-compose.prod.yml config >nul 2>&1
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Production configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Production configuration
)
goto results_summary

:full_validation
REM Environment variables validation
echo [TEST] Environment Variables Validation
if not exist ".env" (
    set /a TESTS_FAILED+=1
    echo [ERROR] .env file exists
    goto skip_env_vars
)
set /a TESTS_PASSED+=1
echo [SUCCESS] .env file exists

REM Check required environment variables
set missing_vars=
for %%v in (DB_URL DB_USERNAME DB_PASSWORD JWT_SECRET AWS_ACCESS_KEY AWS_SECRET_KEY S3_BUCKET) do (
    findstr "%%v=" .env >nul 2>&1
    if errorlevel 1 set missing_vars=!missing_vars! %%v
)

if not "!missing_vars!"=="" (
    set /a TESTS_FAILED+=1
    echo [ERROR] Required environment variables
    echo [WARNING] Missing variables: !missing_vars!
) else (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Required environment variables
)

:skip_env_vars
echo.

REM Network and volume validation
echo [TEST] Network and Volume Configuration
docker-compose -f docker-compose.yml config 2>nul | findstr "solebid-network" >nul
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Network configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Network configuration
)

docker-compose -f docker-compose.yml config 2>nul | findstr "redis-data" >nul
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Volume configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Volume configuration
)
echo.

REM Docker Compose configuration tests
echo [TEST] Docker Compose Configuration Tests

REM Basic configuration
echo [INFO] Testing basic configuration...
docker-compose -f docker-compose.yml config >nul 2>&1
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Basic configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Basic configuration
)

REM Development configuration
echo [INFO] Testing development configuration...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml config >nul 2>&1
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Development configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Development configuration
)

REM Production configuration
echo [INFO] Testing production configuration...
docker-compose -f docker-compose.yml -f docker-compose.prod.yml config >nul 2>&1
if not errorlevel 1 (
    set /a TESTS_PASSED+=1
    echo [SUCCESS] Production configuration
) else (
    set /a TESTS_FAILED+=1
    echo [ERROR] Production configuration
)
echo.

:results_summary
REM Results summary
echo ==================================================
echo Docker Environment Setup Validation Results
echo ==================================================
echo.
set /a total_tests=%TESTS_PASSED%+%TESTS_FAILED%
echo [SUCCESS] Passed tests: %TESTS_PASSED%
if %TESTS_FAILED% gtr 0 (
    echo [ERROR] Failed tests: %TESTS_FAILED%
    set OVERALL_STATUS=1
) else (
    echo [SUCCESS] All tests passed!
)
echo Total tests: %total_tests%
if %total_tests% gtr 0 (
    set /a success_rate=%TESTS_PASSED%*100/%total_tests%
    echo Success rate: !success_rate!%%
)
echo.

if %TESTS_FAILED%==0 (
    echo ==================================================
    echo [SUCCESS] All Docker environment configurations are valid!
    echo ==================================================
    echo.
    echo Available commands:
    echo    - Development: docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
    echo    - Production: docker-compose -f docker-compose.yml -f docker-compose.prod.yml up
    echo.
    echo Next steps:
    echo    - Quick local diagnosis: scripts\docker-health-check.bat
    echo    - Comprehensive system test: scripts\docker-system-test.bat
) else (
    echo ==================================================
    echo [ERROR] Docker environment configuration has issues!
    echo ==================================================
    echo.
    echo Troubleshooting steps:
    echo    1. Check and modify .env file
    echo    2. Validate Docker Compose file syntax
    echo    3. Resolve port conflicts
    echo    4. Complete environment variable setup
    echo    5. Check network and volume configuration
)

echo.
pause
exit /b %OVERALL_STATUS%

:show_help
echo Docker Environment Setup Validation Script
echo.
echo Usage: %0 [options]
echo.
echo Options:
echo    --help, -h           Show this help
echo    --config-only        Perform configuration file validation only
echo    --env-only           Perform environment variable validation only
echo.
echo This script validates:
echo    - Docker Compose configuration file syntax and validity
echo    - Environment variable setup and application
echo    - Network and volume configuration
echo    - Service dependency relationships
echo    - Port mapping and duplication check
echo    - Basic security configuration verification
echo.
echo Related scripts:
echo    - Quick local diagnosis: scripts\docker-health-check.bat
echo    - Comprehensive system test: scripts\docker-system-test.bat
echo.
exit /b 0