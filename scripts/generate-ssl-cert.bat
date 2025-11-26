@echo off
REM ============================================================================
REM SSL 자체 서명 인증서 생성 스크립트 (개발 환경용 - Windows)
REM ============================================================================
REM 이 스크립트는 로컬 개발 환경에서 HTTPS 테스트를 위한 자체 서명 인증서를 생성합니다.
REM 
REM 사용법:
REM   scripts\generate-ssl-cert.bat [도메인] [유효기간(일)]
REM
REM 예시:
REM   scripts\generate-ssl-cert.bat localhost 365
REM   scripts\generate-ssl-cert.bat example.com 730
REM
REM 생성되는 파일:
REM   - nginx\ssl\cert.pem: SSL 인증서 (공개키)
REM   - nginx\ssl\key.pem: 개인키 (비밀키)
REM ============================================================================

setlocal enabledelayedexpansion

REM 기본값 설정
set "DEFAULT_DOMAIN=localhost"
set "DEFAULT_DAYS=365"

REM 인자 처리
if "%~1"=="" (
    set "DOMAIN=%DEFAULT_DOMAIN%"
) else (
    set "DOMAIN=%~1"
)

if "%~2"=="" (
    set "DAYS=%DEFAULT_DAYS%"
) else (
    set "DAYS=%~2"
)

REM SSL 디렉토리 경로
set "SSL_DIR=nginx\ssl"
set "CERT_FILE=%SSL_DIR%\cert.pem"
set "KEY_FILE=%SSL_DIR%\key.pem"
set "CSR_FILE=%SSL_DIR%\cert.csr"

REM ============================================================================
REM 배너 출력
REM ============================================================================
echo.
echo ============================================
echo   SSL 자체 서명 인증서 생성 스크립트
echo ============================================
echo.

REM ============================================================================
REM OpenSSL 설치 확인
REM ============================================================================
echo [INFO] OpenSSL 설치 확인 중...

where openssl >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] OpenSSL이 설치되어 있지 않거나 PATH에 없습니다.
    echo.
    echo OpenSSL 설치 방법:
    echo   1. Git for Windows 설치 (OpenSSL 포함)
    echo      https://git-scm.com/download/win
    echo   2. 또는 OpenSSL 직접 설치
    echo      https://slproweb.com/products/Win32OpenSSL.html
    echo.
    exit /b 1
)

for /f "tokens=*" %%i in ('openssl version') do set "OPENSSL_VERSION=%%i"
echo [SUCCESS] OpenSSL 발견: %OPENSSL_VERSION%

REM ============================================================================
REM SSL 디렉토리 생성
REM ============================================================================
echo [INFO] SSL 디렉토리 생성 중: %SSL_DIR%

if not exist "%SSL_DIR%" (
    mkdir "%SSL_DIR%" 2>nul
    if %errorlevel% neq 0 (
        echo [ERROR] SSL 디렉토리 생성 실패: %SSL_DIR%
        exit /b 1
    )
    echo [SUCCESS] SSL 디렉토리 생성 완료
) else (
    echo [INFO] SSL 디렉토리가 이미 존재합니다
)

REM ============================================================================
REM 기존 인증서 백업
REM ============================================================================
if exist "%CERT_FILE%" (
    echo [WARNING] 기존 인증서 파일이 발견되었습니다
    
    REM 백업 디렉토리 생성 (타임스탬프 포함)
    for /f "tokens=2 delims==" %%i in ('wmic os get localdatetime /value') do set datetime=%%i
    set "BACKUP_DIR=%SSL_DIR%\backup_%datetime:~0,8%_%datetime:~8,6%"
    mkdir "%BACKUP_DIR%" 2>nul
    
    if exist "%CERT_FILE%" (
        move /y "%CERT_FILE%" "%BACKUP_DIR%\" >nul 2>&1
        echo [INFO] 기존 인증서를 백업했습니다: %BACKUP_DIR%\cert.pem
    )
    
    if exist "%KEY_FILE%" (
        move /y "%KEY_FILE%" "%BACKUP_DIR%\" >nul 2>&1
        echo [INFO] 기존 개인키를 백업했습니다: %BACKUP_DIR%\key.pem
    )
    
    if exist "%CSR_FILE%" (
        move /y "%CSR_FILE%" "%BACKUP_DIR%\" >nul 2>&1
    )
)

REM ============================================================================
REM SSL 인증서 생성
REM ============================================================================
echo [INFO] SSL 인증서 생성 중...
echo [INFO] 도메인: %DOMAIN%
echo [INFO] 유효기간: %DAYS%일
echo [INFO] 키 강도: 2048-bit RSA

REM Subject Alternative Name (SAN) 설정 파일 생성
set "SAN_CONFIG=%TEMP%\san_config_%RANDOM%.cnf"

(
echo [req]
echo default_bits = 2048
echo prompt = no
echo default_md = sha256
echo distinguished_name = dn
echo req_extensions = v3_req
echo.
echo [dn]
echo C=KR
echo ST=Seoul
echo L=Seoul
echo O=Solebid Development
echo OU=Development Team
echo CN=%DOMAIN%
echo.
echo [v3_req]
echo subjectAltName = @alt_names
echo.
echo [alt_names]
echo DNS.1 = %DOMAIN%
echo DNS.2 = localhost
echo DNS.3 = *.localhost
echo IP.1 = 127.0.0.1
echo IP.2 = ::1
) > "%SAN_CONFIG%"

REM 개인키 및 자체 서명 인증서 생성
openssl req -x509 -nodes -days %DAYS% -newkey rsa:2048 -keyout "%KEY_FILE%" -out "%CERT_FILE%" -config "%SAN_CONFIG%" -extensions v3_req 2>nul

REM 임시 설정 파일 삭제
del /f /q "%SAN_CONFIG%" >nul 2>&1

REM 생성 확인
if not exist "%CERT_FILE%" (
    echo [ERROR] 인증서 생성 실패
    exit /b 1
)

if not exist "%KEY_FILE%" (
    echo [ERROR] 개인키 생성 실패
    exit /b 1
)

echo [SUCCESS] SSL 인증서 생성 완료

REM ============================================================================
REM 파일 권한 설정 (Windows)
REM ============================================================================
echo [INFO] 파일 권한 설정 중...

REM Windows에서는 icacls를 사용하여 권한 설정
REM 개인키: 현재 사용자만 읽기/쓰기
icacls "%KEY_FILE%" /inheritance:r /grant:r "%USERNAME%:(R,W)" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] 개인키 권한 설정 완료: 현재 사용자만 읽기/쓰기
) else (
    echo [WARNING] 개인키 권한 설정 실패 (관리자 권한 필요할 수 있음)
)

REM 인증서: 읽기 권한
icacls "%CERT_FILE%" /inheritance:r /grant:r "%USERNAME%:(R,W)" /grant:r "Users:(R)" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] 인증서 권한 설정 완료: 모두 읽기 가능
) else (
    echo [WARNING] 인증서 권한 설정 실패 (관리자 권한 필요할 수 있음)
)

REM ============================================================================
REM 인증서 정보 출력
REM ============================================================================
echo.
echo [INFO] 생성된 인증서 정보:
echo.
echo 인증서 파일: %CERT_FILE%
echo 개인키 파일: %KEY_FILE%
echo.

echo 인증서 상세 정보:
openssl x509 -in "%CERT_FILE%" -noout -subject -issuer -dates -fingerprint
echo.

REM 키 강도 확인
for /f "tokens=2 delims=:" %%i in ('openssl rsa -in "%KEY_FILE%" -text -noout 2^>nul ^| findstr "Private-Key"') do (
    set "KEY_BITS=%%i"
    set "KEY_BITS=!KEY_BITS: =!"
    set "KEY_BITS=!KEY_BITS:(=!"
    set "KEY_BITS=!KEY_BITS:bit=!"
    set "KEY_BITS=!KEY_BITS:)=!"
)
echo 키 강도: %KEY_BITS% bits
echo.

echo Subject Alternative Names (SAN):
openssl x509 -in "%CERT_FILE%" -noout -text | findstr /C:"Subject Alternative Name" /C:"DNS:" /C:"IP Address:"
echo.

REM ============================================================================
REM 사용 안내 출력
REM ============================================================================
echo.
echo ============================================
echo   인증서 생성 완료!
echo ============================================
echo.
echo 다음 단계:
echo   1. Docker Compose로 서비스 시작:
echo      docker-compose up -d
echo.
echo   2. 브라우저에서 접속:
echo      https://localhost:3443
echo.
echo   3. 브라우저 보안 경고:
echo      - 자체 서명 인증서이므로 '안전하지 않음' 경고가 표시됩니다
echo      - '고급' -^> '계속 진행' 클릭하여 접속하세요
echo.
echo 참고:
echo   - 이 인증서는 개발 환경 전용입니다
echo   - 프로덕션 환경에서는 Let's Encrypt를 사용하세요
echo   - 인증서 유효기간: %DAYS%일
echo.

endlocal
exit /b 0
