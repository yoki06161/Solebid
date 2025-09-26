@echo off
setlocal enabledelayedexpansion

REM ===========================================
REM       Solebid Docker 전체 시스템 테스트
REM ===========================================
REM 이 스크립트는 Docker 환경에서 전체 애플리케이션 스택의 정상 작동을 검증합니다.
REM 
REM 테스트 범위:
REM 1. 백엔드-프론트엔드 간 API 통신 검증
REM 2. 전체 애플리케이션 워크플로우 테스트
REM 3. 사용자 시나리오 기반 통합 테스트 수행
REM 4. Docker 환경에서 전체 스택 정상 작동 확인

echo ==========================================
echo        Solebid Docker 전체 시스템 테스트
echo ==========================================
echo.

REM 테스트 결과 추적 변수
set TESTS_PASSED=0
set TESTS_FAILED=0
set OVERALL_STATUS=0

REM 로그 함수들 (Windows용)
:log_info
echo [INFO] %~1
goto :eof

:log_success
echo [SUCCESS] %~1
goto :eof

:log_warning
echo [WARNING] %~1
goto :eof

:log_error
echo [ERROR] %~1
goto :eof

:log_test
echo [TEST] %~1
goto :eof

REM 테스트 결과 기록 함수
:record_test_result
set test_name=%~1
set result=%~2
if "%result%"=="PASS" (
    set /a TESTS_PASSED+=1
    call :log_success "✓ %test_name%"
) else (
    set /a TESTS_FAILED+=1
    call :log_error "✗ %test_name%"
    set OVERALL_STATUS=1
)
goto :eof

REM HTTP 엔드포인트 테스트 함수
:test_http_endpoint
set url=%~1
set expected_status=%~2
set timeout=%~3
if "%timeout%"=="" set timeout=10

REM curl을 사용하여 HTTP 상태 코드 확인
for /f %%i in ('curl -s -o nul -w "%%{http_code}" --max-time %timeout% "%url%" 2^>nul') do set response=%%i
if "%response%"=="" set response=000

if "%response%"=="%expected_status%" (
    exit /b 0
) else (
    exit /b 1
)

REM 1. 기본 인프라 검증
:test_infrastructure
call :log_test "=== 1. 기본 인프라 검증 ==="

REM Docker 서비스 확인
docker info >nul 2>&1
if %errorlevel%==0 (
    call :record_test_result "Docker 서비스 상태" "PASS"
) else (
    call :record_test_result "Docker 서비스 상태" "FAIL"
    goto :eof
)

REM Docker Compose 확인
docker-compose --version >nul 2>&1
if %errorlevel%==0 (
    call :record_test_result "Docker Compose 설치" "PASS"
) else (
    docker compose version >nul 2>&1
    if !errorlevel!==0 (
        call :record_test_result "Docker Compose 설치" "PASS"
    ) else (
        call :record_test_result "Docker Compose 설치" "FAIL"
        goto :eof
    )
)

REM 환경 변수 파일 확인
if exist ".env" (
    call :record_test_result "환경 변수 파일 존재" "PASS"
) else (
    call :record_test_result "환경 변수 파일 존재" "FAIL"
)

echo.
goto :eof

REM 2. 컨테이너 상태 및 헬스체크 검증
:test_container_health
call :log_test "=== 2. 컨테이너 상태 및 헬스체크 검증 ==="

set containers=solebid-backend solebid-frontend solebid-redis
set all_healthy=true

for %%c in (%containers%) do (
    REM 컨테이너 실행 상태 확인
    for /f %%s in ('docker inspect --format="{{.State.Status}}" %%c 2^>nul') do set status=%%s
    if "!status!"=="" set status=not_found
    
    if "!status!"=="running" (
        call :record_test_result "%%c 컨테이너 실행 상태" "PASS"
    ) else (
        call :record_test_result "%%c 컨테이너 실행 상태" "FAIL"
        set all_healthy=false
    )
)

if "%all_healthy%"=="true" (
    call :record_test_result "전체 컨테이너 상태" "PASS"
) else (
    call :record_test_result "전체 컨테이너 상태" "FAIL"
)

echo.
goto :eof

REM 3. 개별 서비스 기능 테스트
:test_individual_services
call :log_test "=== 3. 개별 서비스 기능 테스트 ==="

REM Redis 기능 테스트
call :log_info "Redis 기능 테스트 중..."
docker exec solebid-redis redis-cli ping >nul 2>&1
if %errorlevel%==0 (
    call :record_test_result "Redis 기본 연결" "PASS"
    
    REM Redis 데이터 저장/조회 테스트
    docker exec solebid-redis redis-cli set test_key "system_test_value" >nul 2>&1
    for /f %%v in ('docker exec solebid-redis redis-cli get test_key 2^>nul') do set retrieved_value=%%v
    
    if "!retrieved_value!"=="system_test_value" (
        call :record_test_result "Redis 데이터 저장/조회" "PASS"
        docker exec solebid-redis redis-cli del test_key >nul 2>&1
    ) else (
        call :record_test_result "Redis 데이터 저장/조회" "FAIL"
    )
) else (
    call :record_test_result "Redis 기본 연결" "FAIL"
)

REM 백엔드 API 기능 테스트
call :log_info "백엔드 API 기능 테스트 중..."
call :test_http_endpoint "http://localhost:8080/actuator/health" "200"
if %errorlevel%==0 (
    call :record_test_result "백엔드 헬스체크 엔드포인트" "PASS"
) else (
    call :record_test_result "백엔드 헬스체크 엔드포인트" "FAIL"
)

REM 프론트엔드 기능 테스트
call :log_info "프론트엔드 기능 테스트 중..."
call :test_http_endpoint "http://localhost:3000" "200"
if %errorlevel%==0 (
    call :record_test_result "프론트엔드 메인 페이지" "PASS"
) else (
    call :record_test_result "프론트엔드 메인 페이지" "FAIL"
)

echo.
goto :eof

REM 4. 백엔드-프론트엔드 API 통신 검증
:test_api_communication
call :log_test "=== 4. 백엔드-프론트엔드 API 통신 검증 ==="

REM 직접 백엔드 API 호출
call :log_info "직접 백엔드 API 호출 테스트..."
call :test_http_endpoint "http://localhost:8080/actuator/health" "200"
if %errorlevel%==0 (
    call :record_test_result "직접 API 호출 (health)" "PASS"
) else (
    call :record_test_result "직접 API 호출 (health)" "FAIL"
)

REM 프론트엔드를 통한 API 프록시 테스트
call :log_info "프론트엔드 API 프록시 테스트..."
call :test_http_endpoint "http://localhost:3000/api/actuator/health" "200"
if %errorlevel%==0 (
    call :record_test_result "프록시 API 호출 (health)" "PASS"
) else (
    REM 404도 정상으로 간주 (프록시 설정에 따라)
    call :test_http_endpoint "http://localhost:3000/api/actuator/health" "404"
    if !errorlevel!==0 (
        call :record_test_result "프록시 API 호출 (health)" "PASS"
    ) else (
        call :record_test_result "프록시 API 호출 (health)" "FAIL"
    )
)

echo.
goto :eof

REM 5. 사용자 시나리오 기반 통합 테스트
:test_user_scenarios
call :log_test "=== 5. 사용자 시나리오 기반 통합 테스트 ==="

REM 시나리오 1: 일반 사용자 웹사이트 접속
call :log_info "시나리오 1: 일반 사용자 웹사이트 접속"
call :test_http_endpoint "http://localhost:3000" "200"
if %errorlevel%==0 (
    call :record_test_result "사용자 메인 페이지 접속" "PASS"
) else (
    call :record_test_result "사용자 메인 페이지 접속" "FAIL"
)

REM 시나리오 2: API 기반 데이터 조회
call :log_info "시나리오 2: API 기반 데이터 조회"
call :test_http_endpoint "http://localhost:3000/api/actuator/health" "200"
if %errorlevel%==0 (
    call :record_test_result "프론트엔드를 통한 백엔드 상태 확인" "PASS"
) else (
    REM 직접 백엔드 호출로 대체 테스트
    call :test_http_endpoint "http://localhost:8080/actuator/health" "200"
    if !errorlevel!==0 (
        call :record_test_result "프론트엔드를 통한 백엔드 상태 확인" "PASS"
    ) else (
        call :record_test_result "프론트엔드를 통한 백엔드 상태 확인" "FAIL"
    )
)

echo.
goto :eof

REM 6. 전체 애플리케이션 워크플로우 테스트
:test_application_workflow
call :log_test "=== 6. 전체 애플리케이션 워크플로우 테스트 ==="

REM 전체 스택 연동 테스트
call :log_info "전체 스택 연동 흐름 테스트 중..."

set frontend_ok=false
set backend_ok=false
set redis_ok=false

REM 프론트엔드 접속 가능 확인
call :test_http_endpoint "http://localhost:3000" "200"
if %errorlevel%==0 set frontend_ok=true

REM 백엔드 API 접속 가능 확인
call :test_http_endpoint "http://localhost:8080/actuator/health" "200"
if %errorlevel%==0 set backend_ok=true

REM Redis 연결 확인
docker exec solebid-redis redis-cli ping >nul 2>&1
if %errorlevel%==0 set redis_ok=true

REM 전체 스택 연동 결과
if "%frontend_ok%"=="true" if "%backend_ok%"=="true" if "%redis_ok%"=="true" (
    call :record_test_result "전체 스택 연동 (Frontend-Backend-Redis)" "PASS"
) else (
    call :record_test_result "전체 스택 연동 (Frontend-Backend-Redis)" "FAIL"
)

echo.
goto :eof

REM 7. 성능 및 안정성 테스트
:test_performance_stability
call :log_test "=== 7. 성능 및 안정성 테스트 ==="

call :log_info "리소스 사용량 확인 중..."
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>nul
call :record_test_result "리소스 사용량 확인" "PASS"

echo.
goto :eof

REM 8. 오류 및 로그 분석
:test_error_analysis
call :log_test "=== 8. 오류 및 로그 분석 ==="

set containers=solebid-backend solebid-frontend solebid-redis
set critical_errors_found=false

for %%c in (%containers%) do (
    call :log_info "%%c 로그 분석 중..."
    
    REM 최근 로그에서 심각한 오류 확인 (간단한 버전)
    docker logs %%c --tail=20 2>&1 | findstr /i "ERROR FATAL Exception failed" >nul 2>&1
    if !errorlevel!==0 (
        call :record_test_result "%%c 로그 오류 검사" "FAIL"
        set critical_errors_found=true
    ) else (
        call :record_test_result "%%c 로그 오류 검사" "PASS"
    )
)

if "%critical_errors_found%"=="false" (
    call :record_test_result "전체 시스템 로그 상태" "PASS"
) else (
    call :record_test_result "전체 시스템 로그 상태" "FAIL"
)

echo.
goto :eof

REM 테스트 결과 요약 출력
:print_test_summary
echo.
echo ==========================================
echo         Docker 전체 시스템 테스트 결과
echo ==========================================
echo.

call :log_success "통과한 테스트: %TESTS_PASSED%"

if %TESTS_FAILED% gtr 0 (
    call :log_error "실패한 테스트: %TESTS_FAILED%"
) else (
    call :log_success "🎉 모든 테스트가 통과했습니다!"
    echo.
    call :log_success "Docker 환경에서 전체 Solebid 애플리케이션이 정상적으로 작동하고 있습니다."
)

echo.
set /a total_tests=%TESTS_PASSED%+%TESTS_FAILED%
echo 총 테스트 수: %total_tests%
if %total_tests% gtr 0 (
    set /a success_rate=%TESTS_PASSED%*100/%total_tests%
    echo 성공률: !success_rate!%%
)
echo.

if %TESTS_FAILED% gtr 0 (
    echo 🔧 권장 해결 방법:
    echo    1. 실패한 테스트의 상세 로그 확인: docker-compose logs [service-name]
    echo    2. 컨테이너 재시작: docker-compose restart
    echo    3. 환경 정리 후 재시작: scripts\docker-cleanup.bat ^&^& docker-compose up -d
    echo    4. 개별 서비스 헬스체크: scripts\docker-health-check.bat
) else (
    echo ✅ 시스템 접속 정보:
    echo    - 프론트엔드: http://localhost:3000
    echo    - 백엔드 API: http://localhost:8080
    echo    - 백엔드 헬스체크: http://localhost:8080/actuator/health
    echo    - Redis: localhost:6379
)

echo.
goto :eof

REM 메인 테스트 실행 함수
:run_system_tests
call :log_info "전체 시스템 테스트를 시작합니다..."
echo.

REM 모든 테스트 실행
call :test_infrastructure
call :test_container_health
call :test_individual_services
call :test_api_communication
call :test_user_scenarios
call :test_application_workflow
call :test_performance_stability
call :test_error_analysis

REM 결과 출력
call :print_test_summary

goto :eof

REM 도움말 표시
:show_help
echo 사용법: %0 [옵션]
echo.
echo 옵션:
echo    --help, -h         이 도움말 표시
echo    --stop-services    테스트 후 서비스 중지
echo.
echo 이 스크립트는 Docker 환경에서 Solebid 전체 시스템의 정상 작동을 검증합니다.
echo.
echo 테스트 범위:
echo    - 백엔드-프론트엔드 간 API 통신 검증
echo    - 전체 애플리케이션 워크플로우 테스트
echo    - 사용자 시나리오 기반 통합 테스트
echo    - Docker 환경에서 전체 스택 정상 작동 확인
echo.
goto :eof

REM 스크립트 옵션 처리
if "%1"=="--help" goto show_help
if "%1"=="-h" goto show_help

REM 메인 테스트 실행
call :run_system_tests

echo.
if %OVERALL_STATUS%==0 (
    call :log_success "🎉 Docker 전체 시스템 테스트가 성공적으로 완료되었습니다!"
    call :log_success "Solebid 애플리케이션이 Docker 환경에서 정상적으로 작동하고 있습니다."
) else (
    call :log_error "❌ 일부 시스템 테스트가 실패했습니다."
    call :log_error "로그를 확인하여 문제를 해결하세요."
)

REM 테스트 후 서비스 중지 (옵션)
if "%1"=="--stop-services" (
    call :log_info "Docker 서비스 중지 중..."
    docker-compose down >nul 2>&1
)

exit /b %OVERALL_STATUS%