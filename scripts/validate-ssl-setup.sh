#!/bin/bash

# ============================================================================
# 배포 전 SSL 설정 검증 스크립트
# ============================================================================
# 이 스크립트는 Docker 배포 전에 SSL 설정이 올바른지 종합적으로 검증합니다.
# 
# 사용법:
#   ./scripts/validate-ssl-setup.sh [--environment dev|prod]
#
# 검증 항목:
#   - SSL 인증서 파일 존재 및 유효성
#   - Nginx 설정 파일 구문 검사
#   - Docker Compose 설정 검증
#   - 환경 변수 설정 확인
#   - 포트 충돌 검사
#   - 파일 권한 확인
# ============================================================================

set -e

# 색상 코드 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 설정
ENVIRONMENT="dev"
SSL_DIR="nginx/ssl"
CERT_FILE="$SSL_DIR/cert.pem"
KEY_FILE="$SSL_DIR/key.pem"
NGINX_SSL_CONF="frontend/nginx-ssl.conf"
DOCKER_COMPOSE_FILE="docker-compose.yml"
DOCKER_COMPOSE_PROD_FILE="docker-compose.prod.yml"
ENV_FILE=".env"
ENV_PROD_FILE=".env.prod.example"

# 검증 결과 카운터
PASSED=0
FAILED=0
WARNINGS=0

# 인자 처리
while [[ $# -gt 0 ]]; do
    case $1 in
        --environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -e)
            ENVIRONMENT="$2"
            shift 2
            ;;
        *)
            echo "알 수 없는 옵션: $1"
            exit 1
            ;;
    esac
done

# 환경에 따른 설정 조정
if [ "$ENVIRONMENT" = "prod" ]; then
    DOCKER_COMPOSE_FILE="$DOCKER_COMPOSE_PROD_FILE"
    ENV_FILE="$ENV_PROD_FILE"
fi

# ============================================================================
# 함수: 배너 출력
# ============================================================================
print_banner() {
    echo -e "${BLUE}"
    echo "============================================"
    echo "  배포 전 SSL 설정 검증"
    echo "============================================"
    echo -e "${NC}"
    echo "환경: $ENVIRONMENT"
    echo "Docker Compose: $DOCKER_COMPOSE_FILE"
    echo "환경 변수 파일: $ENV_FILE"
    echo ""
}

# ============================================================================
# 함수: 테스트 통과 메시지
# ============================================================================
print_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED++))
}

# ============================================================================
# 함수: 테스트 실패 메시지
# ============================================================================
print_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED++))
}

# ============================================================================
# 함수: 경고 메시지
# ============================================================================
print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    ((WARNINGS++))
}

# ============================================================================
# 함수: 정보 메시지
# ============================================================================
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# ============================================================================
# 테스트 1: SSL 디렉토리 존재 확인
# ============================================================================
test_ssl_directory() {
    print_info "테스트 1: SSL 디렉토리 존재 확인"
    
    if [ -d "$SSL_DIR" ]; then
        print_pass "SSL 디렉토리가 존재합니다: $SSL_DIR"
    else
        print_fail "SSL 디렉토리가 존재하지 않습니다: $SSL_DIR"
        echo "       실행: mkdir -p $SSL_DIR"
        return 1
    fi
}

# ============================================================================
# 테스트 2: 인증서 파일 존재 확인
# ============================================================================
test_certificate_files() {
    print_info "테스트 2: 인증서 파일 존재 확인"
    
    local all_exist=true
    
    if [ -f "$CERT_FILE" ]; then
        print_pass "인증서 파일 존재: $CERT_FILE"
    else
        print_fail "인증서 파일이 없습니다: $CERT_FILE"
        echo "       실행: ./scripts/generate-ssl-cert.sh"
        all_exist=false
    fi
    
    if [ -f "$KEY_FILE" ]; then
        print_pass "개인키 파일 존재: $KEY_FILE"
    else
        print_fail "개인키 파일이 없습니다: $KEY_FILE"
        echo "       실행: ./scripts/generate-ssl-cert.sh"
        all_exist=false
    fi
    
    [ "$all_exist" = true ]
}

# ============================================================================
# 테스트 3: 인증서 유효성 확인
# ============================================================================
test_certificate_validity() {
    print_info "테스트 3: 인증서 유효성 확인"
    
    if [ ! -f "$CERT_FILE" ] || [ ! -f "$KEY_FILE" ]; then
        print_fail "인증서 파일이 없어 검증을 건너뜁니다"
        return
    fi
    
    # 인증서 형식 확인
    if openssl x509 -in "$CERT_FILE" -noout 2>/dev/null; then
        print_pass "인증서 형식이 유효합니다"
    else
        print_fail "인증서 형식이 올바르지 않습니다"
        return
    fi
    
    # 개인키 형식 확인
    if openssl rsa -in "$KEY_FILE" -check -noout 2>/dev/null; then
        print_pass "개인키 형식이 유효합니다"
    else
        print_fail "개인키 형식이 올바르지 않습니다"
        return
    fi
    
    # 키 강도 확인
    local key_bits=$(openssl rsa -in "$KEY_FILE" -text -noout 2>/dev/null | grep "Private-Key:" | grep -oE '[0-9]+')
    if [ "$key_bits" -ge 2048 ]; then
        print_pass "키 강도가 충분합니다: ${key_bits} bits"
    else
        print_fail "키 강도가 부족합니다: ${key_bits} bits (최소 2048 bits 필요)"
    fi
    
    # 만료일 확인
    local not_after=$(openssl x509 -in "$CERT_FILE" -noout -enddate | cut -d= -f2)
    local expiry_epoch=$(date -d "$not_after" +%s 2>/dev/null || date -j -f "%b %d %T %Y %Z" "$not_after" +%s 2>/dev/null)
    local current_epoch=$(date +%s)
    local days_until_expiry=$(( ($expiry_epoch - $current_epoch) / 86400 ))
    
    if [ $days_until_expiry -gt 0 ]; then
        if [ $days_until_expiry -lt 30 ]; then
            print_warning "인증서가 곧 만료됩니다: ${days_until_expiry}일 남음"
        else
            print_pass "인증서가 유효합니다: ${days_until_expiry}일 남음"
        fi
    else
        print_fail "인증서가 만료되었습니다"
    fi
}

# ============================================================================
# 테스트 4: 인증서와 키 일치 확인
# ============================================================================
test_cert_key_match() {
    print_info "테스트 4: 인증서와 개인키 일치 확인"
    
    if [ ! -f "$CERT_FILE" ] || [ ! -f "$KEY_FILE" ]; then
        print_fail "인증서 또는 개인키 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    local cert_modulus=$(openssl x509 -noout -modulus -in "$CERT_FILE" 2>/dev/null | openssl md5)
    local key_modulus=$(openssl rsa -noout -modulus -in "$KEY_FILE" 2>/dev/null | openssl md5)
    
    if [ "$cert_modulus" = "$key_modulus" ]; then
        print_pass "인증서와 개인키가 일치합니다"
    else
        print_fail "인증서와 개인키가 일치하지 않습니다"
        echo "       실행: ./scripts/generate-ssl-cert.sh"
        return 1
    fi
}

# ============================================================================
# 테스트 5: 파일 권한 확인
# ============================================================================
test_file_permissions() {
    print_info "테스트 5: 파일 권한 확인"
    
    if [ ! -f "$KEY_FILE" ]; then
        print_fail "개인키 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    local key_perms=$(stat -c "%a" "$KEY_FILE" 2>/dev/null || stat -f "%OLp" "$KEY_FILE" 2>/dev/null)
    
    if [ "$key_perms" = "600" ] || [ "$key_perms" = "400" ]; then
        print_pass "개인키 파일 권한이 안전합니다: $key_perms"
    else
        print_warning "개인키 파일 권한이 안전하지 않습니다: $key_perms"
        echo "       실행: chmod 600 $KEY_FILE"
    fi
}

# ============================================================================
# 테스트 6: Nginx SSL 설정 파일 확인
# ============================================================================
test_nginx_ssl_config() {
    print_info "테스트 6: Nginx SSL 설정 파일 확인"
    
    # nginx-ssl.conf 파일 존재 확인
    if [ -f "$NGINX_SSL_CONF" ]; then
        print_pass "Nginx SSL 설정 파일이 존재합니다: $NGINX_SSL_CONF"
    else
        print_fail "Nginx SSL 설정 파일이 존재하지 않습니다: $NGINX_SSL_CONF"
        return
    fi
    
    # SSL 설정 내용 확인
    if grep -q "listen 443 ssl" "$NGINX_SSL_CONF"; then
        print_pass "HTTPS 포트 443 설정이 있습니다"
    else
        print_fail "HTTPS 포트 443 설정이 없습니다"
    fi
    
    if grep -q "ssl_certificate" "$NGINX_SSL_CONF"; then
        print_pass "SSL 인증서 경로 설정이 있습니다"
    else
        print_fail "SSL 인증서 경로 설정이 없습니다"
    fi
    
    if grep -q "ssl_certificate_key" "$NGINX_SSL_CONF"; then
        print_pass "SSL 개인키 경로 설정이 있습니다"
    else
        print_fail "SSL 개인키 경로 설정이 없습니다"
    fi
    
    # HTTP 리다이렉트 확인
    if grep -q "return 301 https" "$NGINX_SSL_CONF"; then
        print_pass "HTTP에서 HTTPS로 리다이렉트 설정이 있습니다"
    else
        print_warning "HTTP에서 HTTPS로 리다이렉트 설정이 없습니다"
    fi
    
    # HSTS 헤더 확인
    if grep -q "Strict-Transport-Security" "$NGINX_SSL_CONF"; then
        print_pass "HSTS 보안 헤더 설정이 있습니다"
    else
        print_warning "HSTS 보안 헤더 설정이 없습니다"
    fi
}

# ============================================================================
# 테스트 7: Nginx 설정 구문 확인
# ============================================================================
test_nginx_syntax() {
    print_info "테스트 7: Nginx 설정 구문 확인"
    
    if [ ! -f "$NGINX_SSL_CONF" ]; then
        print_fail "Nginx 설정 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    # Docker를 사용하여 Nginx 설정 테스트
    if command -v docker &> /dev/null; then
        docker run --rm -v "$(pwd)/$NGINX_SSL_CONF:/etc/nginx/nginx.conf:ro" nginx:alpine nginx -t 2>&1 | grep -q "successful"
        
        if [ $? -eq 0 ]; then
            print_pass "Nginx 설정 구문이 올바릅니다"
        else
            print_fail "Nginx 설정 구문 오류"
            echo "       실행: docker run --rm -v \$(pwd)/$NGINX_SSL_CONF:/etc/nginx/nginx.conf:ro nginx:alpine nginx -t"
            return 1
        fi
    else
        print_warning "Docker가 설치되어 있지 않아 Nginx 구문 검사를 건너뜁니다"
    fi
}

# ============================================================================
# 테스트 8: Docker Compose SSL 설정 확인
# ============================================================================
test_docker_compose_config() {
    print_info "테스트 8: Docker Compose SSL 설정 확인"
    
    # Docker Compose 파일 존재 확인
    if [ -f "$DOCKER_COMPOSE_FILE" ]; then
        print_pass "Docker Compose 파일이 존재합니다: $DOCKER_COMPOSE_FILE"
    else
        print_fail "Docker Compose 파일이 존재하지 않습니다: $DOCKER_COMPOSE_FILE"
        return
    fi
    
    # HTTPS 포트 설정 확인
    if grep -q "443:443" "$DOCKER_COMPOSE_FILE" || grep -q "\${DOCKER_FRONTEND_HTTPS_PORT" "$DOCKER_COMPOSE_FILE"; then
        print_pass "HTTPS 포트 매핑 설정이 있습니다"
    else
        print_fail "HTTPS 포트 매핑 설정이 없습니다"
    fi
    
    # SSL 볼륨 마운트 확인
    if grep -q "nginx/ssl:/etc/nginx/ssl" "$DOCKER_COMPOSE_FILE"; then
        print_pass "SSL 인증서 볼륨 마운트 설정이 있습니다"
    else
        print_fail "SSL 인증서 볼륨 마운트 설정이 없습니다"
    fi
    
    # Docker Compose 구문 검사
    if docker-compose -f "$DOCKER_COMPOSE_FILE" config > /dev/null 2>&1; then
        print_pass "Docker Compose 설정 구문이 유효합니다"
    else
        print_fail "Docker Compose 설정 구문에 오류가 있습니다"
        echo "       실행: docker-compose -f $DOCKER_COMPOSE_FILE config"
    fi
}

# ============================================================================
# 테스트 9: 환경 변수 설정 확인
# ============================================================================
test_environment_variables() {
    print_info "테스트 9: 환경 변수 설정 확인"
    
    # 환경 변수 파일 존재 확인
    if [ -f "$ENV_FILE" ]; then
        print_pass "환경 변수 파일이 존재합니다: $ENV_FILE"
    else
        print_fail "환경 변수 파일이 존재하지 않습니다: $ENV_FILE"
        return
    fi
    
    # HTTPS 포트 설정 확인
    if grep -q "DOCKER_FRONTEND_HTTPS_PORT" "$ENV_FILE"; then
        print_pass "HTTPS 포트 환경 변수가 설정되어 있습니다"
    else
        print_warning "HTTPS 포트 환경 변수가 설정되어 있지 않습니다"
    fi
    
    # SSL 활성화 설정 확인
    if grep -q "NGINX_SSL_ENABLED" "$ENV_FILE"; then
        print_pass "SSL 활성화 환경 변수가 설정되어 있습니다"
        
        # SSL 활성화 값 확인
        local ssl_enabled=$(grep "NGINX_SSL_ENABLED" "$ENV_FILE" | cut -d= -f2)
        if [ "$ssl_enabled" = "true" ]; then
            print_info "SSL이 활성화되어 있습니다"
        else
            print_info "SSL이 비활성화되어 있습니다 (개발 환경)"
        fi
    else
        print_warning "SSL 활성화 환경 변수가 설정되어 있지 않습니다"
    fi
}

# ============================================================================
# 테스트 10: 필수 도구 확인
# ============================================================================
test_required_tools() {
    print_info "테스트 10: 필수 도구 설치 확인"
    
    # OpenSSL 확인
    if command -v openssl &> /dev/null; then
        print_pass "OpenSSL이 설치되어 있습니다"
    else
        print_fail "OpenSSL이 설치되어 있지 않습니다"
    fi
    
    # Docker 확인
    if command -v docker &> /dev/null; then
        print_pass "Docker가 설치되어 있습니다"
    else
        print_fail "Docker가 설치되어 있지 않습니다"
    fi
    
    # Docker Compose 확인
    if command -v docker-compose &> /dev/null; then
        print_pass "Docker Compose가 설치되어 있습니다"
    else
        print_fail "Docker Compose가 설치되어 있지 않습니다"
    fi
}

# ============================================================================
# 테스트 11: 포트 충돌 확인
# ============================================================================
test_port_conflicts() {
    print_info "테스트 11: 포트 충돌 확인"
    
    # 포트 80 확인
    if netstat -tuln 2>/dev/null | grep -q ":80 " || ss -tuln 2>/dev/null | grep -q ":80 "; then
        print_warning "포트 80이 이미 사용 중입니다"
    else
        print_pass "포트 80이 사용 가능합니다"
    fi
    
    # 포트 443 확인
    if netstat -tuln 2>/dev/null | grep -q ":443 " || ss -tuln 2>/dev/null | grep -q ":443 "; then
        print_warning "포트 443이 이미 사용 중입니다"
    else
        print_pass "포트 443이 사용 가능합니다"
    fi
    
    # 개발 환경 HTTPS 포트 확인 (3443)
    if [ "$ENVIRONMENT" = "dev" ]; then
        if netstat -tuln 2>/dev/null | grep -q ":3443 " || ss -tuln 2>/dev/null | grep -q ":3443 "; then
            print_warning "개발 환경 HTTPS 포트 3443이 이미 사용 중입니다"
        else
            print_pass "개발 환경 HTTPS 포트 3443이 사용 가능합니다"
        fi
    fi
}

# ============================================================================
# 테스트 12: 디스크 공간 확인
# ============================================================================
test_disk_space() {
    print_info "테스트 12: 디스크 공간 확인"
    
    local available_space=$(df . | tail -1 | awk '{print $4}')
    local available_mb=$((available_space / 1024))
    
    if [ $available_mb -gt 1000 ]; then
        print_pass "충분한 디스크 공간이 있습니다: ${available_mb}MB"
    elif [ $available_mb -gt 500 ]; then
        print_warning "디스크 공간이 부족할 수 있습니다: ${available_mb}MB"
    else
        print_fail "디스크 공간이 부족합니다: ${available_mb}MB"
    fi
}

# ============================================================================
# 테스트 13: 네트워크 연결 확인 (프로덕션)
# ============================================================================
test_network_connectivity() {
    if [ "$ENVIRONMENT" != "prod" ]; then
        return
    fi
    
    print_info "테스트 13: 네트워크 연결 확인 (프로덕션)"
    
    # Let's Encrypt 서버 연결 확인
    if curl -s --connect-timeout 5 https://acme-v02.api.letsencrypt.org/directory > /dev/null; then
        print_pass "Let's Encrypt 서버에 연결할 수 있습니다"
    else
        print_warning "Let's Encrypt 서버에 연결할 수 없습니다"
    fi
}

# ============================================================================
# 검증 결과 요약
# ============================================================================
display_summary() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  검증 결과 요약${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
    echo -e "${GREEN}통과:${NC} $PASSED"
    echo -e "${RED}실패:${NC} $FAILED"
    echo -e "${YELLOW}경고:${NC} $WARNINGS"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✓ SSL 설정 검증 완료!${NC}"
        echo ""
        echo "배포 준비가 완료되었습니다."
        echo ""
        echo "다음 단계:"
        if [ "$ENVIRONMENT" = "dev" ]; then
            echo "  docker-compose up -d"
        else
            echo "  docker-compose -f docker-compose.prod.yml up -d"
        fi
        echo ""
        return 0
    else
        echo -e "${RED}✗ SSL 설정 검증 실패${NC}"
        echo ""
        echo "문제를 해결한 후 다시 시도해주세요."
        echo ""
        return 1
    fi
}

# ============================================================================
# 메인 실행
# ============================================================================
main() {
    print_banner
    
    # 모든 테스트 실행
    test_required_tools
    test_ssl_directory || true
    test_certificate_files || true
    test_certificate_validity || true
    test_cert_key_match || true
    test_file_permissions || true
    test_nginx_ssl_config || true
    test_nginx_syntax || true
    test_docker_compose_config || true
    test_environment_variables || true
    test_port_conflicts || true
    test_disk_space || true
    test_network_connectivity || true
    
    # 결과 요약
    display_summary
}

# 스크립트 실행
main
exit $?
