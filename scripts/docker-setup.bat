@echo off
REM Solebid Docker 초기 설정 자동화 스크립트 (Windows용)
REM 프로젝트를 처음 설정하는 개발자를 위한 원클릭 설정 스크립트

setlocal enabledelayedexpansion

REM 헤더 출력
echo ==================================================
echo 🚀 Solebid Docker 환경 초기 설정
echo ==================================================
echo.

REM 시스템 요구사항 확인
echo ℹ️  시스템 요구사항을 확인합니다...

REM Docker 설치 확인
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker가 설치되지 않았습니다.
    echo Docker Desktop 설치 가이드: https://docs.docker.com/desktop/windows/
    pause
    exit /b 1
)

REM Docker Compose 설치 확인
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose가 설치되지 않았습니다.
    echo Docker Compose 설치 가이드: https://docs.docker.com/compose/install/
    pause
    exit /b 1
)

REM Docker 서비스 실행 확인
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker 서비스가 실행되지 않고 있습니다.
    echo Docker Desktop을 시작하세요.
    pause
    exit /b 1
)

echo ✅ 시스템 요구사항 확인 완료
for /f "tokens=*" %%i in ('docker --version') do echo    - Docker: %%i
for /f "tokens=*" %%i in ('docker-compose --version') do echo    - Docker Compose: %%i
echo.

REM 환경 변수 파일 설정
echo ℹ️  환경 변수 파일을 설정합니다...

REM 개발환경 .env 파일 생성
if not exist ".env" (
    if exist ".env.example" (
        copy .env.example .env >nul
        echo ✅ .env 파일이 생성되었습니다.
    ) else (
        echo ❌ .env.example 파일을 찾을 수 없습니다.
        pause
        exit /b 1
    )
) else (
    echo ⚠️  .env 파일이 이미 존재합니다. 건너뜁니다.
)

REM 프로덕션 환경 .env.prod 파일 생성 (선택사항)
if not exist ".env.prod" (
    if exist ".env.prod.example" (
        set /p create_prod="프로덕션용 .env.prod 파일을 생성하시겠습니까? (y/N): "
        if /i "!create_prod!"=="y" (
            copy .env.prod.example .env.prod >nul
            echo ✅ .env.prod 파일이 생성되었습니다.
        )
    )
)
echo.

REM 필수 디렉토리 생성
echo ℹ️  필수 디렉토리를 생성합니다...

if not exist "data" mkdir data
if not exist "data\redis" mkdir data\redis
if not exist "data\prometheus" mkdir data\prometheus
if not exist "logs" mkdir logs
if not exist "backups" mkdir backups

echo ✅ 디렉토리 생성 완료
echo    - data\redis: Redis 데이터 저장
echo    - data\prometheus: Prometheus 메트릭 저장
echo    - logs: 애플리케이션 로그
echo    - backups: 백업 파일
echo.

REM Docker 이미지 사전 다운로드
echo ℹ️  기본 Docker 이미지를 다운로드합니다...

docker pull redis:7-alpine
docker pull nginx:alpine
docker pull openjdk:17-jdk-slim
docker pull node:18-alpine

echo ✅ 기본 이미지 다운로드 완료
echo.

REM 환경 변수 검증
echo ℹ️  환경 변수를 검증합니다...

if exist "scripts\validate-env.bat" (
    call scripts\validate-env.bat
    if errorlevel 1 (
        echo ⚠️  환경 변수 검증에서 경고가 발생했습니다.
        echo 계속 진행하려면 .env 파일을 확인하고 필요한 값을 설정하세요.
    ) else (
        echo ✅ 환경 변수 검증 완료
    )
) else (
    echo ⚠️  환경 변수 검증 스크립트를 찾을 수 없습니다.
)
echo.

REM 개발환경 테스트 빌드
echo ℹ️  개발환경 테스트 빌드를 수행합니다...

set /p test_build="테스트 빌드를 수행하시겠습니까? (시간이 다소 걸릴 수 있습니다) (y/N): "
if /i "!test_build!"=="y" (
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml build
    if errorlevel 1 (
        echo ❌ 테스트 빌드 실패
        echo 빌드 로그를 확인하고 문제를 해결하세요.
        pause
        exit /b 1
    ) else (
        echo ✅ 테스트 빌드 완료
    )
) else (
    echo ℹ️  테스트 빌드를 건너뜁니다.
)
echo.

REM 완료 메시지 및 다음 단계 안내
echo ==================================================
echo ✅ Solebid Docker 환경 초기 설정이 완료되었습니다!
echo ==================================================
echo.
echo 🎯 다음 단계:
echo.
echo 1. 환경 변수 설정:
echo    - .env 파일을 편집하여 필요한 환경 변수를 설정하세요
echo    - 특히 데이터베이스, AWS, OAuth2 설정을 확인하세요
echo.
echo 2. 개발환경 시작:
echo    scripts\dev-start.bat
echo.
echo 3. 접속 URL:
echo    - 프론트엔드: http://localhost:3000
echo    - 백엔드 API: http://localhost:8080
echo    - 백엔드 헬스체크: http://localhost:8080/actuator/health
echo.
echo 📚 추가 문서:
echo    - Docker 가이드: README-Docker.md
echo    - 환경 변수 가이드: docs/environment-variables-guide.md
echo    - 시스템 테스트 가이드: docs/docker-system-testing-guide.md
echo.
echo ❓ 문제가 발생하면:
echo    - 로그 확인: docker-compose logs -f
echo    - 환경 변수 검증: scripts\validate-env.bat
echo    - 컨테이너 정리: docker-compose down --volumes
echo.

pause