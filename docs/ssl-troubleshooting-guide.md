# SSL/HTTPS 문제 해결 가이드

이 문서는 SSL/HTTPS 설정 중 발생할 수 있는 일반적인 문제와 해결 방법을 제공합니다.

## 목차

1. [인증서 관련 문제](#인증서-관련-문제)
2. [Nginx 설정 문제](#nginx-설정-문제)
3. [Docker 관련 문제](#docker-관련-문제)
4. [Let's Encrypt 문제](#lets-encrypt-문제)
5. [브라우저 관련 문제](#브라우저-관련-문제)
6. [OAuth2 HTTPS 문제](#oauth2-https-문제)
7. [성능 관련 문제](#성능-관련-문제)
8. [로그 분석](#로그-분석)

## 인증서 관련 문제

### 1. "인증서 파일을 찾을 수 없습니다"

**증상**:
```
[ERROR] 인증서 파일이 존재하지 않습니다: nginx/ssl/cert.pem
```

**원인**: SSL 인증서가 생성되지 않았거나 경로가 잘못됨

**해결 방법**:
```bash
# 1. 인증서 생성
./scripts/generate-ssl-cert.sh

# 2. 파일 존재 확인
ls -la nginx/ssl/

# 3. 권한 확인
chmod 600 nginx/ssl/key.pem
chmod 644 nginx/ssl/cert.pem
```

### 2. "인증서와 개인키가 일치하지 않습니다"

**증상**:
```
[FAIL] 인증서와 개인키가 일치하지 않습니다
```

**원인**: 인증서와 개인키가 서로 다른 쌍

**해결 방법**:
```bash
# 1. 인증서와 키 일치 확인
cert_modulus=$(openssl x509 -noout -modulus -in nginx/ssl/cert.pem | openssl md5)
key_modulus=$(openssl rsa -noout -modulus -in nginx/ssl/key.pem | openssl md5)
echo "Certificate: $cert_modulus"
echo "Key: $key_modulus"

# 2. 일치하지 않으면 새로 생성
if [ "$cert_modulus" != "$key_modulus" ]; then
    ./scripts/generate-ssl-cert.sh
fi
```

### 3. "인증서가 만료되었습니다"

**증상**:
```
[FAIL] 인증서가 만료되었습니다 (만료일: Dec 1 00:00:00 2023 GMT)
```

**해결 방법**:

**개발 환경**:
```bash
# 새 인증서 생성
./scripts/generate-ssl-cert.sh localhost 365
docker-compose restart frontend
```

**프로덕션 환경**:
```bash
# 인증서 갱신
./scripts/renew-certificates.sh
```

### 4. "키 강도가 부족합니다"

**증상**:
```
[FAIL] 키 강도가 부족합니다: 1024 bits (최소 2048 bits 필요)
```

**해결 방법**:
```bash
# 2048-bit 키로 새 인증서 생성
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout nginx/ssl/key.pem -out nginx/ssl/cert.pem \
    -subj "/CN=localhost"
```

### 5. "파일 권한이 안전하지 않습니다"

**증상**:
```
[FAIL] 개인키 파일 권한이 안전하지 않습니다: 644 (권장: 600)
```

**해결 방법**:
```bash
# 올바른 권한 설정
chmod 600 nginx/ssl/key.pem    # 개인키: 소유자만 읽기/쓰기
chmod 644 nginx/ssl/cert.pem   # 인증서: 모두 읽기 가능

# 소유권 확인
chown $USER:$USER nginx/ssl/*
```

## Nginx 설정 문제

### 1. "Nginx 설정 구문 오류"

**증상**:
```
nginx: [emerg] unexpected "}" in /etc/nginx/nginx.conf:45
```

**해결 방법**:
```bash
# 1. 설정 파일 구문 검사
docker run --rm -v $(pwd)/frontend/nginx-ssl.conf:/etc/nginx/nginx.conf:ro nginx nginx -t

# 2. 오류 위치 확인
cat -n frontend/nginx-ssl.conf | sed -n '40,50p'

# 3. 백업에서 복원
cp frontend/nginx.conf frontend/nginx-ssl.conf
# 또는 다시 생성
git checkout frontend/nginx-ssl.conf
```

### 2. "SSL 인증서 경로를 찾을 수 없습니다"

**증상**:
```
nginx: [emerg] cannot load certificate "/etc/nginx/ssl/cert.pem": BIO_new_file() failed
```

**해결 방법**:
```bash
# 1. Docker 볼륨 마운트 확인
docker-compose config | grep -A 5 -B 5 "nginx/ssl"

# 2. 컨테이너 내부에서 파일 확인
docker exec solebid-frontend ls -la /etc/nginx/ssl/

# 3. 볼륨 마운트 재설정
docker-compose down
docker-compose up -d
```

### 3. "HTTP에서 HTTPS로 리다이렉트되지 않습니다"

**증상**: HTTP 요청이 HTTPS로 리다이렉트되지 않음

**해결 방법**:
```bash
# 1. Nginx 설정에서 리다이렉트 확인
grep -n "return 301 https" frontend/nginx-ssl.conf

# 2. 설정이 없으면 추가
cat >> frontend/nginx-ssl.conf << 'EOF'
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}
EOF

# 3. Nginx 재로드
docker exec solebid-frontend nginx -s reload
```

### 4. "HSTS 헤더가 설정되지 않습니다"

**증상**: 브라우저에서 HSTS 헤더를 확인할 수 없음

**해결 방법**:
```bash
# 1. HSTS 헤더 설정 확인
grep -n "Strict-Transport-Security" frontend/nginx-ssl.conf

# 2. 설정이 없으면 추가
sed -i '/server {/a\    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;' frontend/nginx-ssl.conf

# 3. 테스트
curl -I https://localhost:3443 | grep -i strict
```

## Docker 관련 문제

### 1. "포트 443이 이미 사용 중입니다"

**증상**:
```
ERROR: for solebid-frontend  Cannot start service frontend: driver failed programming external connectivity on endpoint solebid-frontend: Bind for 0.0.0.0:443 failed: port is already allocated
```

**해결 방법**:
```bash
# 1. 포트 사용 프로세스 확인
sudo netstat -tulpn | grep :443
# 또는
sudo ss -tulpn | grep :443

# 2. 다른 포트 사용 (개발 환경)
echo "DOCKER_FRONTEND_HTTPS_PORT=8443" >> .env
docker-compose up -d

# 3. 기존 서비스 중지
sudo systemctl stop apache2  # Apache가 443 포트 사용 중인 경우
sudo systemctl stop nginx    # 시스템 Nginx가 443 포트 사용 중인 경우
```

### 2. "SSL 볼륨 마운트 실패"

**증상**:
```
ERROR: for solebid-frontend  Cannot start service frontend: invalid mount config for type "bind": bind source path does not exist: /path/to/nginx/ssl
```

**해결 방법**:
```bash
# 1. SSL 디렉토리 생성
mkdir -p nginx/ssl

# 2. 인증서 생성
./scripts/generate-ssl-cert.sh

# 3. 절대 경로 확인
echo "Current directory: $(pwd)"
ls -la nginx/ssl/

# 4. Docker Compose 재시작
docker-compose down
docker-compose up -d
```

### 3. "컨테이너가 시작되지 않습니다"

**증상**: Frontend 컨테이너가 계속 재시작됨

**해결 방법**:
```bash
# 1. 컨테이너 로그 확인
docker logs solebid-frontend

# 2. 설정 파일 검증
docker run --rm -v $(pwd)/frontend/nginx-ssl.conf:/etc/nginx/nginx.conf:ro nginx nginx -t

# 3. 헬스체크 비활성화 후 테스트
docker-compose up --no-deps frontend

# 4. 기본 설정으로 복원
cp frontend/nginx.conf frontend/nginx-ssl.conf
```

### 4. "환경 변수가 적용되지 않습니다"

**증상**: NGINX_SSL_ENABLED 등 환경 변수가 무시됨

**해결 방법**:
```bash
# 1. 환경 변수 파일 확인
cat .env | grep SSL

# 2. Docker Compose에서 환경 변수 확인
docker-compose config | grep -A 10 environment

# 3. 컨테이너 내부에서 환경 변수 확인
docker exec solebid-frontend env | grep SSL

# 4. 캐시 클리어 후 재시작
docker-compose down
docker system prune -f
docker-compose up -d
```

## Let's Encrypt 문제

### 1. "도메인 DNS 설정을 확인할 수 없습니다"

**증상**:
```
[ERROR] 도메인 example.com의 DNS 설정을 확인할 수 없습니다
```

**해결 방법**:
```bash
# 1. DNS 전파 확인
nslookup your-domain.com
dig your-domain.com

# 2. 외부 DNS 서버에서 확인
nslookup your-domain.com 8.8.8.8

# 3. DNS 전파 대기 (최대 48시간)
# 4. DNS 설정이 올바른지 도메인 제공업체에서 확인
```

### 2. "포트 80이 접근 불가능합니다"

**증상**:
```
[ERROR] 포트 80이 접근 불가능합니다
```

**해결 방법**:
```bash
# 1. 방화벽 설정 확인
sudo ufw status
sudo firewall-cmd --list-all

# 2. 포트 80 열기
sudo ufw allow 80/tcp
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --reload

# 3. 웹서버가 포트 80에서 실행 중인지 확인
sudo netstat -tulpn | grep :80

# 4. 외부에서 접근 테스트
curl -I http://your-domain.com
```

### 3. "Certbot 인증 실패"

**증상**:
```
Failed authorization procedure. example.com (http-01): urn:ietf:params:acme:error:unauthorized
```

**해결 방법**:
```bash
# 1. 웹 루트 디렉토리 확인
ls -la /var/www/html/.well-known/acme-challenge/

# 2. 권한 설정
sudo chmod 755 /var/www/html/.well-known/acme-challenge/

# 3. Nginx 설정에서 .well-known 경로 확인
grep -n "well-known" frontend/nginx-ssl.conf

# 4. 수동으로 챌린지 테스트
echo "test" > /var/www/html/.well-known/acme-challenge/test.txt
curl http://your-domain.com/.well-known/acme-challenge/test.txt
```

### 4. "인증서 갱신 실패"

**증상**:
```
Attempting to renew cert (example.com) from /etc/letsencrypt/renewal/example.com.conf produced an unexpected error
```

**해결 방법**:
```bash
# 1. Dry Run으로 테스트
sudo certbot renew --dry-run

# 2. 상세 로그 확인
sudo tail -f /var/log/letsencrypt/letsencrypt.log

# 3. 수동 갱신 시도
sudo certbot renew --force-renewal

# 4. Certbot 재설치
sudo apt-get remove certbot
sudo apt-get install certbot
```

## 브라우저 관련 문제

### 1. "NET::ERR_CERT_AUTHORITY_INVALID"

**증상**: 브라우저에서 인증서 오류 표시

**원인**: 자체 서명 인증서 사용 (개발 환경)

**해결 방법**:
```
1. Chrome/Edge: "고급" → "localhost(으)로 이동(안전하지 않음)" 클릭
2. Firefox: "고급" → "위험을 감수하고 계속" 클릭
3. 또는 브라우저에 인증서 추가:
   - Chrome: 설정 → 개인정보 및 보안 → 보안 → 인증서 관리
   - 인증서 파일(cert.pem)을 "신뢰할 수 있는 루트 인증 기관"에 추가
```

### 2. "ERR_SSL_PROTOCOL_ERROR"

**증상**: SSL 프로토콜 오류

**해결 방법**:
```bash
# 1. TLS 버전 확인
openssl s_client -connect localhost:443 -tls1_2

# 2. Nginx SSL 설정 확인
grep "ssl_protocols" frontend/nginx-ssl.conf

# 3. 브라우저 캐시 클리어
# Chrome: Ctrl+Shift+Delete → "캐시된 이미지 및 파일" 삭제

# 4. SSL 세션 캐시 클리어
docker exec solebid-frontend nginx -s reload
```

### 3. "Mixed Content 경고"

**증상**: HTTPS 페이지에서 HTTP 리소스 로드 차단

**해결 방법**:
```javascript
// 1. 모든 리소스를 HTTPS로 변경
// 잘못된 예:
<script src="http://example.com/script.js"></script>

// 올바른 예:
<script src="https://example.com/script.js"></script>

// 2. 프로토콜 상대 URL 사용
<script src="//example.com/script.js"></script>

// 3. CSP 헤더 확인
// frontend/nginx-ssl.conf에서:
add_header Content-Security-Policy "upgrade-insecure-requests" always;
```

## OAuth2 HTTPS 문제

### 1. "OAuth2 리다이렉트 URI 불일치"

**증상**:
```
redirect_uri_mismatch: The redirect URI provided does not match
```

**해결 방법**:
```bash
# 1. 환경 변수 확인
cat .env | grep REDIRECT_URI

# 2. HTTPS URL로 업데이트
GOOGLE_REDIRECT_URI=https://your-domain.com/auth/callback/google
KAKAO_REDIRECT_URI=https://your-domain.com/auth/callback/kakao

# 3. OAuth2 제공자 콘솔에서 리다이렉트 URI 업데이트
# Google: https://console.cloud.google.com/
# Kakao: https://developers.kakao.com/

# 4. 서비스 재시작
docker-compose restart backend
```

### 2. "CORS 오류 (HTTPS 환경)"

**증상**:
```
Access to XMLHttpRequest has been blocked by CORS policy
```

**해결 방법**:
```java
// SecurityConfig.java에서 HTTPS origin 추가
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "https://localhost:3443",  // HTTPS 추가
        "https://your-domain.com"
    ));
    // ...
}
```

### 3. "쿠키가 전송되지 않습니다"

**증상**: HTTPS 환경에서 쿠키가 저장/전송되지 않음

**해결 방법**:
```properties
# application-prod.properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

## 성능 관련 문제

### 1. "SSL 핸드셰이크가 느립니다"

**증상**: HTTPS 연결이 HTTP보다 현저히 느림

**해결 방법**:
```nginx
# frontend/nginx-ssl.conf에 추가
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 10m;
ssl_session_tickets on;

# HTTP/2 활성화
listen 443 ssl http2;
```

### 2. "높은 CPU 사용률"

**증상**: SSL/TLS 암호화로 인한 높은 CPU 사용

**해결 방법**:
```nginx
# 1. 효율적인 암호화 스위트 사용
ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256';

# 2. SSL 세션 재사용
ssl_session_cache shared:SSL:50m;

# 3. Worker 프로세스 수 조정
worker_processes auto;
```

### 3. "메모리 사용량 증가"

**증상**: SSL 세션 캐시로 인한 메모리 사용 증가

**해결 방법**:
```nginx
# SSL 세션 캐시 크기 조정
ssl_session_cache shared:SSL:10m;  # 10MB = 약 40,000 세션

# 타임아웃 단축
ssl_session_timeout 5m;
```

## 로그 분석

### Nginx 오류 로그 확인

```bash
# 컨테이너 로그
docker logs solebid-frontend

# SSL 관련 오류만 필터링
docker logs solebid-frontend 2>&1 | grep -i ssl

# 실시간 로그 모니터링
docker logs -f solebid-frontend
```

### SSL 핸드셰이크 로그

```bash
# SSL 핸드셰이크 디버그
openssl s_client -connect localhost:443 -servername localhost -debug

# 암호화 스위트 확인
openssl s_client -connect localhost:443 -servername localhost | grep "Cipher"
```

### 인증서 체인 확인

```bash
# 인증서 체인 검증
openssl s_client -connect your-domain.com:443 -showcerts

# 인증서 발급자 확인
openssl x509 -in nginx/ssl/cert.pem -noout -issuer -subject
```

## 일반적인 체크리스트

SSL/HTTPS 문제 발생 시 다음 항목을 순서대로 확인하세요:

- [ ] 인증서 파일이 존재하는가? (`nginx/ssl/cert.pem`, `nginx/ssl/key.pem`)
- [ ] 인증서가 유효한가? (만료되지 않았는가?)
- [ ] 인증서와 개인키가 일치하는가?
- [ ] 파일 권한이 올바른가? (key: 600, cert: 644)
- [ ] Nginx 설정 구문이 올바른가?
- [ ] Docker 볼륨 마운트가 올바른가?
- [ ] 포트가 충돌하지 않는가? (80, 443)
- [ ] 방화벽이 포트를 차단하지 않는가?
- [ ] DNS 설정이 올바른가? (프로덕션)
- [ ] 환경 변수가 올바르게 설정되었는가?

## 추가 도움말

문제가 해결되지 않으면:

1. **검증 스크립트 실행**:
   ```bash
   ./scripts/validate-ssl-setup.sh --environment dev
   ```

2. **로그 확인**:
   ```bash
   docker logs solebid-frontend
   docker logs solebid-backend
   ```

3. **설정 파일 검증**:
   ```bash
   docker run --rm -v $(pwd)/frontend/nginx-ssl.conf:/etc/nginx/nginx.conf:ro nginx nginx -t
   ```

4. **커뮤니티 지원**:
   - GitHub Issues
   - Stack Overflow
   - Let's Encrypt Community Forum

---

**관련 문서**:
- [SSL/HTTPS 설정 가이드](ssl-https-setup-guide.md)
- [환경 변수 가이드](environment-variables-guide.md)
- [Docker 헬스체크 가이드](docker-healthcheck-guide.md)
