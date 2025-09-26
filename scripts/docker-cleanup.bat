@echo off
REM Docker 컨테이너 정리 및 재시작 자동화 스크립트 (Windows용)
REM 포트 충돌 및 Docker 환경 문제 해결을 위한 종합 정리 스크립트

setlocal enabledelayedexpansion

echo ==================================================
echo 🧹 Docker 환경 정리 및 재시작 스크립트
echo ==================================================
echo.

REM 현재 실행 중인 Docker 컨테이너 확인
echo ℹ️  현재 실행 중인 Docker 컨테이너를 확인합니다...
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

REM 사용자 선택 메뉴
echo 정리 옵션을 선택하세요:
echo 1. 기본 정리 (Solebid 컨테이너만)
echo 2. 전체 정리 (모든 Docker 리소스)
echo 3. 포트 충돌 해결
echo 4. 네트워크 및 볼륨 정리
echo 5. 전체 시스템 정리 (주의: 모든 Docker 데이터 삭제)
echo 0. 취소
echo.

set /p choice="선택하세요 (0-5): "

if "%choice%"=="0" (
    echo 작업이 취소되었습니다.
    pause
    exit /b 0
)

if "%choice%"=="1" goto basic_cleanup
if "%choice%"=="2" goto full_cleanup
if "%choice%"=="3" goto port_conflict
if "%choice%"=="4" goto network_volume_cleanup
if "%choice%"=="5" goto system_cleanup

echo 잘못된 선택입니다.
pause
exit /b 1

:basic_cleanup
echo.
echo 🧹 기본 정리를 시작합니다...
echo.

REM Solebid 컨테이너 중지 및 제거
echo ℹ️  Solebid 컨테이너를 중지하고 제거합니다...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down --remove-orphans

REM 사용하지 않는 이미지 정리
echo ℹ️  사용하지 않는 Docker 이미지를 정리합니다...
docker image prune -f

echo ✅ 기본 정리가 완료되었습니다.
goto end

:full_cleanup
echo.
echo 🧹 전체 정리를 시작합니다...
echo.

REM 모든 컨테이너 중지
echo ℹ️  모든 Docker 컨테이너를 중지합니다...
for /f "tokens=1" %%i in ('docker ps -q') do docker stop %%i

REM 모든 컨테이너 제거
echo ℹ️  모든 Docker 컨테이너를 제거합니다...
docker container prune -f

REM 사용하지 않는 이미지 정리
echo ℹ️  사용하지 않는 Docker 이미지를 정리합니다...
docker image prune -a -f

REM 사용하지 않는 네트워크 정리
echo ℹ️  사용하지 않는 Docker 네트워크를 정리합니다...
docker network prune -f

echo ✅ 전체 정리가 완료되었습니다.
goto end

:port_conflict
echo.
echo 🔍 포트 충돌 문제를 해결합니다...
echo.

REM 포트 3000 사용 프로세스 확인
echo ℹ️  포트 3000을 사용하는 프로세스를 확인합니다...
netstat -ano | findstr :3000
if errorlevel 1 (
    echo ✅ 포트 3000은 현재 사용되지 않고 있습니다.
) else (
    echo ⚠️  포트 3000을 사용하는 프로세스가 발견되었습니다.
    set /p kill_process="해당 프로세스를 종료하시겠습니까? (y/N): "
    if /i "!kill_process!"=="y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3000') do (
            echo 프로세스 ID %%a를 종료합니다...
            taskkill /PID %%a /F
        )
    )
)

REM 포트 8080 사용 프로세스 확인
echo ℹ️  포트 8080을 사용하는 프로세스를 확인합니다...
netstat -ano | findstr :8080
if errorlevel 1 (
    echo ✅ 포트 8080은 현재 사용되지 않고 있습니다.
) else (
    echo ⚠️  포트 8080을 사용하는 프로세스가 발견되었습니다.
    set /p kill_process="해당 프로세스를 종료하시겠습니까? (y/N): "
    if /i "!kill_process!"=="y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
            echo 프로세스 ID %%a를 종료합니다...
            taskkill /PID %%a /F
        )
    )
)

REM 포트 6379 사용 프로세스 확인
echo ℹ️  포트 6379를 사용하는 프로세스를 확인합니다...
netstat -ano | findstr :6379
if errorlevel 1 (
    echo ✅ 포트 6379는 현재 사용되지 않고 있습니다.
) else (
    echo ⚠️  포트 6379를 사용하는 프로세스가 발견되었습니다.
    set /p kill_process="해당 프로세스를 종료하시겠습니까? (y/N): "
    if /i "!kill_process!"=="y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :6379') do (
            echo 프로세스 ID %%a를 종료합니다...
            taskkill /PID %%a /F
        )
    )
)

echo ✅ 포트 충돌 해결이 완료되었습니다.
goto end

:network_volume_cleanup
echo.
echo 🧹 네트워크 및 볼륨 정리를 시작합니다...
echo.

REM Docker 네트워크 정리
echo ℹ️  사용하지 않는 Docker 네트워크를 정리합니다...
docker network prune -f

REM Docker 볼륨 정리 (주의: 데이터 손실 가능)
echo ⚠️  사용하지 않는 Docker 볼륨을 정리합니다. (데이터가 삭제될 수 있습니다)
set /p confirm_volume="계속하시겠습니까? (y/N): "
if /i "!confirm_volume!"=="y" (
    docker volume prune -f
    echo ✅ 볼륨 정리가 완료되었습니다.
) else (
    echo ℹ️  볼륨 정리를 건너뜁니다.
)

echo ✅ 네트워크 및 볼륨 정리가 완료되었습니다.
goto end

:system_cleanup
echo.
echo ⚠️  전체 시스템 정리는 모든 Docker 데이터를 삭제합니다!
echo    - 모든 컨테이너
echo    - 모든 이미지
echo    - 모든 네트워크
echo    - 모든 볼륨
echo    - 모든 빌드 캐시
echo.
set /p confirm_system="정말로 계속하시겠습니까? (yes/no): "
if /i "!confirm_system!"=="yes" (
    echo 🧹 전체 시스템 정리를 시작합니다...
    docker system prune -a --volumes -f
    echo ✅ 전체 시스템 정리가 완료되었습니다.
) else (
    echo ℹ️  전체 시스템 정리를 취소했습니다.
)
goto end

:end
echo.
echo ==================================================
echo ✅ Docker 정리 작업이 완료되었습니다!
echo ==================================================
echo.
echo 💡 다음 단계:
echo    1. 개발환경 시작: scripts\dev-start.bat
echo    2. 상태 확인: docker ps
echo    3. 로그 확인: docker-compose logs -f
echo.

pause