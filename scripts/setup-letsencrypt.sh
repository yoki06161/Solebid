#!/bin/bash

# ============================================================================
# Let's Encrypt SSL 인증서 설정 스크립트 (프로덕션 환경용)
# ============================================================================
# 이 스크립트는 Let's Encrypt를 사용하여 무료 SSL 인증서를 발급받습니다.
# 
# 사용법:
#   ./scripts/setup-letsencrypt.sh [도메인] [이메일]
#
# 예시:
#   ./scripts/setup-letsencrypt.sh example.com admin@example.com
#
# 요구사항:
#   - 도메인이 서버 IP를 가리키도록 DNS 설정 완료
#   - 포트 80이 방화벽에서 열려있어야 함
#   - Certbot 설치 필요
# ============================================================================

set -e

# 색상 코드 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 기본값 설정
DOMAIN="${1}"
EMAIL="${2}"
SSL_DIR="nginx/ssl"
WEBROOT_DIR="nginx/webroot"

# ============================================================================
# 함수: 배너 출력
# ============================================================================
print_banner() {
    echo -e "${BLUE}"
    echo "============================================"
    echo "  Let's Encrypt SSL 인증서 설정"
    echo "============================================"
    echo -e "${NC}"
}

# ============================================================================
# 함수: 정보 메시지 출력
# ============================================================================
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# ============================================================================
# 함수: 성공 메시지 출력
# ============================================================================
print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# ============================================================================
# 함수: 경고 메시지 출력
# ============================================================================
print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# ============================================================================
# 함수: 오류 메시지 출력 및 종료
# ============================================================================
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# ============================================================================
# 함수: 사용법 출력
# ============================================================================
print_usage() {
    echo "사용법: $0 <도메인> <이메일>"
    echo ""
    echo "예시:"
    echo "  $0 example.com admin@example.com"
    echo ""
    echo "설명:"
    echo "  도메인: SSL 인증서를 발급받을 도메인 (예: example.com)"
    echo "  이메일: Let's Encrypt 알림을 받을 이메일 주소"
    echo ""
}

# ============================================================================
# 함수: 인자 검증
# ============================================================================
validate_arguments() {
    if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
        print_error "도메인과 이메일을 모두 입력해주세요"
        print_usage
        exit 1
    fi
    
    # 이메일 형식 간단 검증
    if ! echo "$EMAIL" | grep -qE '^[^@]+@[^@]+\.[^@]+$'; then
        print_error "올바른 이메일 형식이 아닙니다: $EMAIL"
        exit 1
    fi
    
    print_success "도메인: $DOMAIN"
    print_success "이메일: $EMAIL"
}

# ============================================================================
# 함수: Certbot 설치 확인
# ============================================================================
check_certbot() {
    print_info "Certbot 설치 확인 중..."
    
    if command -v certbot &> /dev/null; then
        local version=$(certbot --version 2>&1 | head -n 1)
        print_success "Certbot이 설치되어 있습니다: $version"
        return 0
    fi
    
    print_warning "Certbot이 설치되어 있지 않습니다"
    print_info "Certbot 설치를 시작합니다..."
    
    # OS 감지 및 설치
    if [ -f /etc/debian_version ]; then
        # Debian/Ubuntu
        sudo apt-get update
        sudo apt-get install -y certbot
    elif [ -f /etc/redhat-release ]; then
        # CentOS/RHEL
        sudo yum install -y certbot
    else
        print_error "지원하지 않는 OS입니다. Certbot을 수동으로 설치해주세요: https://certbot.eff.org/"
    fi
    
    if command -v certbot &> /dev/null; then
        print_success "Certbot 설치 완료"
    else
        print_error "Certbot 설치 실패"
    fi
}

# ============================================================================
# 함수: DNS 확인
# ============================================================================
check_dns() {
    print_info "DNS 설정 확인 중: $DOMAIN"
    
    if host "$DOMAIN" > /dev/null 2>&1; then
        local ip=$(host "$DOMAIN" | grep "has address" | awk '{print $4}' | head -n 1)
        print_success "DNS 설정 확인됨: $DOMAIN -> $ip"
    else
        print_error "도메인 $DOMAIN의 DNS 설정을 확인할 수 없습니다"
        echo "       도메인이 이 서버의 IP를 가리키도록 DNS를 설정해주세요"
        exit 1
    fi
}

# ============================================================================
# 함수: 포트 80 접근성 확인
# ============================================================================
check_port_80() {
    print_info "포트 80 접근성 확인 중..."
    
    # 로컬에서 포트 80이 열려있는지 확인
    if netstat -tuln 2>/dev/null | grep -q ":80 " || ss -tuln 2>/dev/null | grep -q ":80 "; then
        print_success "포트 80이 열려있습니다"
    else
        print_warning "포트 80이 열려있지 않습니다"
        echo "       Let's Encrypt HTTP-01 챌린지를 위해 포트 80이 필요합니다"
    fi
}

# ============================================================================
# 함수: Webroot 디렉토리 생성
# ============================================================================
create_webroot() {
    print_info "Webroot 디렉토리 생성 중: $WEBROOT_DIR"
    
    mkdir -p "$WEBROOT_DIR"
    
    # 테스트 파일 생성
    echo "Let's Encrypt verification" > "$WEBROOT_DIR/test.html"
    
    print_success "Webroot 디렉토리 생성 완료"
}

# ============================================================================
# 함수: SSL 디렉토리 생성
# ============================================================================
create_ssl_directory() {
    print_info "SSL 디렉토리 생성 중: $SSL_DIR"
    
    if [ ! -d "$SSL_DIR" ]; then
        mkdir -p "$SSL_DIR"
        print_success "SSL 디렉토리 생성 완료"
    else
        print_info "SSL 디렉토리가 이미 존재합니다"
    fi
}

# ============================================================================
# 함수: Dry Run 테스트
# ============================================================================
dry_run_test() {
    print_info "Dry Run 테스트 실행 중..."
    print_info "실제 인증서를 발급하지 않고 테스트만 수행합니다"
    
    sudo certbot certonly \
        --webroot \
        -w "$WEBROOT_DIR" \
        -d "$DOMAIN" \
        --email "$EMAIL" \
        --agree-tos \
        --non-interactive \
        --dry-run
    
    if [ $? -eq 0 ]; then
        print_success "Dry Run 테스트 성공!"
        print_info "실제 인증서 발급을 진행합니다..."
    else
        print_error "Dry Run 테스트 실패"
        echo "       문제를 해결한 후 다시 시도해주세요"
        exit 1
    fi
}

# ============================================================================
# 함수: 인증서 발급
# ============================================================================
obtain_certificate() {
    print_info "Let's Encrypt 인증서 발급 중..."
    print_info "도메인: $DOMAIN"
    print_info "이메일: $EMAIL"
    
    sudo certbot certonly \
        --webroot \
        -w "$WEBROOT_DIR" \
        -d "$DOMAIN" \
        --email "$EMAIL" \
        --agree-tos \
        --non-interactive
    
    if [ $? -eq 0 ]; then
        print_success "인증서 발급 완료!"
    else
        print_error "인증서 발급 실패"
        echo "       Certbot 로그를 확인하세요: /var/log/letsencrypt/letsencrypt.log"
        exit 1
    fi
}

# ============================================================================
# 함수: 인증서 복사
# ============================================================================
copy_certificates() {
    print_info "인증서를 nginx/ssl 디렉토리로 복사 중..."
    
    local cert_path="/etc/letsencrypt/live/$DOMAIN"
    
    if [ ! -d "$cert_path" ]; then
        print_error "인증서 디렉토리를 찾을 수 없습니다: $cert_path"
        exit 1
    fi
    
    # 인증서 복사
    sudo cp "$cert_path/fullchain.pem" "$SSL_DIR/cert.pem"
    sudo cp "$cert_path/privkey.pem" "$SSL_DIR/key.pem"
    
    # 권한 설정
    sudo chmod 644 "$SSL_DIR/cert.pem"
    sudo chmod 600 "$SSL_DIR/key.pem"
    
    # 소유권 변경 (현재 사용자)
    sudo chown $USER:$USER "$SSL_DIR/cert.pem"
    sudo chown $USER:$USER "$SSL_DIR/key.pem"
    
    print_success "인증서 복사 완료"
    print_info "인증서: $SSL_DIR/cert.pem"
    print_info "개인키: $SSL_DIR/key.pem"
}

# ============================================================================
# 함수: 인증서 정보 출력
# ============================================================================
display_certificate_info() {
    print_info "인증서 정보:"
    echo ""
    
    openssl x509 -in "$SSL_DIR/cert.pem" -noout -subject -issuer -dates
    
    echo ""
}

# ============================================================================
# 함수: 자동 갱신 설정
# ============================================================================
setup_auto_renewal() {
    print_info "자동 갱신 설정 중..."
    
    # Certbot 자동 갱신 테스트
    print_info "자동 갱신 테스트 실행 중..."
    sudo certbot renew --dry-run
    
    if [ $? -eq 0 ]; then
        print_success "자동 갱신 테스트 성공"
        print_info "Certbot은 자동으로 인증서를 갱신합니다"
        print_info "갱신 후 Nginx를 재로드하려면 renew-certificates.sh 스크립트를 사용하세요"
    else
        print_warning "자동 갱신 테스트 실패"
    fi
}

# ============================================================================
# 함수: 사용 안내 출력
# ============================================================================
display_usage_instructions() {
    echo ""
    echo -e "${GREEN}"
    echo "============================================"
    echo "  인증서 발급 완료!"
    echo "============================================"
    echo -e "${NC}"
    echo ""
    echo "다음 단계:"
    echo "  1. .env.prod 파일 업데이트:"
    echo "     NGINX_SSL_ENABLED=true"
    echo "     DOMAIN=$DOMAIN"
    echo ""
    echo "  2. Docker Compose로 프로덕션 서비스 시작:"
    echo "     docker-compose -f docker-compose.prod.yml up -d"
    echo ""
    echo "  3. 브라우저에서 접속:"
    echo "     https://$DOMAIN"
    echo ""
    echo "인증서 갱신:"
    echo "  - Let's Encrypt 인증서는 90일마다 갱신이 필요합니다"
    echo "  - Certbot이 자동으로 갱신을 시도합니다"
    echo "  - 수동 갱신: ./scripts/renew-certificates.sh"
    echo ""
    echo "인증서 위치:"
    echo "  - 원본: /etc/letsencrypt/live/$DOMAIN/"
    echo "  - 복사본: $SSL_DIR/"
    echo ""
}

# ============================================================================
# 메인 실행
# ============================================================================
main() {
    print_banner
    
    # 1. 인자 검증
    validate_arguments
    
    # 2. Certbot 설치 확인
    check_certbot
    
    # 3. DNS 확인
    check_dns
    
    # 4. 포트 80 확인
    check_port_80
    
    # 5. Webroot 디렉토리 생성
    create_webroot
    
    # 6. SSL 디렉토리 생성
    create_ssl_directory
    
    # 7. Dry Run 테스트
    dry_run_test
    
    # 8. 인증서 발급
    obtain_certificate
    
    # 9. 인증서 복사
    copy_certificates
    
    # 10. 인증서 정보 출력
    display_certificate_info
    
    # 11. 자동 갱신 설정
    setup_auto_renewal
    
    # 12. 사용 안내 출력
    display_usage_instructions
}

# 스크립트 실행
main
