@echo off
REM HTTPS 연결 테스트 스크립트 (Windows)

echo ========================================
echo HTTPS 연결 테스트
echo ========================================
echo.

echo [1] 컨테이너 상태 확인...
docker ps --filter "name=solebid-frontend" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

echo [2] SSL 인증서 확인...
docker exec solebid-frontend ls -lh /etc/nginx/ssl/
echo.

echo [3] Nginx 설정 테스트...
docker exec solebid-frontend nginx -t
echo.

echo [4] HTTPS 엔드포인트 테스트 (컨테이너 내부)...
docker exec solebid-frontend wget --no-check-certificate -O- https://localhost:443/health/ssl
echo.

echo [5] HTTPS 포트 리스닝 확인...
netstat -an | findstr ":3443"
echo.

echo ========================================
echo 브라우저 테스트 방법:
echo ========================================
echo 1. 브라우저를 완전히 종료하세요
echo 2. 브라우저 캐시를 삭제하세요
echo 3. 새 시크릿/프라이빗 창을 여세요
echo 4. https://localhost:3443 에 접속하세요
echo 5. "고급" 버튼을 클릭하고 "계속 진행"을 선택하세요
echo.
echo 또는 HTTP로 접속하면 자동으로 HTTPS로 리다이렉트됩니다:
echo http://localhost:3000
echo.

pause
