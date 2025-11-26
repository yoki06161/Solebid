#!/bin/bash

# ============================================================================
# SSL 인증서 검증 스크립트
# ============================================================================
# 이 스크립트는 생성된 SSL 인증서의 유효성을 검증합니다.
# 
# 사용법:
#   ./scripts/validate-ssl.sh
#
# 검증 항목:
#   - 인증서 파일 존재 여부
#   - 개인키 파일 존재 여부
#   - 인증서와 개인키 일치 여부
#   - 인증서 만료일 확인
#   - 키 강도 확인 (최소 2048-bit)
#   - 파일 권한 확인
# ============================================================================

set -e

# 색상 코드 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# SSL 디렉토리 경로
SSL_DIR="nginx/ssl"
CERT_FILE="$SSL_DIR/cert.pem"
KEY_FILE="$SSL_DIR/key.pem"

# 검증 결과 카운터
PASSED=0
FAILED=0
WARNINGS=0

# ============================================================================
# 함수: 배너 출력
# ============================================================================
print_banner() {
    echo -e "${BLUE}"
    echo "============================================"
    echo "  SSL 인증서 검증 스크립트"
    echo "============================================"
    echo -e "${NC}"
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
# 테스트 1: OpenSSL 설치 확인
# ============================================================================
test_openssl_installed() {
    print_info "테스트 1: OpenSSL 설치 확인"
    
    if command -v openssl &> /dev/null; then
        local version=$(openssl version)
        print_pass "OpenSSL이 설치되어 있습니다: $version"
    else
        print_fail "OpenSSL이 설치되어 있지 않습니다"
        return 1
    fi
}

# ============================================================================
# 테스트 2: SSL 디렉토리 존재 확인
# ============================================================================
test_ssl_directory_exists() {
    print_info "테스트 2: SSL 디렉토리 존재 확인"
    
    if [ -d "$SSL_DIR" ]; then
        print_pass "SSL 디렉토리가 존재합니다: $SSL_DIR"
    else
        print_fail "SSL 디렉토리가 존재하지 않습니다: $SSL_DIR"
        return 1
    fi
}

# ============================================================================
# 테스트 3: 인증서 파일 존재 확인
# ============================================================================
test_certificate_file_exists() {
    print_info "테스트 3: 인증서 파일 존재 확인"
    
    if [ -f "$CERT_FILE" ]; then
        print_pass "인증서 파일이 존재합니다: $CERT_FILE"
    else
        print_fail "인증서 파일이 존재하지 않습니다: $CERT_FILE"
        echo "       실행: ./scripts/generate-ssl-cert.sh"
        return 1
    fi
}

# ============================================================================
# 테스트 4: 개인키 파일 존재 확인
# ============================================================================
test_key_file_exists() {
    print_info "테스트 4: 개인키 파일 존재 확인"
    
    if [ -f "$KEY_FILE" ]; then
        print_pass "개인키 파일이 존재합니다: $KEY_FILE"
    else
        print_fail "개인키 파일이 존재하지 않습니다: $KEY_FILE"
        echo "       실행: ./scripts/generate-ssl-cert.sh"
        return 1
    fi
}

# ============================================================================
# 테스트 5: 인증서 유효성 확인
# ============================================================================
test_certificate_validity() {
    print_info "테스트 5: 인증서 유효성 확인"
    
    if [ ! -f "$CERT_FILE" ]; then
        print_fail "인증서 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    # 인증서 파싱 가능 여부 확인
    if openssl x509 -in "$CERT_FILE" -noout 2>/dev/null; then
        print_pass "인증서가 유효한 형식입니다"
    else
        print_fail "인증서 형식이 올바르지 않습니다"
        return 1
    fi
}

# ============================================================================
# 테스트 6: 인증서 만료일 확인
# ============================================================================
test_certificate_expiration() {
    print_info "테스트 6: 인증서 만료일 확인"
    
    if [ ! -f "$CERT_FILE" ]; then
        print_fail "인증서 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    local not_after=$(openssl x509 -in "$CERT_FILE" -noout -enddate | cut -d= -f2)
    local expiry_epoch=$(date -d "$not_after" +%s 2>/dev/null || date -j -f "%b %d %T %Y %Z" "$not_after" +%s 2>/dev/null)
    local current_epoch=$(date +%s)
    local days_until_expiry=$(( ($expiry_epoch - $current_epoch) / 86400 ))
    
    if [ $days_until_expiry -gt 0 ]; then
        if [ $days_until_expiry -lt 30 ]; then
            print_warning "인증서가 곧 만료됩니다: ${days_until_expiry}일 남음 (만료일: $not_after)"
        else
            print_pass "인증서가 유효합니다: ${days_until_expiry}일 남음 (만료일: $not_after)"
        fi
    else
        print_fail "인증서가 만료되었습니다 (만료일: $not_after)"
        echo "       실행: ./scripts/generate-ssl-cert.sh"
        return 1
    fi
}

# ============================================================================
# 테스트 7: 키 강도 확인
# ============================================================================
test_key_strength() {
    print_info "테스트 7: 키 강도 확인 (최소 2048-bit)"
    
    if [ ! -f "$KEY_FILE" ]; then
        print_fail "개인키 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    local key_bits=$(openssl rsa -in "$KEY_FILE" -text -noout 2>/dev/null | grep "Private-Key:" | grep -oE '[0-9]+')
    
    if [ -z "$key_bits" ]; then
        print_fail "키 강도를 확인할 수 없습니다"
        return 1
    fi
    
    if [ "$key_bits" -ge 2048 ]; then
        print_pass "키 강도가 충분합니다: ${key_bits} bits"
    else
        print_fail "키 강도가 부족합니다: ${key_bits} bits (최소 2048 bits 필요)"
        echo "       실행: ./scripts/generate-ssl-cert.sh"
        return 1
    fi
}

# ============================================================================
# 테스트 8: 인증서와 개인키 일치 확인
# ============================================================================
test_certificate_key_match() {
    print_info "테스트 8: 인증서와 개인키 일치 확인"
    
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
# 테스트 9: 파일 권한 확인
# ============================================================================
test_file_permissions() {
    print_info "테스트 9: 파일 권한 확인"
    
    if [ ! -f "$KEY_FILE" ]; then
        print_fail "개인키 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    # 개인키 권한 확인 (600 또는 400)
    local key_perms=$(stat -c "%a" "$KEY_FILE" 2>/dev/null || stat -f "%OLp" "$KEY_FILE" 2>/dev/null)
    
    if [ "$key_perms" = "600" ] || [ "$key_perms" = "400" ]; then
        print_pass "개인키 파일 권한이 안전합니다: $key_perms"
    else
        print_warning "개인키 파일 권한이 안전하지 않습니다: $key_perms (권장: 600)"
        echo "       실행: chmod 600 $KEY_FILE"
    fi
    
    # 인증서 권한 확인
    if [ -f "$CERT_FILE" ]; then
        local cert_perms=$(stat -c "%a" "$CERT_FILE" 2>/dev/null || stat -f "%OLp" "$CERT_FILE" 2>/dev/null)
        
        if [ "$cert_perms" = "644" ] || [ "$cert_perms" = "444" ]; then
            print_pass "인증서 파일 권한이 적절합니다: $cert_perms"
        else
            print_warning "인증서 파일 권한: $cert_perms (권장: 644)"
        fi
    fi
}

# ============================================================================
# 테스트 10: Subject Alternative Names 확인
# ============================================================================
test_subject_alternative_names() {
    print_info "테스트 10: Subject Alternative Names (SAN) 확인"
    
    if [ ! -f "$CERT_FILE" ]; then
        print_fail "인증서 파일이 없어 검증을 건너뜁니다"
        return 1
    fi
    
    local san_output=$(openssl x509 -in "$CERT_FILE" -noout -text 2>/dev/null | grep -A 4 "Subject Alternative Name")
    
    if [ -n "$san_output" ]; then
        print_pass "Subject Alternative Names가 설정되어 있습니다"
        echo "$san_output" | sed 's/^/       /'
    else
        print_warning "Subject Alternative Names가 설정되어 있지 않습니다"
    fi
}

# ============================================================================
# 인증서 상세 정보 출력
# ============================================================================
display_certificate_details() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  인증서 상세 정보${NC}"
    echo -e "${BLUE}============================================${NC}"
    
    if [ -f "$CERT_FILE" ]; then
        echo ""
        openssl x509 -in "$CERT_FILE" -noout -subject -issuer -dates -fingerprint 2>/dev/null || echo "인증서 정보를 읽을 수 없습니다"
    else
        echo "인증서 파일이 없습니다"
    fi
    
    echo ""
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
        echo -e "${GREEN}✓ SSL 인증서 검증 완료!${NC}"
        echo ""
        echo "다음 단계:"
        echo "  docker-compose up -d"
        echo ""
        return 0
    else
        echo -e "${RED}✗ SSL 인증서 검증 실패${NC}"
        echo ""
        echo "문제 해결:"
        echo "  1. 인증서 재생성: ./scripts/generate-ssl-cert.sh"
        echo "  2. 파일 권한 수정: chmod 600 $KEY_FILE"
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
    test_openssl_installed || true
    test_ssl_directory_exists || true
    test_certificate_file_exists || true
    test_key_file_exists || true
    test_certificate_validity || true
    test_certificate_expiration || true
    test_key_strength || true
    test_certificate_key_match || true
    test_file_permissions || true
    test_subject_alternative_names || true
    
    # 인증서 상세 정보 출력
    display_certificate_details
    
    # 결과 요약
    display_summary
}

# 스크립트 실행
main
exit $?
