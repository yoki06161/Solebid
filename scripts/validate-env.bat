@echo off
setlocal enabledelayedexpansion

REM Solebid 환경 변수 검증 스크립트 (Windows)
REM 이 스크립트는 Docker 환경에서 필요한 환경 변수들이 올바르게 설정되었는지 확인합니다.

echo ==========================================
echo    Solebid 환경 변수 검증 스크립트
echo ==========================================
echo.

REM 검증 결과 카운터
set TOTAL_CHECKS=0
set PASSED_CHECKS=0
set FAILED_CHECKS=0
set WARNING_CHECKS=0

REM .env 파일 존재 확인
echo 1. .env 파일 존재 확인...
set /a TOTAL_CHECKS+=1

if exist ".env" (
    echo [32m✓[0m .env 파일이 존재합니다.
    set /a PASSED_CHECKS+=1
) else (
    echo [31m✗[0m .env 파일이 존재하지 않습니다.
    echo [33m  해결방법: copy .env.example .env[0m
    set /a FAILED_CHECKS+=1
    goto :end_with_error
)

echo.
echo 2. 필수 환경 변수 검증...

REM 필수 환경 변수들 검증
call :check_env_var "DB_URL" "데이터베이스 연결 URL" true
call :check_env_var "DB_USERNAME" "데이터베이스 사용자명" true
call :check_env_var "DB_PASSWORD" "데이터베이스 비밀번호" true
call :check_env_var "JWT_SECRET" "JWT 토큰 시크릿" true
call :check_env_var "AWS_ACCESS_KEY" "AWS 액세스 키" true
call :check_env_var "AWS_SECRET_KEY" "AWS 시크릿 키" true
call :check_env_var "S3_BUCKET" "S3 버킷 이름" true
call :check_env_var "GOOGLE_CLIENT_ID" "Google OAuth2 클라이언트 ID" true
call :check_env_var "GOOGLE_CLIENT_SECRET" "Google OAuth2 클라이언트 시크릿" true
call :check_env_var "KAKAO_CLIENT_ID" "Kakao OAuth2 클라이언트 ID" true
call :check_env_var "KAKAO_CLIENT_SECRET" "Kakao OAuth2 클라이언트 시크릿" true
call :check_env_var "KAKAO_ADMIN_KEY" "Kakao API 관리자 키" true
call :check_env_var "PORTONE_API_KEY" "PortOne API 키" true
call :check_env_var "PORTONE_API_SECRET" "PortOne API 시크릿" true

REM JWT 시크릿 길이 검증
echo.
echo 3. JWT 시크릿 보안 검증...
set /a TOTAL_CHECKS+=1

for /f "tokens=2 delims==" %%a in ('findstr "^JWT_SECRET=" .env 2^>nul') do set JWT_SECRET=%%a
if defined JWT_SECRET (
    set JWT_LENGTH=0
    set JWT_TEMP=!JWT_SECRET!
    :count_loop
    if defined JWT_TEMP (
        set JWT_TEMP=!JWT_TEMP:~1!
        set /a JWT_LENGTH+=1
        goto count_loop
    )
    
    if !JWT_LENGTH! geq 64 (
        echo [32m✓[0m JWT_SECRET 길이가 충분합니다 ^(!JWT_LENGTH! 바이트^)
        set /a PASSED_CHECKS+=1
    ) else (
        echo [31m✗[0m JWT_SECRET 길이가 부족합니다 ^(!JWT_LENGTH! 바이트, 최소 64바이트 필요^)
        echo [33m  보안을 위해 더 긴 시크릿을 사용하세요[0m
        set /a FAILED_CHECKS+=1
    )
) else (
    echo [31m✗[0m JWT_SECRET이 설정되지 않았습니다
    set /a FAILED_CHECKS+=1
)

echo.
echo 4. 선택적 환경 변수 검증...

REM 선택적 환경 변수들 검증
call :check_env_var "MAIL_USERNAME" "이메일 사용자명" false
call :check_env_var "GOOGLE_MAIL_REFRESH_TOKEN" "Gmail OAuth2 리프레시 토큰" false
call :check_env_var "REDIS_PASSWORD" "Redis 비밀번호" false

REM 결과 요약
echo.
echo ==========================================
echo    검증 결과 요약
echo ==========================================
echo 총 검사 항목: !TOTAL_CHECKS!
echo [32m통과: !PASSED_CHECKS![0m
echo [31m실패: !FAILED_CHECKS![0m
echo [33m경고: !WARNING_CHECKS![0m
echo.

if !FAILED_CHECKS! equ 0 (
    echo [32m✓ 모든 필수 환경 변수가 올바르게 설정되었습니다![0m
    echo [32m  Docker Compose를 실행할 수 있습니다.[0m
    echo.
    echo 실행 명령어:
    echo    개발 환경: docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
    echo    프로덕션: docker-compose up
    goto :end_success
) else (
    echo [31m✗ !FAILED_CHECKS!개의 필수 환경 변수가 설정되지 않았습니다.[0m
    echo [33m  .env 파일을 수정한 후 다시 실행하세요.[0m
    echo.
    echo 도움말:
    echo    1. .env.example 파일을 참고하여 .env 파일을 수정하세요
    echo    2. docs\environment-variables-guide.md 문서를 참고하세요
    goto :end_with_error
)

REM 환경 변수 검증 함수
:check_env_var
set var_name=%~1
set var_description=%~2
set is_required=%~3

set /a TOTAL_CHECKS+=1

REM .env 파일에서 변수 값 읽기
for /f "tokens=2 delims==" %%a in ('findstr "^%var_name%=" .env 2^>nul') do set var_value=%%a

if not defined var_value (
    set var_value=
)

REM 기본값이나 템플릿 값인지 확인
echo !var_value! | findstr /i "your-" >nul
if !errorlevel! equ 0 set var_value=

if "!var_value!"=="" (
    if "%is_required%"=="true" (
        echo [31m✗[0m %var_name%: %var_description% - 설정되지 않음
        set /a FAILED_CHECKS+=1
    ) else (
        echo [33m⚠[0m %var_name%: %var_description% - 선택적 설정
        set /a WARNING_CHECKS+=1
    )
) else (
    echo [32m✓[0m %var_name%: %var_description% - 설정됨
    set /a PASSED_CHECKS+=1
)
goto :eof

:end_success
exit /b 0

:end_with_error
echo [31m환경 변수 검증을 완료할 수 없습니다.[0m
exit /b 1