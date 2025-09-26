@echo off
REM Docker 개발환경 포트 충돌 진단 스크립트 (Windows용)
REM 필요한 포트들의 사용 상태를 확인하고 충돌 해결 방안을 제시

setlocal enabledelayedexpansion

echo ==================================================
echo 🔍 Docker 개발환경 포트 충돌 진단
echo ==================================================
echo.

REM .env 파일에서 포트 설정 읽기
if exist ".env" (
    echo ℹ️  .env 파일에서 포트 설정을 읽습니다...
    for /f "tokens=1,2 delims==" %%a in ('type .env ^| findstr "DOCKER_.*_PORT"') do (
        set %%a=%%b
    )
) else (
    echo ⚠️  .env 파일이 없습니다. 기본 포트를 사용합니다.
)

REM 기본 포트 설정
if not defined DOCKER_FRONTEND_PORT set DOCKER_FRONTEND_PORT=3000
if not defined DOCKER_BACKEND_PORT set DOCKER_BACKEND_PORT=8080
if not defined DOCKER_REDIS_PORT set DOCKER_REDIS_PORT=6379
if not defined DOCKER_DEBUG_PORT set DOCKER_DEBUG_PORT=5005
if not defined DOCKER_LIVERELOAD_PORT set DOCKER_LIVERELOAD_PORT=35729

echo ✅ 포트 설정:
echo    - 프론트엔드: %DOCKER_FRONTEND_PORT%
echo    - 백엔드: %DOCKER_BACKEND_PORT%
echo    - Redis: %DOCKER_REDIS_PORT%
echo    - 디버깅: %DOCKER_DEBUG_PORT%
echo    - LiveReload: %DOCKER_LIVERELOAD_PORT%
echo.

REM 포트 충돌 검사 함수
set conflict_found=0

echo ℹ️  포트 사용 상태를 확인합니다...
echo.

REM 프론트엔드 포트 확인
netstat -ano | findstr :%DOCKER_FRONTEND_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ❌ 포트 %DOCKER_FRONTEND_PORT% (프론트엔드)가 사용 중입니다:
    netstat -ano | findstr :%DOCKER_FRONTEND_PORT%
    set conflict_found=1
) else (
    echo ✅ 포트 %DOCKER_FRONTEND_PORT% (프론트엔드)는 사용 가능합니다.
)

REM 백엔드 포트 확인
netstat -ano | findstr :%DOCKER_BACKEND_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ❌ 포트 %DOCKER_BACKEND_PORT% (백엔드)가 사용 중입니다:
    netstat -ano | findstr :%DOCKER_BACKEND_PORT%
    set conflict_found=1
) else (
    echo ✅ 포트 %DOCKER_BACKEND_PORT% (백엔드)는 사용 가능합니다.
)

REM Redis 포트 확인
netstat -ano | findstr :%DOCKER_REDIS_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ❌ 포트 %DOCKER_REDIS_PORT% (Redis)가 사용 중입니다:
    netstat -ano | findstr :%DOCKER_REDIS_PORT%
    set conflict_found=1
) else (
    echo ✅ 포트 %DOCKER_REDIS_PORT% (Redis)는 사용 가능합니다.
)

REM 디버깅 포트 확인
netstat -ano | findstr :%DOCKER_DEBUG_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ❌ 포트 %DOCKER_DEBUG_PORT% (디버깅)가 사용 중입니다:
    netstat -ano | findstr :%DOCKER_DEBUG_PORT%
    set conflict_found=1
) else (
    echo ✅ 포트 %DOCKER_DEBUG_PORT% (디버깅)는 사용 가능합니다.
)

REM LiveReload 포트 확인
netstat -ano | findstr :%DOCKER_LIVERELOAD_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ❌ 포트 %DOCKER_LIVERELOAD_PORT% (LiveReload)가 사용 중입니다:
    netstat -ano | findstr :%DOCKER_LIVERELOAD_PORT%
    set conflict_found=1
) else (
    echo ✅ 포트 %DOCKER_LIVERELOAD_PORT% (LiveReload)는 사용 가능합니다.
)

echo.



REM 결과 및 해결 방안 제시
if %conflict_found%==1 (
    echo ==================================================
    echo ⚠️  포트 충돌이 발견되었습니다!
    echo ==================================================
    echo.
    echo 🔧 해결 방안:
    echo.
    echo 1. 충돌하는 프로세스 종료:
    echo    scripts\docker-cleanup.bat (옵션 3 선택)
    echo.
    echo 2. Docker Compose 설정 검증:
    echo    scripts\validate-setup.bat
    echo.
    echo 3. 자동 해결:
    set /p auto_resolve="자동으로 충돌을 해결하시겠습니까? (y/N): "
    if /i "!auto_resolve!"=="y" (
        call scripts\docker-cleanup.bat
    )
) else (
    echo ==================================================
    echo ✅ 모든 포트가 사용 가능합니다!
    echo ==================================================
    echo.
    echo 🚀 Docker 개발환경을 시작할 수 있습니다:
    echo    scripts\dev-start.bat
    echo    scripts\dev-start-smart.bat
)

echo.
pause