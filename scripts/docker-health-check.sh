#!/bin/bash

# Docker 개발환경 안정성 검증 스크립트
# 전체 Docker 환경의 상태를 확인하고 문제점을 진단

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 헤더 출력
print_header() {
    echo "=================================================="
    echo "🏥 Docker 개발환경 안정성 검증"
    echo "=================================================="
    echo ""
}

# 전체 검증 결과 추적
overall_status=0

# 1. Docker 서비스 상태 확인
check_docker_service() {
    log_info "1. Docker 서비스 상태를 확인합니다..."
    
    if docker info >/dev/null 2>&1; then
        log_success "Docker 서비스가 정상 실행 중입니다."
    else
        log_error "Docker 서비스가 실행되지 않고 있습니다."
        overall_status=1
    fi
    echo ""
}

# 2. 컨테이너 상태 확인
check_container_status() {
    log_info "2. 컨테이너 상태를 확인합니다..."
    
    if docker-compose ps >/dev/null 2>&1; then
        log_success "Docker Compose 프로젝트 상태:"
        docker-compose ps --format "table {{.Name}}\t{{.State}}\t{{.Status}}"
    else
        log_warning "Docker Compose 프로젝트가 실행되지 않고 있습니다."
    fi
    echo ""
}

# 3. 헬스체크 상태 확인
check_health_status() {
    log_info "3. 서비스 헬스체크를 확인합니다..."
    
    # 백엔드 헬스체크
    echo "    - 백엔드 API 헬스체크..."
    if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_success "백엔드 API가 정상 응답합니다."
    else
        log_error "백엔드 API가 응답하지 않습니다."
        overall_status=1
    fi
    
    # 프론트엔드 헬스체크
    echo "    - 프론트엔드 웹서버 헬스체크..."
    if curl -f -s http://localhost:3000/ >/dev/null 2>&1; then
        log_success "프론트엔드 웹서버가 정상 응답합니다."
    else
        log_error "프론트엔드 웹서버가 응답하지 않습니다."
        overall_status=1
    fi
    
    # Redis 헬스체크
    echo "    - Redis 연결 확인..."
    if docker exec solebid-redis redis-cli ping >/dev/null 2>&1; then
        log_success "Redis 서버가 정상 작동합니다."
    else
        log_error "Redis 서버에 연결할 수 없습니다."
        overall_status=1
    fi
    echo ""
}

# 4. 네트워크 연결 확인
check_network_connectivity() {
    log_info "4. 네트워크 연결을 확인합니다..."
    
    # 백엔드-Redis 연결 확인
    if docker exec solebid-backend ping -c 1 redis >/dev/null 2>&1; then
        log_success "백엔드-Redis 네트워크 연결이 정상입니다."
    else
        log_error "백엔드에서 Redis로 연결할 수 없습니다."
        overall_status=1
    fi
    
    # 프론트엔드-백엔드 연결 확인 (API 프록시)
    if curl -f -s http://localhost:3000/api/health >/dev/null 2>&1; then
        log_success "프론트엔드-백엔드 API 프록시가 정상 작동합니다."
    else
        log_warning "프론트엔드-백엔드 API 프록시 연결을 확인할 수 없습니다."
    fi
    echo ""
}

# 5. 리소스 사용량 확인
check_resource_usage() {
    log_info "5. 리소스 사용량을 확인합니다..."
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
    echo ""
}

# 6. 볼륨 및 데이터 확인
check_volumes() {
    log_info "6. 볼륨 및 데이터 상태를 확인합니다..."
    
    if docker volume ls | grep -q solebid; then
        log_success "Docker 볼륨이 정상적으로 생성되어 있습니다:"
        docker volume ls | grep solebid
    else
        log_warning "Solebid 관련 볼륨을 찾을 수 없습니다."
    fi
    echo ""
}

# 7. 로그 오류 확인
check_logs_for_errors() {
    log_info "7. 최근 로그에서 오류를 확인합니다..."
    
    if docker-compose logs --tail=20 2>&1 | grep -i -E "error|exception|failed" >/dev/null 2>&1; then
        log_warning "최근 로그에서 오류가 발견되었습니다:"
        docker-compose logs --tail=10 2>&1 | grep -i -E "error|exception|failed"
        overall_status=1
    else
        log_success "최근 로그에서 심각한 오류가 발견되지 않았습니다."
    fi
    echo ""
}

# 8. 환경 변수 검증
check_environment_variables() {
    log_info "8. 환경 변수를 검증합니다..."
    
    if [ -f "scripts/validate-env.sh" ]; then
        if ./scripts/validate-env.sh >/dev/null 2>&1; then
            log_success "환경 변수가 올바르게 설정되어 있습니다."
        else
            log_warning "환경 변수 설정에 문제가 있을 수 있습니다."
            echo "    자세한 내용은 ./scripts/validate-env.sh를 실행하세요."
        fi
    else
        log_warning "환경 변수 검증 스크립트를 찾을 수 없습니다."
    fi
    echo ""
}

# 최종 결과 출력
print_final_result() {
    echo "=================================================="
    if [ $overall_status -eq 0 ]; then
        log_success "Docker 개발환경이 안정적으로 작동하고 있습니다!"
        echo "=================================================="
        echo ""
        echo "🎯 접속 정보:"
        echo "    - 프론트엔드: http://localhost:3000"
        echo "    - 백엔드 API: http://localhost:8080"
        echo "    - 백엔드 헬스체크: http://localhost:8080/actuator/health"
        echo "    - Redis: localhost:6379"
    else
        log_warning "Docker 개발환경에 문제가 발견되었습니다!"
        echo "=================================================="
        echo ""
        echo "🔧 권장 해결 방법:"
        echo "    1. 로그 확인: docker-compose logs -f"
        echo "    2. 컨테이너 재시작: docker-compose restart"
        echo "    3. 환경 정리: ./scripts/docker-cleanup.sh"
        echo "    4. 트러블슈팅 가이드: docs/docker-troubleshooting-guide.md"
    fi
    
    echo ""
    echo "📊 추가 모니터링:"
    echo "    - 실시간 로그: docker-compose logs -f"
    echo "    - 리소스 모니터링: docker stats"
    echo "    - 컨테이너 상태: docker-compose ps"
    echo ""
}

# 메인 실행 함수
main() {
    print_header
    check_docker_service
    check_container_status
    check_health_status
    check_network_connectivity
    check_resource_usage
    check_volumes
    check_logs_for_errors
    check_environment_variables
    print_final_result
}

# 스크립트 실행
main "$@"