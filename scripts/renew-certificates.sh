#!/bin/bash

# ============================================================================
# SSL 인증서 갱신 스크립트
# ============================================================================
# 이 스크립트는 Let's Encrypt 인증서를 갱신하고 Nginx를 재로드합니다.
# 
# 사용법:
#   ./scripts/renew-certificates.sh [--dry-run]
#
# 옵션:
#   --dry-run: 실제 갱신 없이 테스트만 수행
#
# Cron 설정 예시:
#   0 0,12 * * * /path/to/renew-certificates.sh >> /var/log/cert-renewal.log 2>&1
# ============================================================================

set -e

# 색상 코드 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 설정
SSL_DIR="nginx/ssl"
DRY_RUN=false

# 인자 처리
if [ "$1" = "--dry-run" ]; then
    DRY_RUN=true
fi

# ============================================================================
# 함수: 배너 출력
# ============================================================================
print_banner() {
    echo -e "${BLUE}"
    echo "============================================"
    echo "  SSL 인증서 갱신 스크립트"
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
# 함수: 오류 메시지 출력
# ============================================================================
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ============================================================================
# 함수: Certbot 설치 확인
# ============================================================================
check_certbot() {
    print_info "Certbot 설치 확인 중..."
    
    if ! command -v certbot &> /dev/null; then
        print_error "Certbot이 설치되어 있지 않습니다"
        exit 1
    fi
    
    print_success "Certbot 발견"
}

# ============================================================================
# 함수: 현재 인증서 정보 출력
# ============================================================================
display_current_certificate() {
    print_info "현재 인증서 정보:"
    
    if [ -f "$SSL_DIR/cert.pem" ]; then
        local expiry=$(openssl x509 -in "$SSL_DIR/cert.pem" -noout -enddate | cut -d= -f2)
        local days_left=$(( ($(date -d "$expiry" +%s) - $(date +%s)) / 86400 ))
        
        echo "  만료일: $expiry"
        echo "  남은 기간: ${days_left}일"
        
        if [ $days_left -lt 30 ]; then
            print_warning "인증서가 30일 이내에 만료됩니다"
        fi
    else
        print_warning "인증서 파일을 찾을 수 없습니다: $SSL_DIR/cert.pem"
    fi
    
    echo ""
}

# ============================================================================
# 함수: 인증서 갱신
# ============================================================================
renew_certificates() {
    if [ "$DRY_RUN" = true ]; then
        print_info "Dry Run 모드: 실제 갱신 없이 테스트만 수행합니다"
        
        sudo certbot renew --dry-run
        
        if [ $? -eq 0 ]; then
            print_success "Dry Run 테스트 성공!"
            print_info "실제 갱신을 수행하려면 --dry-run 옵션 없이 실행하세요"
        else
            print_error "Dry Run 테스트 실패"
            exit 1
        fi
    else
        print_info "인증서 갱신 중..."
        
        sudo certbot renew --quiet
        
        if [ $? -eq 0 ]; then
            print_success "인증서 갱신 완료"
        else
            print_error "인증서 갱신 실패"
            echo "       Certbot 로그를 확인하세요: /var/log/letsencrypt/letsencrypt.log"
            exit 1
        fi
    fi
}

# ============================================================================
# 함수: 인증서 복사
# ============================================================================
copy_certificates() {
    if [ "$DRY_RUN" = true ]; then
        print_info "Dry Run 모드: 인증서 복사를 건너뜁니다"
        return 0
    fi
    
    print_info "갱신된 인증서를 nginx/ssl 디렉토리로 복사 중..."
    
    # 도메인 찾기 (첫 번째 인증서 사용)
    local domain=$(sudo ls /etc/letsencrypt/live/ | grep -v README | head -n 1)
    
    if [ -z "$domain" ]; then
        print_error "인증서 도메인을 찾을 수 없습니다"
        exit 1
    fi
    
    local cert_path="/etc/letsencrypt/live/$domain"
    
    # 인증서 복사
    sudo cp "$cert_path/fullchain.pem" "$SSL_DIR/cert.pem"
    sudo cp "$cert_path/privkey.pem" "$SSL_DIR/key.pem"
    
    # 권한 설정
    sudo chmod 644 "$SSL_DIR/cert.pem"
    sudo chmod 600 "$SSL_DIR/key.pem"
    
    # 소유권 변경
    sudo chown $USER:$USER "$SSL_DIR/cert.pem"
    sudo chown $USER:$USER "$SSL_DIR/key.pem"
    
    print_success "인증서 복사 완료"
}

# ============================================================================
# 함수: Nginx 재로드
# ============================================================================
reload_nginx() {
    if [ "$DRY_RUN" = true ]; then
        print_info "Dry Run 모드: Nginx 재로드를 건너뜁니다"
        return 0
    fi
    
    print_info "Nginx 재로드 중..."
    
    # Docker 컨테이너에서 Nginx 재로드
    if docker ps | grep -q "solebid-frontend"; then
        docker exec solebid-frontend nginx -s reload
        
        if [ $? -eq 0 ]; then
            print_success "Nginx 재로드 완료"
        else
            print_error "Nginx 재로드 실패"
            exit 1
        fi
    elif docker ps | grep -q "solebid-frontend-prod"; then
        docker exec solebid-frontend-prod nginx -s reload
        
        if [ $? -eq 0 ]; then
            print_success "Nginx 재로드 완료"
        else
            print_error "Nginx 재로드 실패"
            exit 1
        fi
    else
        print_warning "실행 중인 Nginx 컨테이너를 찾을 수 없습니다"
        print_info "Docker Compose를 재시작하세요: docker-compose restart frontend"
    fi
}

# ============================================================================
# 함수: 갱신 후 인증서 정보 출력
# ============================================================================
display_renewed_certificate() {
    if [ "$DRY_RUN" = true ]; then
        return 0
    fi
    
    print_info "갱신된 인증서 정보:"
    
    if [ -f "$SSL_DIR/cert.pem" ]; then
        openssl x509 -in "$SSL_DIR/cert.pem" -noout -subject -issuer -dates
    fi
    
    echo ""
}

# ============================================================================
# 함수: 완료 메시지
# ============================================================================
display_completion_message() {
    echo ""
    echo -e "${GREEN}"
    echo "============================================"
    if [ "$DRY_RUN" = true ]; then
        echo "  Dry Run 테스트 완료!"
    else
        echo "  인증서 갱신 완료!"
    fi
    echo "============================================"
    echo -e "${NC}"
    echo ""
    
    if [ "$DRY_RUN" = false ]; then
        echo "인증서가 성공적으로 갱신되었습니다."
        echo "Nginx가 새 인증서를 사용하도록 재로드되었습니다."
        echo ""
        echo "다음 갱신 예정일: 약 60일 후"
        echo ""
    fi
}

# ============================================================================
# 메인 실행
# ============================================================================
main() {
    print_banner
    
    # 타임스탬프 출력
    echo "실행 시간: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    
    # 1. Certbot 확인
    check_certbot
    
    # 2. 현재 인증서 정보 출력
    display_current_certificate
    
    # 3. 인증서 갱신
    renew_certificates
    
    # 4. 인증서 복사
    copy_certificates
    
    # 5. Nginx 재로드
    reload_nginx
    
    # 6. 갱신된 인증서 정보 출력
    display_renewed_certificate
    
    # 7. 완료 메시지
    display_completion_message
}

# 스크립트 실행
main
