@echo off
REM Docker 개발환경 안정성 검증 스크립트 (Windows용)
REM 전체 Docker 환경의 상태를 확인하고 문제점을 진단

setlocal enabledelayedexpansion

echo ==================================================
echo 🏥 Docker 개발환경 안정성 검증
echo ==================================================
echo.

REM 전체 검증 결과 추적
set overall_status=0

REM 1. Docker 서비스 상태 확인
echo ℹ️  1. Docker 서비스 상태를 확인합니다...
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker 서비스가 실행되지 않고 있습니다.
    set overall_status=1
) else (
    echo ✅ Docker 서비스가 정상 실행 중입니다.
)
echo.

REM 2. 컨테이너 상태 확인
echo ℹ️  2. 컨테이너 상태를 확인합니다...
docker-compose ps >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Docker Compose 프로젝트가 실행되지 않고 있습니다.
) else (
    echo ✅ Docker Compose 프로젝트 상태:
    docker-compose ps --format "table {{.Name}}\t{{.State}}\t{{.Status}}"
)
echo.

REM 3. 헬스체크 상태 확인
echo ℹ️  3. 서비스 헬스체크를 확인합니다...

REM 백엔드 헬스체크
echo    - 백엔드 API 헬스체크...
curl -f -s http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ❌ 백엔드 API가 응답하지 않습니다.
    set overall_status=1
) else (
    echo ✅ 백엔드 API가 정상 응답합니다.
)

REM 프론트엔드 헬스체크
echo    - 프론트엔드 웹서버 헬스체크...
curl -f -s http://localhost:3000/ >nul 2>&1
if errorlevel 1 (
    echo ❌ 프론트엔드 웹서버가 응답하지 않습니다.
    set overall_status=1
) else (
    echo ✅ 프론트엔드 웹서버가 정상 응답합니다.
)

REM Redis 헬스체크
echo    - Redis 연결 확인...
docker exec solebid-redis redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo ❌ Redis 서버에 연결할 수 없습니다.
    set overall_status=1
) else (
    echo ✅ Redis 서버가 정상 작동합니다.
)
echo.

REM 4. 네트워크 연결 확인
echo ℹ️  4. 네트워크 연결을 확인합니다...

REM 백엔드-Redis 연결 확인
docker exec solebid-backend ping -c 1 redis >nul 2>&1
if errorlevel 1 (
    echo ❌ 백엔드에서 Redis로 연결할 수 없습니다.
    set overall_status=1
) else (
    echo ✅ 백엔드-Redis 네트워크 연결이 정상입니다.
)

REM 프론트엔드-백엔드 연결 확인 (API 프록시)
curl -f -s http://localhost:3000/api/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️  프론트엔드-백엔드 API 프록시 연결을 확인할 수 없습니다.
) else (
    echo ✅ 프론트엔드-백엔드 API 프록시가 정상 작동합니다.
)
echo.

REM 5. 리소스 사용량 확인
echo ℹ️  5. 리소스 사용량을 확인합니다...
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
echo.

REM 6. 볼륨 및 데이터 확인
echo ℹ️  6. 볼륨 및 데이터 상태를 확인합니다...
docker volume ls | findstr solebid >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Solebid 관련 볼륨을 찾을 수 없습니다.
) else (
    echo ✅ Docker 볼륨이 정상적으로 생성되어 있습니다:
    docker volume ls | findstr solebid
)
echo.

REM 7. 로그 오류 확인
echo ℹ️  7. 최근 로그에서 오류를 확인합니다...
docker-compose logs --tail=20 2>&1 | findstr -i "error\|exception\|failed" >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  최근 로그에서 오류가 발견되었습니다:
    docker-compose logs --tail=10 2>&1 | findstr -i "error\|exception\|failed"
    set overall_status=1
) else (
    echo ✅ 최근 로그에서 심각한 오류가 발견되지 않았습니다.
)
echo.

REM 8. 환경 변수 검증
echo ℹ️  8. 환경 변수를 검증합니다...
if exist "scripts\validate-env.bat" (
    call scripts\validate-env.bat >nul 2>&1
    if errorlevel 1 (
        echo ⚠️  환경 변수 설정에 문제가 있을 수 있습니다.
        echo    자세한 내용은 scripts\validate-env.bat를 실행하세요.
    ) else (
        echo ✅ 환경 변수가 올바르게 설정되어 있습니다.
    )
) else (
    echo ⚠️  환경 변수 검증 스크립트를 찾을 수 없습니다.
)
echo.

REM 최종 결과 출력
echo ==================================================
if %overall_status%==0 (
    echo ✅ Docker 개발환경이 안정적으로 작동하고 있습니다!
    echo ==================================================
    echo.
    echo 🎯 접속 정보:
    echo    - 프론트엔드: http://localhost:3000
    echo    - 백엔드 API: http://localhost:8080
    echo    - 백엔드 헬스체크: http://localhost:8080/actuator/health
    echo    - Redis: localhost:6379
) else (
    echo ⚠️  Docker 개발환경에 문제가 발견되었습니다!
    echo ==================================================
    echo.
    echo 🔧 권장 해결 방법:
    echo    1. 로그 확인: docker-compose logs -f
    echo    2. 컨테이너 재시작: docker-compose restart
    echo    3. 환경 정리: scripts\docker-cleanup.bat
    echo    4. 트러블슈팅 가이드: docs\docker-troubleshooting-guide.md
)

echo.
echo 📊 추가 모니터링:
echo    - 실시간 로그: docker-compose logs -f
echo    - 리소스 모니터링: docker stats
echo    - 컨테이너 상태: docker-compose ps
echo.

pause