#!/bin/bash

# ============================================================================
# SSL 자체 서명 인증서 생성 스크립트 (개발 환경용)
# ============================================================================
# 이 스크립트는 로컬 개발 환경에서 HTTPS 테스트를 위한 자체 서명 인증서를 생성합니다.
# 
# 사용법:
#   ./scripts/generate-ssl-cert.sh [도메인] [유효기간(일)]
#
# 예시:
#   ./scripts/generate-ssl-cert.sh localhost 365
#   ./scripts/generate-ssl-cert.sh example.com 730
#
# 생성되는 파일:
#   - nginx/ssl/cert.pem: SSL 인증서 (공개키)
#   - nginx/ssl/key.pem: 개인키 (비밀키)
# ============================================================================

set -e  # 오류 발생 시 스크립트 중단

# 색상 코드 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본값 설정
DEFAULT_DOMAIN="localhost"
DEFAULT_DAYS=365
DOMAIN="${1:-$DEFAULT_DOMAIN}"
DAYS="${2:-$DEFAULT_DAYS}"

# SSL 디렉토리 경로
SSL_DIR="nginx/ssl"
CERT_FILE="$SSL_DIR/cert.pem"
KEY_FILE="$SSL_DIR/key.pem"
CSR_FILE="$SSL_DIR/cert.csr"

# ============================================================================
# 함수: 배너 출력
# ============================================================================
print_banner() {
    echo -e "${BLUE}"
    echo "============================================"
    echo "  SSL 자체 서명 인증서 생성 스크립트"
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
# 함수: OpenSSL 설치 확인
# ============================================================================
check_openssl() {
    print_info "OpenSSL 설치 확인 중..."
    
    if ! command -v openssl &> /dev/null; then
        print_error "OpenSSL이 설치되어 있지 않습니다. 먼저 OpenSSL을 설치해주세요."
    fi
    
    local openssl_version=$(openssl version)
    print_success "OpenSSL 발견: $openssl_version"
}

# ============================================================================
# 함수: SSL 디렉토리 생성
# ============================================================================
create_ssl_directory() {
    print_info "SSL 디렉토리 생성 중: $SSL_DIR"
    
    if [ ! -d "$SSL_DIR" ]; then
        mkdir -p "$SSL_DIR" || print_error "SSL 디렉토리 생성 실패: $SSL_DIR"
        print_success "SSL 디렉토리 생성 완료"
    else
        print_info "SSL 디렉토리가 이미 존재합니다"
    fi
}

# ============================================================================
# 함수: 기존 인증서 백업
# ============================================================================
backup_existing_certificates() {
    if [ -f "$CERT_FILE" ] || [ -f "$KEY_FILE" ]; then
        print_warning "기존 인증서 파일이 발견되었습니다"
        
        local backup_dir="$SSL_DIR/backup_$(date +%Y%m%d_%H%M%S)"
        mkdir -p "$backup_dir"
        
        if [ -f "$CERT_FILE" ]; then
            mv "$CERT_FILE" "$backup_dir/" && print_info "기존 인증서를 백업했습니다: $backup_dir/cert.pem"
        fi
        
        if [ -f "$KEY_FILE" ]; then
            mv "$KEY_FILE" "$backup_dir/" && print_info "기존 개인키를 백업했습니다: $backup_dir/key.pem"
        fi
        
        if [ -f "$CSR_FILE" ]; then
            mv "$CSR_FILE" "$backup_dir/" 2>/dev/null || true
        fi
    fi
}

# ============================================================================
# 함수: SSL 인증서 생성
# ============================================================================
generate_certificate() {
    print_info "SSL 인증서 생성 중..."
    print_info "도메인: $DOMAIN"
    print_info "유효기간: $DAYS일"
    print_info "키 강도: 2048-bit RSA"
    
    # Subject Alternative Name (SAN) 설정 파일 생성
    local san_config="/tmp/san_$$.cnf"
    cat > "$san_config" << EOF
[req]
default_bits = 2048
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = v3_req

[dn]
C=KR
ST=Seoul
L=Seoul
O=Solebid Development
OU=Development Team
CN=$DOMAIN

[v3_req]
subjectAltName = @alt_names

[alt_names]
DNS.1 = $DOMAIN
DNS.2 = localhost
DNS.3 = *.localhost
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

    # 개인키 및 자체 서명 인증서 생성
    openssl req -x509 -nodes \
        -days "$DAYS" \
        -newkey rsa:2048 \
        -keyout "$KEY_FILE" \
        -out "$CERT_FILE" \
        -config "$san_config" \
        -extensions v3_req \
        2>&1 | grep -v "writing new private key" || true
    
    # 임시 설정 파일 삭제
    rm -f "$san_config"
    
    if [ ! -f "$CERT_FILE" ] || [ ! -f "$KEY_FILE" ]; then
        print_error "인증서 생성 실패"
    fi
    
    print_success "SSL 인증서 생성 완료"
}

# ============================================================================
# 함수: 파일 권한 설정
# ============================================================================
set_file_permissions() {
    print_info "파일 권한 설정 중..."
    
    # 개인키: 소유자만 읽기/쓰기 (600)
    chmod 600 "$KEY_FILE" || print_error "개인키 권한 설정 실패"
    print_success "개인키 권한 설정 완료: 600 (소유자만 읽기/쓰기)"
    
    # 인증서: 소유자 읽기/쓰기, 그룹/기타 읽기 (644)
    chmod 644 "$CERT_FILE" || print_error "인증서 권한 설정 실패"
    print_success "인증서 권한 설정 완료: 644 (모두 읽기 가능)"
}

# ============================================================================
# 함수: 인증서 정보 출력
# ============================================================================
display_certificate_info() {
    print_info "생성된 인증서 정보:"
    echo ""
    
    # 인증서 기본 정보
    echo -e "${BLUE}인증서 파일:${NC} $CERT_FILE"
    echo -e "${BLUE}개인키 파일:${NC} $KEY_FILE"
    echo ""
    
    # 인증서 상세 정보
    echo -e "${BLUE}인증서 상세 정보:${NC}"
    openssl x509 -in "$CERT_FILE" -noout -subject -issuer -dates -fingerprint
    echo ""
    
    # 키 강도 확인
    local key_bits=$(openssl rsa -in "$KEY_FILE" -text -noout 2>/dev/null | grep "Private-Key:" | grep -oE '[0-9]+')
    echo -e "${BLUE}키 강도:${NC} $key_bits bits"
    
    # Subject Alternative Names 출력
    echo ""
    echo -e "${BLUE}Subject Alternative Names (SAN):${NC}"
    openssl x509 -in "$CERT_FILE" -noout -text | grep -A 4 "Subject Alternative Name" || echo "  없음"
    
    echo ""
}

# ============================================================================
# 함수: 사용 안내 출력
# ============================================================================
display_usage_instructions() {
    echo -e "${GREEN}"
    echo "============================================"
    echo "  인증서 생성 완료!"
    echo "============================================"
    echo -e "${NC}"
    echo ""
    echo "다음 단계:"
    echo "  1. Docker Compose로 서비스 시작:"
    echo "     docker-compose up -d"
    echo ""
    echo "  2. 브라우저에서 접속:"
    echo "     https://localhost:3443"
    echo ""
    echo "  3. 브라우저 보안 경고:"
    echo "     - 자체 서명 인증서이므로 '안전하지 않음' 경고가 표시됩니다"
    echo "     - '고급' → '계속 진행' 클릭하여 접속하세요"
    echo ""
    echo "참고:"
    echo "  - 이 인증서는 개발 환경 전용입니다"
    echo "  - 프로덕션 환경에서는 Let's Encrypt를 사용하세요"
    echo "  - 인증서 유효기간: $DAYS일"
    echo ""
}

# ============================================================================
# 메인 실행
# ============================================================================
main() {
    print_banner
    
    # 1. OpenSSL 확인
    check_openssl
    
    # 2. SSL 디렉토리 생성
    create_ssl_directory
    
    # 3. 기존 인증서 백업
    backup_existing_certificates
    
    # 4. 인증서 생성
    generate_certificate
    
    # 5. 파일 권한 설정
    set_file_permissions
    
    # 6. 인증서 정보 출력
    display_certificate_info
    
    # 7. 사용 안내 출력
    display_usage_instructions
}

# 스크립트 실행
main
