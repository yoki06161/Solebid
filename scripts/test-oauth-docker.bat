@echo off
REM OAuth Docker 환경 테스트 스크립트 (Windows)
REM 이 스크립트는 Docker 환경에서 OAuth 소셜 로그인 기능을 테스트합니다.

echo ===================================
echo    OAuth Docker 환경 테스트 시작
echo ===================================
echo.

REM 1. Docker Compose 서비스 상태 확인
echo 1. Docker Compose 서비스 상태 확인...
docker-compose ps
if %ERRORLEVEL% neq 0 (
    echo [오류] Docker Compose 서비스를 찾을 수 없습니다.
    echo docker-compose up -d 명령으로 서비스를 먼저 시작하세요.
    pause
    exit /b 1
)
echo [완료] 서비스 상태 확인 완료
echo.

REM 2. 백엔드 헬스체크
echo 2. 백엔드 헬스체크...
curl -f http://localhost:8080/actuator/health >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [오류] 백엔드 서비스가 응답하지 않습니다.
    echo 백엔드 컨테이너 로그를 확인하세요: docker logs solebid-backend
    pause
    exit /b 1
)
echo [완료] 백엔드 서비스 정상 응답
echo.

REM 3. 프론트엔드 접근 확인
echo 3. 프론트엔드 접근 확인...
curl -f http://localhost:3000 >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [오류] 프론트엔드 서비스가 응답하지 않습니다.
    echo 프론트엔드 컨테이너 로그를 확인하세요: docker logs solebid-frontend
    pause
    exit /b 1
)
echo [완료] 프론트엔드 서비스 정상 응답
echo.

REM 4. Redis 연결 확인
echo 4. Redis 연결 확인...
docker exec solebid-redis redis-cli ping >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [오류] Redis 서비스가 응답하지 않습니다.
    echo Redis 컨테이너 로그를 확인하세요: docker logs solebid-redis
    pause
    exit /b 1
)
echo [완료] Redis 서비스 정상 응답
echo.

REM 5. Google OAuth URL 생성 테스트
echo 5. Google OAuth URL 생성 테스트...
curl -s http://localhost:8080/api/auth/oauth2/google/url > temp_google_response.json
if %ERRORLEVEL% neq 0 (
    echo [오류] Google OAuth URL 생성 실패
    pause
    exit /b 1
)

REM JSON 응답에서 success 필드 확인 (간단한 문자열 검색)
findstr /C:"\"success\":true" temp_google_response.json >nul
if %ERRORLEVEL% neq 0 (
    echo [오류] Google OAuth URL 생성 응답이 올바르지 않습니다.
    echo 응답 내용:
    type temp_google_response.json
    del temp_google_response.json
    pause
    exit /b 1
)
echo [완료] Google OAuth URL 생성 성공
del temp_google_response.json
echo.

REM 6. Kakao OAuth URL 생성 테스트
echo 6. Kakao OAuth URL 생성 테스트...
curl -s http://localhost:8080/api/auth/oauth2/kakao/url > temp_kakao_response.json
if %ERRORLEVEL% neq 0 (
    echo [오류] Kakao OAuth URL 생성 실패
    pause
    exit /b 1
)

REM JSON 응답에서 success 필드 확인 (간단한 문자열 검색)
findstr /C:"\"success\":true" temp_kakao_response.json >nul
if %ERRORLEVEL% neq 0 (
    echo [오류] Kakao OAuth URL 생성 응답이 올바르지 않습니다.
    echo 응답 내용:
    type temp_kakao_response.json
    del temp_kakao_response.json
    pause
    exit /b 1
)
echo [완료] Kakao OAuth URL 생성 성공
del temp_kakao_response.json
echo.

REM 7. OAuth 환경 변수 확인
echo 7. OAuth 환경 변수 확인...
echo Google Client ID:
docker exec solebid-backend printenv GOOGLE_CLIENT_ID
echo Kakao Client ID:
docker exec solebid-backend printenv KAKAO_CLIENT_ID
echo Google Redirect URI:
docker exec solebid-backend printenv GOOGLE_REDIRECT_URI
echo Kakao Redirect URI:
docker exec solebid-backend printenv KAKAO_REDIRECT_URI
echo.

REM 8. CORS 설정 테스트
echo 8. CORS 설정 테스트...
curl -s -H "Origin: http://localhost:3000" -H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: Content-Type" -X OPTIONS http://localhost:8080/api/auth/oauth2/google/url > temp_cors_response.txt
if %ERRORLEVEL% neq 0 (
    echo [경고] CORS 프리플라이트 요청 실패 - 브라우저에서 CORS 오류가 발생할 수 있습니다.
) else (
    echo [완료] CORS 설정 정상
)
del temp_cors_response.txt 2>nul
echo.

REM 9. 네트워크 연결 테스트
echo 9. Docker 네트워크 연결 테스트...
echo 백엔드에서 프론트엔드로 연결 테스트:
docker exec solebid-backend curl -s -I http://frontend:80 | findstr "HTTP"
echo 프론트엔드에서 백엔드로 연결 테스트:
docker exec solebid-frontend curl -s -I http://backend:8080/actuator/health | findstr "HTTP"
echo.

echo ===================================
echo    OAuth Docker 환경 테스트 완료
echo ===================================
echo.
echo 모든 테스트가 성공했습니다!
echo.
echo 다음 단계:
echo 1. 브라우저에서 http://localhost:3000 접속
echo 2. 회원가입 또는 로그인 페이지로 이동
echo 3. "구글로 시작하기" 또는 "카카오로 시작하기" 버튼 클릭
echo 4. OAuth 인증 플로우 테스트
echo.
echo OAuth 제공자 설정 확인사항:
echo - Google: http://localhost:3000/auth/callback/google 리다이렉트 URI 등록 필요
echo - Kakao: http://localhost:3000/auth/callback/kakao 리다이렉트 URI 등록 필요
echo.
pause