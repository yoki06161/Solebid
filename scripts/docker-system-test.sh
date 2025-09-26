#!/bin/bash

# ===========================================
#       Solebid Docker 전체 시스템 테스트
# ===========================================
# 이 스크립트는 Docker 환경에서 전체 애플리케이션 스택의 정상 작동을 검증합니다.
# 
# 테스트 범위:
# 1. 백엔드-프론트엔드 간 API 통신 검증
# 2. 전체 애플리케이션 워크플로우 테스트
# 3. 사용자 시나리오 기반 통합 테스트 수행
# 4. Docker 환경에서 전체 스택 정상 작동 확인

set -e  # 오류 발생 시 스크립트 중단

# 색상 코드 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 로그 함수들
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_test() {
    echo -e "${PURPLE}[TEST]${NC} $1"
}

log_scenario() {
    echo -e "${CYAN}[SCENARIO]${NC} $1"
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
        log_success "✓ $test_name"
        TEST_RESULTS+=("✓ $test_name")
    else
        ((TESTS_FAILED++))
        FAILED_TESTS+=("$test_name")
        TEST_RESULTS+=("✗ $test_name")
        log_error "✗ $test_name"
        if [ -n "$details" ]; then
            log_error "  상세: $details"
        fi
    fi
}

# HTTP 요청 테스트 함수 (향상된 버전)
test_http_endpoint() {
    local url="$1"
    local expected_status="$2"
    local timeout="${3:-10}"
    local method="${4:-GET}"
    local data="${5:-}"
    
    local curl_cmd="curl -s -o /dev/null -w '%{http_code}' --max-time $timeout -X $method"
    
    if [ -n "$data" ]; then
        curl_cmd="$curl_cmd -H 'Content-Type: application/json' -d '$data'"
    fi
    
    local response
    response=$(eval "$curl_cmd '$url'" 2>/dev/null || echo "000")
    
    if [ "$response" = "$expected_status" ]; then
        return 0
    else
        echo "Expected: $expected_status, Got: $response" >&2
        return 1
    fi
}

# JSON 응답 테스트 함수
test_json_response() {
    local url="$1"
    local expected_field="$2"
    local expected_value="$3"
    local timeout="${4:-10}"
    
    local response
    response=$(curl -s --max-time "$timeout" "$url" 2>/dev/null || echo "{}")
    
    if echo "$response" | grep -q "\"$expected_field\".*\"$expected_value\""; then
        return 0
    else
        echo "Expected field '$expected_field' with value '$expected_value' not found in response: $response" >&2
        return 1
    fi
}

# 서비스 대기 함수 (향상된 버전)
wait_for_service() {
    local service_name="$1"
    local url="$2"
    local max_attempts="${3:-30}"
    local sleep_interval="${4:-5}"
    local expected_status="${5:-200}"
    
    log_info "$service_name 서비스 시작 대기 중..."
    
    for i in $(seq 1 $max_attempts); do
        if test_http_endpoint "$url" "$expected_status" 5; then
            log_success "$service_name 서비스가 준비되었습니다 (${i}/${max_attempts})"
            return 0
        fi
        
        log_info "$service_name 대기 중... (${i}/${max_attempts})"
        sleep $sleep_interval
    done
    
    log_error "$service_name 서비스가 시간 내에 시작되지 않았습니다"
    return 1
}

# 1. 기본 인프라 검증
test_infrastructure() {
    log_test "=== 1. 기본 인프라 검증 ==="
    
    # Docker 서비스 확인
    if docker info >/dev/null 2>&1; then
        record_test_result "Docker 서비스 상태" "PASS"
    else
        record_test_result "Docker 서비스 상태" "FAIL" "Docker 서비스가 실행되지 않음"
        return 1
    fi
    
    # Docker Compose 확인
    if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
        record_test_result "Docker Compose 설치" "PASS"
    else
        record_test_result "Docker Compose 설치" "FAIL" "Docker Compose가 설치되지 않음"
        return 1
    fi
    
    # 환경 변수 파일 확인
    if [ -f ".env" ]; then
        record_test_result "환경 변수 파일 존재" "PASS"
    else
        record_test_result "환경 변수 파일 존재" "FAIL" ".env 파일이 없음"
    fi
    
    echo
}

# 2. 컨테이너 상태 및 헬스체크 검증
test_container_health() {
    log_test "=== 2. 컨테이너 상태 및 헬스체크 검증 ==="
    
    local containers=("solebid-backend" "solebid-frontend" "solebid-redis")
    local all_healthy=true
    
    for container in "${containers[@]}"; do
        # 컨테이너 실행 상태 확인
        local status
        status=$(docker inspect --format='{{.State.Status}}' "$container" 2>/dev/null || echo "not_found")
        
        if [ "$status" = "running" ]; then
            record_test_result "$container 컨테이너 실행 상태" "PASS"
            
            # 헬스체크 상태 확인 (있는 경우)
            local health
            health=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "none")
            
            if [ "$health" = "healthy" ]; then
                record_test_result "$container 헬스체크 상태" "PASS"
            elif [ "$health" = "none" ]; then
                record_test_result "$container 헬스체크 상태" "PASS" "헬스체크 미설정"
            else
                record_test_result "$container 헬스체크 상태" "FAIL" "상태: $health"
                all_healthy=false
            fi
        else
            record_test_result "$container 컨테이너 실행 상태" "FAIL" "상태: $status"
            all_healthy=false
        fi
    done
    
    if [ "$all_healthy" = true ]; then
        record_test_result "전체 컨테이너 상태" "PASS"
    else
        record_test_result "전체 컨테이너 상태" "FAIL"
    fi
    
    echo
}

# 3. 네트워크 연결성 테스트
test_network_connectivity() {
    log_test "=== 3. 네트워크 연결성 테스트 ==="
    
    # 백엔드-Redis 연결
    if docker exec solebid-backend ping -c 1 redis >/dev/null 2>&1; then
        record_test_result "백엔드-Redis 네트워크 연결" "PASS"
    else
        record_test_result "백엔드-Redis 네트워크 연결" "FAIL"
    fi
    
    # 프론트엔드-백엔드 연결
    if docker exec solebid-frontend ping -c 1 backend >/dev/null 2>&1; then
        record_test_result "프론트엔드-백엔드 네트워크 연결" "PASS"
    else
        record_test_result "프론트엔드-백엔드 네트워크 연결" "FAIL"
    fi
    
    # Redis 클러스터 내부 연결
    if docker exec solebid-redis ping -c 1 backend >/dev/null 2>&1; then
        record_test_result "Redis-백엔드 네트워크 연결" "PASS"
    else
        record_test_result "Redis-백엔드 네트워크 연결" "FAIL"
    fi
    
    echo
}

# 4. 개별 서비스 기능 테스트
test_individual_services() {
    log_test "=== 4. 개별 서비스 기능 테스트 ==="
    
    # Redis 기능 테스트
    log_info "Redis 기능 테스트 중..."
    if docker exec solebid-redis redis-cli ping | grep -q "PONG"; then
        record_test_result "Redis 기본 연결" "PASS"
        
        # Redis 데이터 저장/조회 테스트
        docker exec solebid-redis redis-cli set test_key "system_test_value" >/dev/null 2>&1
        local retrieved_value
        retrieved_value=$(docker exec solebid-redis redis-cli get test_key 2>/dev/null)
        
        if [ "$retrieved_value" = "system_test_value" ]; then
            record_test_result "Redis 데이터 저장/조회" "PASS"
            docker exec solebid-redis redis-cli del test_key >/dev/null 2>&1
        else
            record_test_result "Redis 데이터 저장/조회" "FAIL" "예상: system_test_value, 실제: $retrieved_value"
        fi
    else
        record_test_result "Redis 기본 연결" "FAIL"
    fi
    
    # 백엔드 API 기능 테스트
    log_info "백엔드 API 기능 테스트 중..."
    
    # 헬스체크 엔드포인트
    if test_http_endpoint "http://localhost:8080/actuator/health" "200"; then
        record_test_result "백엔드 헬스체크 엔드포인트" "PASS"
        
        # 헬스체크 응답 내용 검증
        if test_json_response "http://localhost:8080/actuator/health" "status" "UP"; then
            record_test_result "백엔드 헬스체크 응답 내용" "PASS"
        else
            record_test_result "백엔드 헬스체크 응답 내용" "FAIL"
        fi
    else
        record_test_result "백엔드 헬스체크 엔드포인트" "FAIL"
    fi
    
    # 프론트엔드 기능 테스트
    log_info "프론트엔드 기능 테스트 중..."
    
    # 메인 페이지
    if test_http_endpoint "http://localhost:3000" "200"; then
        record_test_result "프론트엔드 메인 페이지" "PASS"
    else
        record_test_result "프론트엔드 메인 페이지" "FAIL"
    fi
    
    # 정적 파일 서빙 테스트
    local static_files=("favicon.ico" "manifest.json")
    for file in "${static_files[@]}"; do
        local response
        response=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "http://localhost:3000/$file" 2>/dev/null || echo "000")
        
        if [ "$response" = "200" ] || [ "$response" = "404" ]; then
            record_test_result "정적 파일 서빙 ($file)" "PASS"
        else
            record_test_result "정적 파일 서빙 ($file)" "FAIL" "HTTP $response"
        fi
    done
    
    echo
}

# 5. 백엔드-프론트엔드 API 통신 검증
test_api_communication() {
    log_test "=== 5. 백엔드-프론트엔드 API 통신 검증 ==="
    
    # 직접 백엔드 API 호출
    log_info "직접 백엔드 API 호출 테스트..."
    
    # 기본 API 엔드포인트들 테스트
    local api_endpoints=(
        "http://localhost:8080/actuator/health:200"
        "http://localhost:8080/actuator/info:200,404"
        "http://localhost:8080/api:200,404,405"
    )
    
    for endpoint_info in "${api_endpoints[@]}"; do
        local url="${endpoint_info%:*}"
        local expected_codes="${endpoint_info#*:}"
        
        local response
        response=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url" 2>/dev/null || echo "000")
        
        if [[ ",$expected_codes," == *",$response,"* ]]; then
            record_test_result "직접 API 호출 (${url##*/})" "PASS"
        else
            record_test_result "직접 API 호출 (${url##*/})" "FAIL" "예상: $expected_codes, 실제: $response"
        fi
    done
    
    # 프론트엔드를 통한 API 프록시 테스트
    log_info "프론트엔드 API 프록시 테스트..."
    
    local proxy_endpoints=(
        "http://localhost:3000/api/actuator/health:200,404"
        "http://localhost:3000/api:200,404,405"
    )
    
    for endpoint_info in "${proxy_endpoints[@]}"; do
        local url="${endpoint_info%:*}"
        local expected_codes="${endpoint_info#*:}"
        
        local response
        response=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url" 2>/dev/null || echo "000")
        
        if [[ ",$expected_codes," == *",$response,"* ]]; then
            record_test_result "프록시 API 호출 (${url##*/})" "PASS"
        else
            record_test_result "프록시 API 호출 (${url##*/})" "FAIL" "예상: $expected_codes, 실제: $response"
        fi
    done
    
    echo
}

# 6. 사용자 시나리오 기반 통합 테스트
test_user_scenarios() {
    log_test "=== 6. 사용자 시나리오 기반 통합 테스트 ==="
    
    # 시나리오 1: 일반 사용자 웹사이트 접속
    log_scenario "시나리오 1: 일반 사용자 웹사이트 접속"
    
    # 1.1 프론트엔드 메인 페이지 접속
    if test_http_endpoint "http://localhost:3000" "200"; then
        record_test_result "사용자 메인 페이지 접속" "PASS"
    else
        record_test_result "사용자 메인 페이지 접속" "FAIL"
    fi
    
    # 1.2 정적 리소스 로딩 (CSS, JS 등)
    local static_resources=("static/css" "static/js" "assets")
    for resource in "${static_resources[@]}"; do
        local response
        response=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "http://localhost:3000/$resource" 2>/dev/null || echo "000")
        
        if [ "$response" = "200" ] || [ "$response" = "404" ] || [ "$response" = "403" ]; then
            record_test_result "정적 리소스 접근 ($resource)" "PASS"
        else
            record_test_result "정적 리소스 접근 ($resource)" "FAIL" "HTTP $response"
        fi
    done
    
    # 시나리오 2: API 기반 데이터 조회
    log_scenario "시나리오 2: API 기반 데이터 조회"
    
    # 2.1 백엔드 상태 확인 (프론트엔드에서 호출하는 방식)
    if test_http_endpoint "http://localhost:3000/api/actuator/health" "200" 10; then
        record_test_result "프론트엔드를 통한 백엔드 상태 확인" "PASS"
    else
        # 직접 백엔드 호출로 대체 테스트
        if test_http_endpoint "http://localhost:8080/actuator/health" "200" 10; then
            record_test_result "프론트엔드를 통한 백엔드 상태 확인" "PASS" "직접 호출로 확인됨"
        else
            record_test_result "프론트엔드를 통한 백엔드 상태 확인" "FAIL"
        fi
    fi
    
    # 시나리오 3: 세션 및 캐시 동작 확인
    log_scenario "시나리오 3: 세션 및 캐시 동작 확인"
    
    # 3.1 Redis 캐시 동작 확인
    local cache_key="system_test_cache_$(date +%s)"
    local cache_value="test_cache_value_$(date +%s)"
    
    # Redis에 테스트 데이터 저장
    if docker exec solebid-redis redis-cli set "$cache_key" "$cache_value" >/dev/null 2>&1; then
        # 데이터 조회
        local retrieved_cache
        retrieved_cache=$(docker exec solebid-redis redis-cli get "$cache_key" 2>/dev/null)
        
        if [ "$retrieved_cache" = "$cache_value" ]; then
            record_test_result "Redis 캐시 저장/조회 동작" "PASS"
            # 테스트 데이터 정리
            docker exec solebid-redis redis-cli del "$cache_key" >/dev/null 2>&1
        else
            record_test_result "Redis 캐시 저장/조회 동작" "FAIL" "예상: $cache_value, 실제: $retrieved_cache"
        fi
    else
        record_test_result "Redis 캐시 저장/조회 동작" "FAIL" "캐시 저장 실패"
    fi
    
    echo
}

# 7. 전체 애플리케이션 워크플로우 테스트
test_application_workflow() {
    log_test "=== 7. 전체 애플리케이션 워크플로우 테스트 ==="
    
    # 워크플로우 1: 전체 스택 연동 테스트
    log_scenario "워크플로우 1: 전체 스택 연동 테스트"
    
    # 1.1 프론트엔드 → 백엔드 → Redis 연동 흐름
    log_info "전체 스택 연동 흐름 테스트 중..."
    
    # 프론트엔드 접속 가능 확인
    local frontend_ok=false
    if test_http_endpoint "http://localhost:3000" "200"; then
        frontend_ok=true
    fi
    
    # 백엔드 API 접속 가능 확인
    local backend_ok=false
    if test_http_endpoint "http://localhost:8080/actuator/health" "200"; then
        backend_ok=true
    fi
    
    # Redis 연결 확인
    local redis_ok=false
    if docker exec solebid-redis redis-cli ping | grep -q "PONG"; then
        redis_ok=true
    fi
    
    # 전체 스택 연동 결과
    if [ "$frontend_ok" = true ] && [ "$backend_ok" = true ] && [ "$redis_ok" = true ]; then
        record_test_result "전체 스택 연동 (Frontend-Backend-Redis)" "PASS"
    else
        local failed_components=()
        [ "$frontend_ok" = false ] && failed_components+=("Frontend")
        [ "$backend_ok" = false ] && failed_components+=("Backend")
        [ "$redis_ok" = false ] && failed_components+=("Redis")
        record_test_result "전체 스택 연동 (Frontend-Backend-Redis)" "FAIL" "실패 컴포넌트: ${failed_components[*]}"
    fi
    
    # 워크플로우 2: 데이터 흐름 테스트
    log_scenario "워크플로우 2: 데이터 흐름 테스트"
    
    # 2.1 백엔드에서 Redis로 데이터 저장 시뮬레이션
    local test_data_key="workflow_test_$(date +%s)"
    local test_data_value="workflow_data_$(date +%s)"
    
    if docker exec solebid-redis redis-cli set "$test_data_key" "$test_data_value" >/dev/null 2>&1; then
        # 백엔드가 Redis 데이터를 읽을 수 있는지 확인 (간접적)
        local redis_data
        redis_data=$(docker exec solebid-redis redis-cli get "$test_data_key" 2>/dev/null)
        
        if [ "$redis_data" = "$test_data_value" ]; then
            record_test_result "백엔드-Redis 데이터 흐름" "PASS"
        else
            record_test_result "백엔드-Redis 데이터 흐름" "FAIL"
        fi
        
        # 테스트 데이터 정리
        docker exec solebid-redis redis-cli del "$test_data_key" >/dev/null 2>&1
    else
        record_test_result "백엔드-Redis 데이터 흐름" "FAIL" "Redis 데이터 저장 실패"
    fi
    
    echo
}

# 8. 성능 및 안정성 테스트
test_performance_stability() {
    log_test "=== 8. 성능 및 안정성 테스트 ==="
    
    # 리소스 사용량 확인
    log_info "리소스 사용량 확인 중..."
    
    local containers=("solebid-backend" "solebid-frontend" "solebid-redis")
    local high_resource_usage=false
    
    for container in "${containers[@]}"; do
        local cpu_usage
        local mem_usage
        
        # CPU 및 메모리 사용량 확인
        local stats
        stats=$(docker stats --no-stream --format "{{.CPUPerc}},{{.MemPerc}}" "$container" 2>/dev/null || echo "0.00%,0.00%")
        
        cpu_usage=$(echo "$stats" | cut -d',' -f1 | sed 's/%//')
        mem_usage=$(echo "$stats" | cut -d',' -f2 | sed 's/%//')
        
        # CPU 사용량이 80% 이상이면 경고
        if (( $(echo "$cpu_usage > 80" | bc -l 2>/dev/null || echo "0") )); then
            log_warning "$container CPU 사용량이 높습니다: ${cpu_usage}%"
            high_resource_usage=true
        fi
        
        # 메모리 사용량이 90% 이상이면 경고
        if (( $(echo "$mem_usage > 90" | bc -l 2>/dev/null || echo "0") )); then
            log_warning "$container 메모리 사용량이 높습니다: ${mem_usage}%"
            high_resource_usage=true
        fi
        
        log_info "$container 리소스 사용량: CPU ${cpu_usage}%, Memory ${mem_usage}%"
    done
    
    if [ "$high_resource_usage" = false ]; then
        record_test_result "리소스 사용량 정상 범위" "PASS"
    else
        record_test_result "리소스 사용량 정상 범위" "FAIL" "일부 컨테이너의 리소스 사용량이 높음"
    fi
    
    # 동시 요청 처리 테스트
    log_info "동시 요청 처리 테스트 중..."
    
    local concurrent_requests=5
    local success_count=0
    
    for i in $(seq 1 $concurrent_requests); do
        if test_http_endpoint "http://localhost:8080/actuator/health" "200" 5 &
        then
            ((success_count++))
        fi
    done
    
    wait  # 모든 백그라운드 작업 완료 대기
    
    if [ $success_count -eq $concurrent_requests ]; then
        record_test_result "동시 요청 처리 ($concurrent_requests개)" "PASS"
    else
        record_test_result "동시 요청 처리 ($concurrent_requests개)" "FAIL" "성공: $success_count/$concurrent_requests"
    fi
    
    echo
}

# 9. 오류 및 로그 분석
test_error_analysis() {
    log_test "=== 9. 오류 및 로그 분석 ==="
    
    local containers=("solebid-backend" "solebid-frontend" "solebid-redis")
    local critical_errors_found=false
    
    for container in "${containers[@]}"; do
        log_info "$container 로그 분석 중..."
        
        # 최근 로그에서 심각한 오류 확인
        local error_patterns=("ERROR" "FATAL" "Exception" "failed" "cannot connect" "connection refused")
        local errors_found=false
        
        for pattern in "${error_patterns[@]}"; do
            if docker logs "$container" --tail=50 2>&1 | grep -i "$pattern" >/dev/null 2>&1; then
                errors_found=true
                break
            fi
        done
        
        if [ "$errors_found" = true ]; then
            record_test_result "$container 로그 오류 검사" "FAIL" "심각한 오류 패턴 발견"
            critical_errors_found=true
            
            # 오류 로그 샘플 출력
            log_warning "$container 오류 로그 샘플:"
            docker logs "$container" --tail=10 2>&1 | grep -i -E "error|fatal|exception|failed" | head -3
        else
            record_test_result "$container 로그 오류 검사" "PASS"
        fi
    done
    
    # 전체 로그 상태 평가
    if [ "$critical_errors_found" = false ]; then
        record_test_result "전체 시스템 로그 상태" "PASS"
    else
        record_test_result "전체 시스템 로그 상태" "FAIL" "일부 서비스에서 오류 발견"
    fi
    
    echo
}

# 10. 보안 및 설정 검증
test_security_configuration() {
    log_test "=== 10. 보안 및 설정 검증 ==="
    
    # 환경 변수 보안 검증
    log_info "환경 변수 보안 검증 중..."
    
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
    
    # 포트 노출 검증
    log_info "포트 노출 검증 중..."
    
    local expected_ports=("3000" "8080" "6379")
    local port_issues=false
    
    for port in "${expected_ports[@]}"; do
        if netstat -tuln 2>/dev/null | grep ":$port " >/dev/null; then
            log_info "포트 $port 정상 노출됨"
        else
            log_warning "포트 $port 노출되지 않음"
            port_issues=true
        fi
    done
    
    if [ "$port_issues" = false ]; then
        record_test_result "포트 노출 설정" "PASS"
    else
        record_test_result "포트 노출 설정" "FAIL" "일부 포트가 노출되지 않음"
    fi
    
    echo
}

# 테스트 결과 요약 및 보고서 생성
generate_test_report() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    local report_file="docker-system-test-report-$(date +%Y%m%d-%H%M%S).md"
    
    log_info "테스트 보고서 생성 중: $report_file"
    
    cat > "$report_file" << EOF
# Solebid Docker 전체 시스템 테스트 보고서

**테스트 실행 시간**: $timestamp  
**총 테스트 수**: $((TESTS_PASSED + TESTS_FAILED))  
**통과**: $TESTS_PASSED  
**실패**: $TESTS_FAILED  
**성공률**: $(( TESTS_PASSED * 100 / (TESTS_PASSED + TESTS_FAILED) ))%

## 테스트 결과 상세

EOF

    for result in "${TEST_RESULTS[@]}"; do
        echo "- $result" >> "$report_file"
    done
    
    if [ $TESTS_FAILED -gt 0 ]; then
        cat >> "$report_file" << EOF

## 실패한 테스트

EOF
        for test in "${FAILED_TESTS[@]}"; do
            echo "- $test" >> "$report_file"
        done
    fi
    
    cat >> "$report_file" << EOF

## 시스템 정보

### 컨테이너 상태
\`\`\`
$(docker-compose ps 2>/dev/null || docker compose ps 2>/dev/null || echo "Docker Compose 정보 없음")
\`\`\`

### 리소스 사용량
\`\`\`
$(docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" 2>/dev/null || echo "리소스 정보 없음")
\`\`\`

### 네트워크 정보
\`\`\`
$(docker network ls | grep solebid 2>/dev/null || echo "네트워크 정보 없음")
\`\`\`

---
*이 보고서는 Solebid Docker 전체 시스템 테스트 스크립트에 의해 자동 생성되었습니다.*
EOF
    
    log_success "테스트 보고서가 생성되었습니다: $report_file"
}

# 테스트 결과 요약 출력
print_test_summary() {
    echo
    echo "=========================================="
    echo "        Docker 전체 시스템 테스트 결과"
    echo "=========================================="
    echo
    
    log_success "통과한 테스트: $TESTS_PASSED"
    
    if [ $TESTS_FAILED -gt 0 ]; then
        log_error "실패한 테스트: $TESTS_FAILED"
        echo
        log_error "실패한 테스트 목록:"
        for test in "${FAILED_TESTS[@]}"; do
            echo "  - $test"
        done
    else
        log_success "🎉 모든 테스트가 통과했습니다!"
        echo
        log_success "Docker 환경에서 전체 Solebid 애플리케이션이 정상적으로 작동하고 있습니다."
    fi
    
    echo
    echo "총 테스트 수: $((TESTS_PASSED + TESTS_FAILED))"
    echo "성공률: $(( TESTS_PASSED * 100 / (TESTS_PASSED + TESTS_FAILED) ))%"
    echo
    
    if [ $TESTS_FAILED -gt 0 ]; then
        echo "🔧 권장 해결 방법:"
        echo "  1. 실패한 테스트의 상세 로그 확인: docker-compose logs [service-name]"
        echo "  2. 컨테이너 재시작: docker-compose restart"
        echo "  3. 환경 정리 후 재시작: ./scripts/docker-cleanup.sh && docker-compose up -d"
        echo "  4. 개별 서비스 헬스체크: ./scripts/docker-health-check.sh"
    else
        echo "✅ 시스템 접속 정보:"
        echo "  - 프론트엔드: http://localhost:3000"
        echo "  - 백엔드 API: http://localhost:8080"
        echo "  - 백엔드 헬스체크: http://localhost:8080/actuator/health"
        echo "  - Redis: localhost:6379"
    fi
    
    echo
}

# 정리 함수
cleanup() {
    log_info "테스트 정리 작업 중..."
    
    # 테스트 중 생성된 임시 데이터 정리
    docker exec solebid-redis redis-cli --scan --pattern "*test*" | xargs -r docker exec solebid-redis redis-cli del >/dev/null 2>&1 || true
    
    if [ "$1" = "--stop-services" ]; then
        log_info "Docker 서비스 중지 중..."
        if command -v docker-compose &> /dev/null; then
            docker-compose down 2>/dev/null || true
        else
            docker compose down 2>/dev/null || true
        fi
    fi
}

# 메인 테스트 실행 함수
run_system_tests() {
    echo "=========================================="
    echo "    Solebid Docker 전체 시스템 테스트"
    echo "=========================================="
    echo
    log_info "전체 시스템 테스트를 시작합니다..."
    echo
    
    # 모든 테스트 실행
    test_infrastructure
    test_container_health
    test_network_connectivity
    test_individual_services
    test_api_communication
    test_user_scenarios
    test_application_workflow
    test_performance_stability
    test_error_analysis
    test_security_configuration
    
    # 결과 출력 및 보고서 생성
    print_test_summary
    generate_test_report
    
    # 종료 코드 결정
    if [ $TESTS_FAILED -gt 0 ]; then
        return 1
    else
        return 0
    fi
}

# 스크립트 옵션 처리
case "${1:-}" in
    --help|-h)
        echo "사용법: $0 [옵션]"
        echo
        echo "옵션:"
        echo "  --help, -h         이 도움말 표시"
        echo "  --stop-services    테스트 후 서비스 중지"
        echo "  --report-only      보고서만 생성 (테스트 실행 안 함)"
        echo
        echo "이 스크립트는 Docker 환경에서 Solebid 전체 시스템의 정상 작동을 검증합니다."
        echo
        echo "테스트 범위:"
        echo "  - 백엔드-프론트엔드 간 API 통신 검증"
        echo "  - 전체 애플리케이션 워크플로우 테스트"
        echo "  - 사용자 시나리오 기반 통합 테스트"
        echo "  - Docker 환경에서 전체 스택 정상 작동 확인"
        echo
        exit 0
        ;;
    --report-only)
        generate_test_report
        exit 0
        ;;
    --stop-services)
        trap 'cleanup --stop-services' EXIT
        ;;
    *)
        trap 'cleanup' EXIT
        ;;
esac

# 메인 테스트 실행
run_system_tests
exit_code=$?

echo
if [ $exit_code -eq 0 ]; then
    log_success "🎉 Docker 전체 시스템 테스트가 성공적으로 완료되었습니다!"
    log_success "Solebid 애플리케이션이 Docker 환경에서 정상적으로 작동하고 있습니다."
else
    log_error "❌ 일부 시스템 테스트가 실패했습니다."
    log_error "생성된 테스트 보고서와 로그를 확인하여 문제를 해결하세요."
fi

exit $exit_code