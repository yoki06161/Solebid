#!/bin/bash

# Docker 개발환경 포트 충돌 진단 스크립트
# 필요한 포트들의 사용 상태를 확인하고 충돌 해결 방안을 제시

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
    echo "🔍 Docker 개발환경 포트 충돌 진단"
    echo "=================================================="
    echo ""
}

# .env 파일에서 포트 설정 읽기
load_port_config() {
    if [ -f ".env" ]; then
        log_info ".env 파일에서 포트 설정을 읽습니다..."
        source .env
    else
        log_warning ".env 파일이 없습니다. 기본 포트를 사용합니다."
    fi
    
    # 기본 포트 설정
    DOCKER_FRONTEND_PORT=${DOCKER_FRONTEND_PORT:-3000}
    DOCKER_BACKEND_PORT=${DOCKER_BACKEND_PORT:-8080}
    DOCKER_REDIS_PORT=${DOCKER_REDIS_PORT:-6379}
    DOCKER_DEBUG_PORT=${DOCKER_DEBUG_PORT:-5005}
    DOCKER_LIVERELOAD_PORT=${DOCKER_LIVERELOAD_PORT:-35729}
    
    log_success "포트 설정:"
    echo "    - 프론트엔드: $DOCKER_FRONTEND_PORT"
    echo "    - 백엔드: $DOCKER_BACKEND_PORT"
    echo "    - Redis: $DOCKER_REDIS_PORT"
    echo "    - 디버깅: $DOCKER_DEBUG_PORT"
    echo "    - LiveReload: $DOCKER_LIVERELOAD_PORT"
    echo ""
}

# 포트 사용 상태 확인
check_port() {
    local port=$1
    local service=$2
    
    if lsof -ti:$port >/dev/null 2>&1; then
        log_error "포트 $port ($service)가 사용 중입니다:"
        lsof -i:$port
        return 1
    else
        log_success "포트 $port ($service)는 사용 가능합니다."
        return 0
    fi
}

# 모든 포트 확인
check_all_ports() {
    log_info "포트 사용 상태를 확인합니다..."
    echo ""
    
    local conflict_found=0
    
    # 기본 포트 확인
    log_info "기본 포트 확인:"
    check_port $DOCKER_FRONTEND_PORT "프론트엔드" || conflict_found=1
    check_port $DOCKER_BACKEND_PORT "백엔드" || conflict_found=1
    check_port $DOCKER_REDIS_PORT "Redis" || conflict_found=1
    check_port $DOCKER_DEBUG_PORT "디버깅" || conflict_found=1
    check_port $DOCKER_LIVERELOAD_PORT "LiveReload" || conflict_found=1
    
    echo ""
    

    
    return $conflict_found
}

# 해결 방안 제시
show_resolution_options() {
    echo "=================================================="
    log_warning "포트 충돌이 발견되었습니다!"
    echo "=================================================="
    echo ""
    echo "🔧 해결 방안:"
    echo ""
    echo "1. 충돌하는 프로세스 종료:"
    echo "    ./scripts/docker-cleanup.sh (옵션 3 선택)"
    echo ""
    echo "2. Docker Compose 설정 검증:"
    echo "    ./scripts/validate-setup.sh"
    echo ""
    echo "3. 자동 해결:"
    read -p "자동으로 충돌을 해결하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ./scripts/docker-cleanup.sh
    fi
}

# 성공 메시지
show_success_message() {
    echo "=================================================="
    log_success "모든 포트가 사용 가능합니다!"
    echo "=================================================="
    echo ""
    echo "🚀 Docker 개발환경을 시작할 수 있습니다:"
    echo "    ./scripts/dev-start.sh"
}

# 메인 실행 함수
main() {
    print_header
    load_port_config
    
    if check_all_ports; then
        show_success_message
    else
        show_resolution_options
    fi
    
    echo ""
}

# 스크립트 실행
main "$@"