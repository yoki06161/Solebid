#!/bin/bash

# Docker 환경 설정 통합 검증 스크립트
# validate-docker-compose와 test-docker-configurations 기능을 통합하여
# Docker 환경 설정의 유효성을 종합적으로 검증합니다.

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 로그 함수들
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_test() {
    echo -e "${PURPLE}[TEST]${NC} $1"
}

# 헤더 출력
print_header() {
    echo "=================================================="
    echo "🔍 Docker 환경 설정 통합 검증"
    echo "=================================================="
    echo "이 스크립트는 Docker Compose 설정의 유효성과"
    echo "다양한 환경 구성의 정상 작동을 검증합니다."
    echo "=================================================="
    echo ""
}

# 테스트 결과 추적
TESTS_PASSED=0
TESTS_FAILED=0
FAILED_TESTS=()
TEST_RESULTS=()

# 테스트 결과 기록 함수
record_test_result() {
    local test_name="$1"
    local result="$2"
    local details="${3:-}"
    
    if [ "$result" = "PASS" ]; then
        ((TESTS_PASSED++))
        log_success "$test_name"
        TEST_RESULTS+=("✓ $test_name")
    else
        ((TESTS_FAILED++))
        FAILED_TESTS+=("$test_name")
        TEST_RESULTS+=("✗ $test_name")
        log_error "$test_name"
        if [ -n "$details" ]; then
            log_error "  상세: $details"
        fi
    fi
}

# Docker Compose 파일 조합 검증
validate_compose_config() {
    local compose_files="$1"
    local config_name="$2"
    local expected_ports="$3"
    
    log_info "$config_name 설정을 검증합니다..."
    
    # 설정 파일 구문 검증
    if docker-compose $compose_files config >/dev/null 2>&1; then
        log_success "$config_name 설정이 유효합니다."
        
        # 포트 중복 검사
        local ports=$(docker-compose $compose_files config | grep -E "^\s*-\s*[0-9]+:" | sed 's/.*- //' | cut -d: -f1 | sort)
        local duplicate_ports=$(echo "$ports" | uniq -d)
        
        if [ -n "$duplicate_ports" ]; then
            record_test_result "$config_name 포트 중복 검사" "FAIL" "중복 포트: $duplicate_ports"
            return 1
        else
            record_test_result "$config_name 포트 중복 검사" "PASS"
        fi
        
        # 포트 매핑 확인 (예상 포트가 제공된 경우)
        if [ -n "$expected_ports" ]; then
            local actual_ports=$(docker-compose $compose_files config | grep -E "published:" | sed 's/.*published: "//' | sed 's/".*//' | sort -n | tr '\n' ',' | sed 's/,$//')
            
            if [ "$actual_ports" = "$expected_ports" ]; then
                record_test_result "$config_name 포트 매핑" "PASS"
            else
                record_test_result "$config_name 포트 매핑" "FAIL" "예상: $expected_ports, 실제: $actual_ports"
            fi
        fi
        
        record_test_result "$config_name 설정 검증" "PASS"
        return 0
    else
        record_test_result "$config_name 설정 검증" "FAIL" "구문 오류"
        log_error "$config_name 설정에 오류가 있습니다:"
        docker-compose $compose_files config 2>&1 | head -10
        return 1
    fi
}

# 환경 변수 검증
validate_environment_variables() {
    log_test "=== 환경 변수 설정 검증 ==="
    
    # .env 파일 존재 확인
    if [ ! -f ".env" ]; then
        record_test_result ".env 파일 존재" "FAIL" ".env 파일이 없습니다. .env.example을 참조하세요."
        return 1
    fi
    
    record_test_result ".env 파일 존재" "PASS"
    
    # 필수 환경 변수 확인
    local required_vars=(
        "DB_URL"
        "DB_USERNAME" 
        "DB_PASSWORD"
        "JWT_SECRET"
        "AWS_ACCESS_KEY"
        "AWS_SECRET_KEY"
        "S3_BUCKET"
    )
    
    local missing_vars=()
    
    source .env
    
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            missing_vars+=("$var")
        fi
    done
    
    if [ ${#missing_vars[@]} -gt 0 ]; then
        record_test_result "필수 환경 변수 설정" "FAIL" "누락된 변수: ${missing_vars[*]}"
        log_warning "다음 필수 환경 변수가 설정되지 않았습니다:"
        for var in "${missing_vars[@]}"; do
            echo "    - $var"
        done
        return 1
    else
        record_test_result "필수 환경 변수 설정" "PASS"
    fi
    
    # 환경 변수 적용 테스트
    local config_output=$(docker-compose -f docker-compose.yml -f docker-compose.dev.yml config 2>/dev/null)
    
    if echo "$config_output" | grep -q "FRONTEND_BASE_URL.*3000"; then
        record_test_result "환경 변수 적용" "PASS"
    else
        record_test_result "환경 변수 적용" "FAIL" "환경 변수가 올바르게 적용되지 않음"
    fi
    
    echo ""
    return 0
}

# 네트워크 및 볼륨 검증
validate_networks_and_volumes() {
    log_test "=== 네트워크 및 볼륨 설정 검증 ==="
    
    # 기본 설정 검증
    local compose_files="-f docker-compose.yml"
    local config_output=$(docker-compose $compose_files config 2>/dev/null)
    
    # 네트워크 확인
    if echo "$config_output" | grep -q "solebid-network"; then
        record_test_result "네트워크 설정" "PASS"
    else
        record_test_result "네트워크 설정" "FAIL" "solebid-network가 정의되지 않음"
    fi
    
    # 볼륨 확인
    if echo "$config_output" | grep -q "redis-data"; then
        record_test_result "볼륨 설정" "PASS"
    else
        record_test_result "볼륨 설정" "FAIL" "redis-data 볼륨이 정의되지 않음"
    fi
    
    echo ""
    return 0
}

# 서비스 의존성 검증
validate_service_dependencies() {
    log_test "=== 서비스 의존성 검증 ==="
    
    local compose_files="-f docker-compose.yml -f docker-compose.dev.yml"
    local config_output=$(docker-compose $compose_files config 2>/dev/null)
    
    # 백엔드가 Redis에 의존하는지 확인
    if echo "$config_output" | grep -A 20 "backend:" | grep -q "redis"; then
        record_test_result "백엔드-Redis 의존성" "PASS"
    else
        record_test_result "백엔드-Redis 의존성" "FAIL" "백엔드가 Redis에 의존하지 않음"
    fi
    
    # 프론트엔드가 백엔드에 의존하는지 확인
    if echo "$config_output" | grep -A 20 "frontend:" | grep -q "backend"; then
        record_test_result "프론트엔드-백엔드 의존성" "PASS"
    else
        record_test_result "프론트엔드-백엔드 의존성" "FAIL" "프론트엔드가 백엔드에 의존하지 않음"
    fi
    
    echo ""
    return 0
}

# Docker Compose 설정 조합 테스트
test_compose_configurations() {
    log_test "=== Docker Compose 설정 조합 테스트 ==="
    
    # 기본 설정
    validate_compose_config "-f docker-compose.yml" "기본 설정" "3000,6379,8080"
    echo ""
    
    # 개발 설정
    validate_compose_config "-f docker-compose.yml -f docker-compose.dev.yml" "개발 설정" "3000,35729,5005,6379,8080"
    echo ""
    
    # 프로덕션 설정
    validate_compose_config "-f docker-compose.yml -f docker-compose.prod.yml" "프로덕션 설정" "3000,6379,8080"
    echo ""
    
    return 0
}

# 보안 설정 검증
validate_security_configuration() {
    log_test "=== 보안 설정 검증 ==="
    
    if [ -f ".env" ]; then
        # 민감한 정보가 기본값으로 설정되어 있는지 확인
        local security_issues=false
        
        if grep -q "your-secret-key\|changeme\|password123\|admin" .env 2>/dev/null; then
            security_issues=true
        fi
        
        if [ "$security_issues" = true ]; then
            record_test_result "환경 변수 보안 설정" "FAIL" "기본값이나 약한 비밀번호 발견"
        else
            record_test_result "환경 변수 보안 설정" "PASS"
        fi
    else
        record_test_result "환경 변수 보안 설정" "FAIL" ".env 파일 없음"
    fi
    
    echo ""
    return 0
}

# 테스트 결과 요약 출력
print_test_summary() {
    echo ""
    echo "=================================================="
    echo "📊 Docker 환경 설정 검증 결과"
    echo "=================================================="
    echo ""
    
    local total_tests=$((TESTS_PASSED + TESTS_FAILED))
    
    log_success "통과한 테스트: $TESTS_PASSED"
    
    if [ $TESTS_FAILED -gt 0 ]; then
        log_error "실패한 테스트: $TESTS_FAILED"
        echo ""
        log_error "실패한 테스트 목록:"
        for test in "${FAILED_TESTS[@]}"; do
            echo "  - $test"
        done
    else
        log_success "🎉 모든 테스트가 통과했습니다!"
    fi
    
    echo ""
    echo "총 테스트 수: $total_tests"
    if [ $total_tests -gt 0 ]; then
        echo "성공률: $(( TESTS_PASSED * 100 / total_tests ))%"
    fi
    echo ""
    
    # 최종 결과
    if [ $TESTS_FAILED -eq 0 ]; then
        echo "=================================================="
        log_success "모든 Docker 환경 설정이 유효합니다!"
        echo "=================================================="
        echo ""
        echo "🚀 사용 가능한 명령어:"
        echo "    - 개발환경: docker-compose -f docker-compose.yml -f docker-compose.dev.yml up"
        echo "    - 프로덕션: docker-compose -f docker-compose.yml -f docker-compose.prod.yml up"
        echo ""
        echo "📋 다음 단계:"
        echo "    - 빠른 로컬 진단: ./scripts/docker-health-check.sh"
        echo "    - 종합 시스템 테스트: ./scripts/docker-system-test.sh"
    else
        echo "=================================================="
        log_error "Docker 환경 설정에 문제가 있습니다!"
        echo "=================================================="
        echo ""
        echo "🔧 문제 해결 방법:"
        echo "    1. .env 파일 확인 및 수정"
        echo "    2. Docker Compose 파일 구문 검사"
        echo "    3. 포트 중복 해결"
        echo "    4. 환경 변수 설정 완료"
        echo "    5. 네트워크 및 볼륨 설정 확인"
        return 1
    fi
    
    return 0
}

# 도움말 표시
show_help() {
    echo "Docker 환경 설정 통합 검증 스크립트"
    echo ""
    echo "사용법: $0 [옵션]"
    echo ""
    echo "옵션:"
    echo "  --help, -h         이 도움말 표시"
    echo "  --verbose, -v      상세한 출력 표시"
    echo "  --config-only      설정 파일 검증만 수행"
    echo "  --env-only         환경 변수 검증만 수행"
    echo ""
    echo "이 스크립트는 다음을 검증합니다:"
    echo "  - Docker Compose 설정 파일 구문 및 유효성"
    echo "  - 환경 변수 설정 및 적용"
    echo "  - 네트워크 및 볼륨 설정"
    echo "  - 서비스 의존성 관계"
    echo "  - 포트 매핑 및 중복 검사"
    echo "  - 보안 설정 기본 검증"
    echo ""
    echo "관련 스크립트:"
    echo "  - 빠른 로컬 진단: ./scripts/docker-health-check.sh"
    echo "  - 종합 시스템 테스트: ./scripts/docker-system-test.sh"
    echo ""
}

# 메인 검증 함수
main() {
    local config_only=false
    local env_only=false
    local verbose=false
    
    # 옵션 처리
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help|-h)
                show_help
                exit 0
                ;;
            --verbose|-v)
                verbose=true
                shift
                ;;
            --config-only)
                config_only=true
                shift
                ;;
            --env-only)
                env_only=true
                shift
                ;;
            *)
                echo "알 수 없는 옵션: $1"
                echo "도움말을 보려면 --help를 사용하세요."
                exit 1
                ;;
        esac
    done
    
    print_header
    
    # 선택적 검증 실행
    if [ "$env_only" = true ]; then
        validate_environment_variables
    elif [ "$config_only" = true ]; then
        test_compose_configurations
    else
        # 전체 검증 실행
        validate_environment_variables
        validate_networks_and_volumes
        validate_service_dependencies
        test_compose_configurations
        validate_security_configuration
    fi
    
    # 결과 출력
    print_test_summary
}

# 스크립트 실행
main "$@"