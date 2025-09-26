@echo off
REM Solebid 스마트 개발환경 시작 스크립트 (Windows용)
REM 포트 충돌을 자동으로 감지하고 해결하는 지능형 시작 스크립트

setlocal enabledelayedexpansion

echo ==================================================
echo 🚀 Solebid 스마트 개발환경 시작
echo ==================================================
echo.

REM .env 파일 존재 확인
if not exist ".env" (
    echo ⚠️  .env 파일이 없습니다. .env.example을 복사하여 생성합니다...
    copy .env.example .env >nul
    echo ✅ .env 파일이 생성되었습니다. 필요시 환경 변수를 수정하세요.
    echo.
)

REM Docker 서비스 확인
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker가 실행되지 않고 있습니다. Docker Desktop을 시작하세요.
    pause
    exit /b 1
)

REM 포트 충돌 자동 감지 및 해결
echo ℹ️  포트 충돌을 자동으로 감지합니다...

REM .env 파일에서 현재 포트 설정 읽기
if exist ".env" (
    for /f "tokens=1,2 delims==" %%a in ('type .env ^| findstr "DOCKER_.*_PORT"') do (
        set %%a=%%b
    )
)

REM 기본 포트 설정
if not defined DOCKER_FRONTEND_PORT set DOCKER_FRONTEND_PORT=3000
if not defined DOCKER_BACKEND_PORT set DOCKER_BACKEND_PORT=8080
if not defined DOCKER_REDIS_PORT set DOCKER_REDIS_PORT=6379
if not defined DOCKER_DEBUG_PORT set DOCKER_DEBUG_PORT=5005
if not defined DOCKER_LIVERELOAD_PORT set DOCKER_LIVERELOAD_PORT=35729

set use_alternative=0
set compose_files=-f docker-compose.yml -f docker-compose.dev.yml

REM 각 포트 확인 및 충돌 기록
netstat -ano | findstr :%DOCKER_FRONTEND_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  포트 %DOCKER_FRONTEND_PORT% (프론트엔드)가 사용 중입니다.
    set use_alternative=1
)

netstat -ano | findstr :%DOCKER_BACKEND_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  포트 %DOCKER_BACKEND_PORT% (백엔드)가 사용 중입니다.
    set use_alternative=1
)

netstat -ano | findstr :%DOCKER_REDIS_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  포트 %DOCKER_REDIS_PORT% (Redis)가 사용 중입니다.
    set use_alternative=1
)

netstat -ano | findstr :%DOCKER_DEBUG_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  포트 %DOCKER_DEBUG_PORT% (디버깅)가 사용 중입니다.
    set use_alternative=1
)

netstat -ano | findstr :%DOCKER_LIVERELOAD_PORT% >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  포트 %DOCKER_LIVERELOAD_PORT% (LiveReload)가 사용 중입니다.
    set use_alternative=1
)

REM 포트 충돌 시 오류 메시지 출력
if !use_alternative!==1 (
    echo ❌ 포트 충돌이 감지되었습니다. 사용 중인 포트들을 정리하세요.
    echo.
    echo 🔧 포트 정리 방법:
    echo    1. 포트 사용 프로세스 확인: scripts\check-ports.bat
    echo    2. Docker 컨테이너 정리: scripts\docker-cleanup.bat
    echo    3. 수동으로 프로세스 종료 후 다시 시도
    pause
    exit /b 1
) else (
    echo ✅ 모든 기본 포트가 사용 가능합니다.
)
echo.

REM 기존 컨테이너 정리 옵션
set /p cleanup="🧹 기존 컨테이너를 정리하시겠습니까? (권장) (Y/n): "
if /i not "!cleanup!"=="n" (
    echo 🧹 기존 컨테이너를 정리합니다...
    docker-compose down --remove-orphans >nul 2>&1
    echo ✅ 정리 완료
)
echo.

REM 개발환경 시작
echo 🔧 개발환경을 시작합니다...
echo 📍 접속 정보:
echo    - 프론트엔드: http://localhost:3000
echo    - 백엔드: http://localhost:8080
echo    - Redis: localhost:6379
echo    - 백엔드 디버깅: http://localhost:5005
echo.

REM Docker Compose 실행
echo ℹ️  Docker Compose를 시작합니다...
docker-compose !compose_files! up --build -d

REM 시작 결과 확인
if errorlevel 1 (
    echo ❌ 개발환경 시작에 실패했습니다.
    echo.
    echo 🔧 문제 해결 방법:
    echo    1. 로그 확인: docker-compose logs -f
    echo    2. 포트 정리: scripts\docker-cleanup.bat
    echo    3. 헬스체크: scripts\docker-health-check.bat
    pause
    exit /b 1
)

REM 서비스 시작 대기
echo ℹ️  서비스가 시작될 때까지 대기합니다...
timeout /t 10 /nobreak >nul

REM 헬스체크 수행
echo ℹ️  서비스 상태를 확인합니다...
call scripts\docker-health-check.bat

echo.
echo ==================================================
echo ✅ 스마트 개발환경 시작이 완료되었습니다!
echo ==================================================
echo.
echo 💡 유용한 명령어:
echo    - 로그 확인: docker-compose logs -f
echo    - 상태 확인: docker-compose ps
echo    - 환경 중지: docker-compose down
echo    - 헬스체크: scripts\docker-health-check.bat
echo.

pause