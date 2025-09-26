@echo off
REM Solebid 프로덕션 배포 스크립트 (Windows)
REM 프로덕션 환경에서 Docker Compose를 사용한 안전한 배포

setlocal enabledelayedexpansion

REM 색상 코드 (Windows 10 이상에서 지원)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM 로그 함수들
:log_info
echo %BLUE%[INFO]%NC% %~1
goto :eof

:log_success
echo %GREEN%[SUCCESS]%NC% %~1
goto :eof

:log_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:log_error
echo %RED%[ERROR]%NC% %~1
goto :eof

REM 필수 조건 확인
:check_prerequisites
call :log_info "프로덕션 배포 전 필수 조건 확인 중..."

REM Docker 설치 확인
docker --version >nul 2>&1
if errorlevel 1 (
    call :log_error "Docker가 설치되지 않았습니다."
    exit /b 1
)

REM Docker Compose 설치 확인
docker-compose --version >nul 2>&1
if errorlevel 1 (
    call :log_error "Docker Compose가 설치되지 않았습니다."
    exit /b 1
)

REM .env.prod 파일 존재 확인
if not exist ".env.prod" (
    call :log_error ".env.prod 파일이 존재하지 않습니다."
    call :log_info ".env.prod.example을 복사하여 .env.prod를 생성하고 값을 설정하세요."
    exit /b 1
)

REM 필수 디렉토리 생성
if not exist "data" mkdir data
if not exist "data\redis" mkdir data\redis
if not exist "data\prometheus" mkdir data\prometheus
if not exist "logs" mkdir logs

call :log_success "필수 조건 확인 완료"
goto :eof

REM 백업 생성
:create_backup
call :log_info "기존 데이터 백업 생성 중..."

REM 백업 디렉토리 생성 (날짜_시간 형식)
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "backup_dir=backups\%YYYY%%MM%%DD%_%HH%%Min%%Sec%"

if not exist "backups" mkdir backups
mkdir "%backup_dir%"

REM Redis 데이터 백업
if exist "data\redis" (
    xcopy "data\redis" "%backup_dir%\redis\" /E /I /Q >nul
    call :log_success "Redis 데이터 백업 완료: %backup_dir%\redis"
)

REM Prometheus 데이터 백업
if exist "data\prometheus" (
    xcopy "data\prometheus" "%backup_dir%\prometheus\" /E /I /Q >nul
    call :log_success "Prometheus 데이터 백업 완료: %backup_dir%\prometheus"
)

REM 로그 백업
if exist "logs" (
    xcopy "logs" "%backup_dir%\logs\" /E /I /Q >nul
    call :log_success "로그 백업 완료: %backup_dir%\logs"
)

call :log_success "백업 생성 완료: %backup_dir%"
goto :eof

REM 이미지 빌드
:build_images
call :log_info "프로덕션 이미지 빌드 중..."

REM 기존 컨테이너 중지
docker-compose -f docker-compose.prod.yml down --remove-orphans 2>nul

REM 이미지 빌드
docker-compose -f docker-compose.prod.yml build --no-cache --parallel
if errorlevel 1 (
    call :log_error "이미지 빌드 실패"
    exit /b 1
)

call :log_success "이미지 빌드 완료"
goto :eof

REM 서비스 배포
:deploy_services
call :log_info "프로덕션 서비스 배포 중..."

REM 서비스 시작
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
if errorlevel 1 (
    call :log_error "서비스 배포 실패"
    exit /b 1
)

call :log_success "서비스 배포 완료"
goto :eof

REM 헬스체크
:health_check
call :log_info "서비스 헬스체크 수행 중..."

REM 서비스 시작 대기
timeout /t 30 /nobreak >nul

REM 백엔드 헬스체크 (최대 30회 시도)
set /a attempt=1
set /a max_attempts=30

:backend_health_loop
curl -f http://localhost:8080/actuator/health >nul 2>&1
if not errorlevel 1 (
    call :log_success "백엔드 헬스체크 통과"
    goto :frontend_health
)

call :log_info "백엔드 시작 대기 중... (!attempt!/!max_attempts!)"
timeout /t 10 /nobreak >nul
set /a attempt+=1

if !attempt! leq !max_attempts! goto :backend_health_loop

call :log_error "백엔드 헬스체크 실패"
exit /b 1

:frontend_health
REM 프론트엔드 헬스체크
curl -f http://localhost:80 >nul 2>&1
if errorlevel 1 (
    call :log_error "프론트엔드 헬스체크 실패"
    exit /b 1
)

call :log_success "프론트엔드 헬스체크 통과"
call :log_success "모든 서비스 헬스체크 통과"
goto :eof

REM 배포 후 정리
:cleanup
call :log_info "배포 후 정리 작업 수행 중..."

REM 사용하지 않는 이미지 정리
docker image prune -f >nul 2>&1

REM 오래된 백업 정리 (30일 이상) - Windows에서는 PowerShell 사용
powershell -Command "Get-ChildItem -Path 'backups' -Directory | Where-Object {$_.LastWriteTime -lt (Get-Date).AddDays(-30)} | Remove-Item -Recurse -Force" 2>nul

call :log_success "정리 작업 완료"
goto :eof

REM 배포 상태 확인
:show_status
call :log_info "배포 상태 확인"

echo.
echo === 컨테이너 상태 ===
docker-compose -f docker-compose.prod.yml ps

echo.
echo === 서비스 URL ===
echo 프론트엔드: http://localhost:80
echo 백엔드 API: http://localhost:8080
echo 백엔드 헬스체크: http://localhost:8080/actuator/health
echo Prometheus: http://localhost:9090

echo.
echo === 로그 확인 명령어 ===
echo 전체 로그: docker-compose -f docker-compose.prod.yml logs -f
echo 백엔드 로그: docker-compose -f docker-compose.prod.yml logs -f backend
echo 프론트엔드 로그: docker-compose -f docker-compose.prod.yml logs -f frontend
echo Redis 로그: docker-compose -f docker-compose.prod.yml logs -f redis
goto :eof

REM 롤백 함수
:rollback
call :log_warning "배포 롤백을 수행합니다..."

REM 현재 서비스 중지
docker-compose -f docker-compose.prod.yml down

REM 가장 최근 백업 찾기
for /f "delims=" %%i in ('dir /b /od backups 2^>nul ^| findstr /r ".*"') do set "latest_backup=%%i"

if defined latest_backup (
    call :log_info "백업에서 데이터 복원 중: !latest_backup!"
    
    REM Redis 데이터 복원
    if exist "backups\!latest_backup!\redis" (
        if exist "data\redis" rmdir /s /q "data\redis"
        xcopy "backups\!latest_backup!\redis" "data\redis\" /E /I /Q >nul
    )
    
    REM Prometheus 데이터 복원
    if exist "backups\!latest_backup!\prometheus" (
        if exist "data\prometheus" rmdir /s /q "data\prometheus"
        xcopy "backups\!latest_backup!\prometheus" "data\prometheus\" /E /I /Q >nul
    )
    
    call :log_success "데이터 복원 완료"
) else (
    call :log_warning "복원할 백업이 없습니다."
)
goto :eof

REM 메인 배포 함수
:main
call :log_info "Solebid 프로덕션 배포를 시작합니다..."

REM 배포 전 확인
call :check_prerequisites
if errorlevel 1 exit /b 1

REM 사용자 확인
echo.
set /p "confirm=프로덕션 배포를 계속하시겠습니까? (y/N): "
if /i not "%confirm%"=="y" (
    call :log_info "배포가 취소되었습니다."
    exit /b 0
)

REM 배포 실행
call :create_backup
if errorlevel 1 goto :deploy_error

call :build_images
if errorlevel 1 goto :deploy_error

call :deploy_services
if errorlevel 1 goto :deploy_error

call :health_check
if errorlevel 1 goto :deploy_error

call :cleanup
call :show_status
call :log_success "프로덕션 배포가 성공적으로 완료되었습니다!"
goto :eof

:deploy_error
call :log_error "배포 중 오류가 발생했습니다."
set /p "rollback_confirm=롤백을 수행하시겠습니까? (y/N): "
if /i "%rollback_confirm%"=="y" call :rollback
exit /b 1

REM 스크립트 인자 처리
if "%~1"=="" goto :main
if /i "%~1"=="deploy" goto :main
if /i "%~1"=="rollback" goto :rollback
if /i "%~1"=="status" goto :show_status
if /i "%~1"=="health" goto :health_check

echo 사용법: %0 [deploy^|rollback^|status^|health]
echo    deploy  : 프로덕션 배포 (기본값)
echo    rollback: 이전 버전으로 롤백
echo    status  : 현재 배포 상태 확인
echo    health  : 헬스체크 수행
exit /b 1