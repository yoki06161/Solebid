# SSL/HTTPS 설정 가이드

이 문서는 Solebid 프로젝트에서 SSL/HTTPS를 설정하는 방법을 설명합니다.

## 목차

1. [개요](#개요)
2. [개발 환경 설정](#개발-환경-설정)
3. [프로덕션 환경 설정](#프로덕션-환경-설정)
4. [인증서 관리](#인증서-관리)
5. [문제 해결](#문제-해결)
6. [보안 고려사항](#보안-고려사항)

## 개요

### HTTPS의 필요성

- **데이터 암호화**: 클라이언트와 서버 간 통신 암호화
- **인증**: 서버의 신원 확인
- **무결성**: 데이터 변조 방지
- **SEO**: 검색 엔진 최적화
- **OAuth2 요구사항**: 대부분의 OAuth2 제공자가 HTTPS 필수

### 지원하는 환경

- **개발 환경**: 자체 서명 인증서 (Self-signed Certificate)
- **프로덕션 환경**: Let's Encrypt 무료 인증서

### 주요 기능

- HTTP → HTTPS 자동 리다이렉트
- TLS 1.2, 1.3 지원
- 강력한 암호화 스위트
- HSTS (HTTP Strict Transport Security)
- 보안 헤더 설정
- 자동 인증서 갱신

## 개발 환경 설정

### 1. 자체 서명 인증서 생성

#### Linux/Mac

```bash
# 인증서 생성
./scripts/generate-ssl-cert.sh

# 또는 사용자 정의 도메인으로 생성
./scripts/generate-ssl-cert.sh localhost 365
```

#### Windows

```cmd
# 인증서 생성
scripts\generate-ssl-cert.bat

# 또는 사용자 정의 도메인으로 생성
scripts\generate-ssl-cert.bat localhost 365
```

### 2. 환경 변수 설정

`.env` 파일에서 HTTPS를 활성화합니다:

```bash
# HTTPS 활성화
NGINX_SSL_ENABLED=true

# HTTPS 포트 (기본값: 3443)
DOCKER_FRONTEND_HTTPS_PORT=3443
```

### 3. Docker Compose 실행

```bash
# SSL 설정 검증
./scripts/validate-ssl-setup.sh --environment dev

# Docker 서비스 시작
docker-compose up -d
```

### 4. 브라우저에서 접속

- HTTP: http://localhost:3000 (자동으로 HTTPS로 리다이렉트)
- HTTPS: https://localhost:3443

**주의**: 자체 서명 인증서이므로 브라우저에서 보안 경고가 표시됩니다. "고급" → "계속 진행"을 클릭하여 접속하세요.

## 프로덕션 환경 설정

### 사전 요구사항

1. **도메인 설정**: 도메인이 서버 IP를 가리키도록 DNS 설정
2. **방화벽 설정**: 포트 80, 443 열기
3. **이메일 주소**: Let's Encrypt 알림용

### 1. Let's Encrypt 인증서 발급

```bash
# Let's Encrypt 인증서 발급
./scripts/setup-letsencrypt.sh your-domain.com admin@your-domain.com
```

스크립트가 자동으로 수행하는 작업:
- Certbot 설치 (필요시)
- DNS 설정 확인
- 포트 80 접근성 확인
- Dry Run 테스트
- 인증서 발급
- 인증서 복사 및 권한 설정

### 2. 환경 변수 설정

`.env.prod` 파일을 생성하고 설정:

```bash
# 프로덕션 설정 복사
cp .env.prod.example .env.prod

# 설정 편집
vim .env.prod
```

주요 설정:

```bash
# HTTPS 활성화 (프로덕션에서는 항상 true)
NGINX_SSL_ENABLED=true

# 도메인 설정
DOMAIN=your-domain.com
EMAIL=admin@your-domain.com

# OAuth2 리다이렉트 URI (HTTPS)
GOOGLE_REDIRECT_URI=https://your-domain.com/auth/callback/google
KAKAO_REDIRECT_URI=https://your-domain.com/auth/callback/kakao
```

### 3. 프로덕션 배포

```bash
# SSL 설정 검증
./scripts/validate-ssl-setup.sh --environment prod

# 프로덕션 서비스 시작
docker-compose -f docker-compose.prod.yml up -d
```

### 4. 인증서 자동 갱신 설정

```bash
# Cron job 설정
sudo crontab -e

# 다음 라인 추가 (경로를 실제 프로젝트 경로로 수정)
0 0,12 * * * cd /path/to/solebid && /path/to/solebid/scripts/renew-certificates.sh >> /var/log/cert-renewal.log 2>&1
```

또는 제공된 템플릿 사용:

```bash
# Cron 설정 템플릿 확인
cat scripts/cert-renewal-cron.txt

# 템플릿을 참고하여 cron job 설정
```

## 인증서 관리

### 인증서 상태 확인

```bash
# 인증서 검증
./scripts/validate-ssl.sh

# 전체 SSL 설정 검증
./scripts/validate-ssl-setup.sh
```

### 인증서 갱신 (수동)

```bash
# Dry Run 테스트
./scripts/renew-certificates.sh --dry-run

# 실제 갱신
./scripts/renew-certificates.sh
```

### 인증서 재생성 (개발 환경)

```bash
# 새 인증서 생성
./scripts/generate-ssl-cert.sh localhost 365

# Docker 서비스 재시작
docker-compose restart frontend
```

### 인증서 정보 확인

```bash
# 인증서 상세 정보
openssl x509 -in nginx/ssl/cert.pem -noout -text

# 만료일 확인
openssl x509 -in nginx/ssl/cert.pem -noout -enddate

# 키 강도 확인
openssl rsa -in nginx/ssl/key.pem -text -noout | grep "Private-Key:"
```

## 설정 파일 구조

### 주요 파일

```
solebid/
├── nginx/ssl/                    # SSL 인증서 저장소
│   ├── cert.pem                  # SSL 인증서
│   └── key.pem                   # 개인키
├── frontend/
│   ├── nginx.conf                # 기본 Nginx 설정 (HTTP)
│   └── nginx-ssl.conf            # SSL 지원 Nginx 설정 (HTTPS)
├── scripts/
│   ├── generate-ssl-cert.sh      # 자체 서명 인증서 생성
│   ├── setup-letsencrypt.sh      # Let's Encrypt 인증서 발급
│   ├── renew-certificates.sh     # 인증서 갱신
│   ├── validate-ssl.sh           # 인증서 검증
│   └── validate-ssl-setup.sh     # 전체 SSL 설정 검증
├── docker-compose.yml            # 개발 환경 Docker 설정
├── docker-compose.prod.yml       # 프로덕션 환경 Docker 설정
├── .env                          # 개발 환경 변수
└── .env.prod                     # 프로덕션 환경 변수
```

### Nginx 설정 전환

HTTPS를 사용하려면 Nginx 설정을 SSL 버전으로 전환해야 합니다:

#### 방법 1: Docker Compose에서 설정 파일 마운트

`docker-compose.yml`에서 주석을 해제:

```yaml
volumes:
  # SSL 지원 Nginx 설정 파일 마운트
  - ./frontend/nginx-ssl.conf:/etc/nginx/nginx.conf:ro
```

#### 방법 2: 환경 변수로 제어

환경 변수 `NGINX_SSL_ENABLED=true`로 설정하면 자동으로 SSL 설정이 적용됩니다.

### 환경 변수 설명

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `NGINX_SSL_ENABLED` | `false` | HTTPS 활성화 여부 |
| `DOCKER_FRONTEND_HTTPS_PORT` | `3443` | HTTPS 포트 (개발 환경) |
| `SSL_CERT_PATH` | `/etc/nginx/ssl/cert.pem` | 인증서 파일 경로 |
| `SSL_KEY_PATH` | `/etc/nginx/ssl/key.pem` | 개인키 파일 경로 |
| `DOMAIN` | - | 도메인 (프로덕션) |
| `EMAIL` | - | 이메일 (Let's Encrypt) |

## 보안 설정

### TLS 설정

- **지원 프로토콜**: TLS 1.2, TLS 1.3
- **비활성화**: SSLv2, SSLv3, TLS 1.0, TLS 1.1
- **암호화 스위트**: ECDHE, AES-GCM, CHACHA20-POLY1305
- **Perfect Forward Secrecy**: 지원

### 보안 헤더

자동으로 설정되는 보안 헤더:

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'; ...
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### 쿠키 보안

HTTPS 환경에서 자동으로 설정:

- `Secure`: HTTPS에서만 쿠키 전송
- `HttpOnly`: JavaScript에서 접근 불가
- `SameSite=Strict`: CSRF 공격 방지

## 성능 최적화

### SSL 세션 재사용

```nginx
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 10m;
ssl_session_tickets on;
```

### HTTP/2 지원

```nginx
listen 443 ssl http2;
```

### OCSP Stapling

```nginx
ssl_stapling on;
ssl_stapling_verify on;
```

### Gzip 압축

HTTPS 트래픽도 압축하여 대역폭 절약:

```nginx
gzip on;
gzip_types text/plain text/css application/json application/javascript;
```

## 모니터링

### 인증서 만료 모니터링

```bash
# 인증서 만료일 확인
openssl x509 -in nginx/ssl/cert.pem -noout -enddate

# 남은 일수 계산
echo "$(( ($(date -d "$(openssl x509 -in nginx/ssl/cert.pem -noout -enddate | cut -d= -f2)" +%s) - $(date +%s)) / 86400 )) days left"
```

### SSL 연결 테스트

```bash
# SSL 연결 테스트
openssl s_client -connect localhost:443 -servername localhost

# 암호화 스위트 확인
nmap --script ssl-enum-ciphers -p 443 localhost
```

### 로그 모니터링

```bash
# Nginx SSL 로그
docker logs solebid-frontend | grep ssl

# 인증서 갱신 로그
tail -f /var/log/cert-renewal.log
```

## 백업 및 복구

### 인증서 백업

```bash
# 인증서 백업
tar -czf ssl-backup-$(date +%Y%m%d).tar.gz nginx/ssl/

# Let's Encrypt 원본 백업 (프로덕션)
sudo tar -czf letsencrypt-backup-$(date +%Y%m%d).tar.gz /etc/letsencrypt/
```

### 인증서 복구

```bash
# 백업에서 복구
tar -xzf ssl-backup-20231201.tar.gz

# 권한 설정
chmod 600 nginx/ssl/key.pem
chmod 644 nginx/ssl/cert.pem

# Docker 서비스 재시작
docker-compose restart frontend
```

## 외부 도구 연동

### SSL Labs 테스트

프로덕션 환경에서 SSL 설정을 검증:

1. https://www.ssllabs.com/ssltest/ 방문
2. 도메인 입력하여 테스트
3. A+ 등급 목표

### Certbot 수동 설치

Ubuntu/Debian:
```bash
sudo apt-get update
sudo apt-get install certbot
```

CentOS/RHEL:
```bash
sudo yum install certbot
```

### 방화벽 설정

Ubuntu (ufw):
```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

CentOS (firewalld):
```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

## 다음 단계

1. [SSL 문제 해결 가이드](ssl-troubleshooting-guide.md) 참고
2. 정기적인 보안 업데이트 적용
3. 인증서 만료 알림 설정
4. 백업 및 복구 절차 테스트
5. 성능 모니터링 설정

---

**참고 자료**:
- [Let's Encrypt 공식 문서](https://letsencrypt.org/docs/)
- [Mozilla SSL Configuration Generator](https://ssl-config.mozilla.org/)
- [OWASP TLS Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Protection_Cheat_Sheet.html)
