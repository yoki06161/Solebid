#!/bin/bash

# Solebid 스마트 개발환경 시작 스크립트
# 포트 충돌을 자동으로 감지하고 해결하는 지능형 시작 스크립트

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
    echo "🚀 Solebid 스마트 개발환경 시작"
    echo "=================================================="
    echo ""
}

# .env 파일 확인 및 생성
check_env_file() {
    if [ ! -f ".env" ]; then
        log_warning ".env 파일이 없습니다. .env.example을 복사하여 생성합니다..."
        cp .env.example .env
        log_success ".env 파일이 생성되었습니다. 필요시 환경 변수를 수정하세요."
        echo ""
    fi
}

# Docker 서비스 확인
check_docker_service() {
    if ! docker info >/dev/null 2>&1; then
        log_error "Docker가 실행되지 않고 있습니다. Docker Desktop을 시작하세요."
        exit 1
    fi
}

# 포트 사용 확인
check_port_usage() {
    local port=$1
    if lsof -ti:$port >/dev/null 2>&1; then
        return 0  # 포트 사용 중
    else
        return 1  # 포트 사용 가능
    fi
}

# 포트 충돌 자동 감지 및 해결
detect_and_resolve_conflicts() {
    log_info "포트 충돌을 자동으로 감지합니다..."
    
    local use_alternative=0
    local compose_files="-f docker-compose.yml -f docker-compose.dev.yml"
    local conflicted_ports=()
    
    # .env 파일에서 현재 포트 설정 읽기
    if [ -f ".env" ]; then
        source .env
    fi
    
    # 기본 포트 설정
    local frontend_port=${DOCKER_FRONTEND_PORT:-3000}
    local backend_port=${DOCKER_BACKEND_PORT:-8080}
    local redis_port=${DOCKER_REDIS_PORT:-6379}
    local debug_port=${DOCKER_DEBUG_PORT:-5005}
    local livereload_port=${DOCKER_LIVERELOAD_PORT:-35729}
    
    # 각 포트 확인 및 충돌 기록
    if check_port_usage $frontend_port; then
        log_warning "포트 $frontend_port (프론트엔드)가 사용 중입니다."
        conflicted_ports+=("frontend:$frontend_port")
        use_alternative=1
    fi
    
    if check_port_usage $backend_port; then
        log_warning "포트 $backend_port (백엔드)가 사용 중입니다."
        conflicted_ports+=("backend:$backend_port")
        use_alternative=1
    fi
    
    if check_port_usage $redis_port; then
        log_warning "포트 $redis_port (Redis)가 사용 중입니다."
        conflicted_ports+=("redis:$redis_port")
        use_alternative=1
    fi
    
    if check_port_usage $debug_port; then
        log_warning "포트 $debug_port (디버깅)가 사용 중입니다."
        conflicted_ports+=("debug:$debug_port")
        use_alternative=1
    fi
    
    if check_port_usage $livereload_port; then
        log_warning "포트 $livereload_port (LiveReload)가 사용 중입니다."
        conflicted_ports+=("livereload:$livereload_port")
        use_alternative=1
    fi
    
    # 포트 충돌 시 오류 메시지 출력
    if [ $use_alternative -eq 1 ]; then
        log_error "포트 충돌이 감지되었습니다. 다음 포트들을 사용 중인 프로세스를 종료하세요:"
        for conflict in "${conflicted_ports[@]}"; do
            echo "    - $conflict"
        done
        echo ""
        echo "🔧 포트 정리 방법:"
        echo "    1. 포트 사용 프로세스 확인: ./scripts/check-ports.sh"
        echo "    2. Docker 컨테이너 정리: ./scripts/docker-cleanup.sh"
        echo "    3. 수동으로 프로세스 종료 후 다시 시도"
        exit 1
    else
        log_success "모든 기본 포트가 사용 가능합니다."
    fi
    
    echo ""
    echo $compose_files
}

# 기존 컨테이너 정리
cleanup_containers() {
    read -p "🧹 기존 컨테이너를 정리하시겠습니까? (권장) (Y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Nn]$ ]]; then
        log_info "기존 컨테이너를 정리합니다..."
        docker-compose down --remove-orphans >/dev/null 2>&1 || true
        log_success "정리 완료"
    fi
    echo ""
}

# 개발환경 시작
start_development_environment() {
    local compose_files=$1
    
    log_info "개발환경을 시작합니다..."
    
    echo "📍 접속 정보:"
    echo "    - 프론트엔드: http://localhost:3000"
    echo "    - 백엔드: http://localhost:8080"
    echo "    - Redis: localhost:6379"
    echo "    - 백엔드 디버깅: localhost:5005"
    echo ""
    
    # Docker Compose 실행
    log_info "Docker Compose를 시작합니다..."
    if ! docker-compose $compose_files up --build -d; then
        log_error "개발환경 시작에 실패했습니다."
        echo ""
        echo "🔧 문제 해결 방법:"
        echo "    1. 로그 확인: docker-compose logs -f"
        echo "    2. 포트 정리: ./scripts/docker-cleanup.sh"
        echo "    3. 헬스체크: ./scripts/docker-health-check.sh"
        exit 1
    fi
}

# 서비스 시작 대기 및 헬스체크
wait_and_health_check() {
    log_info "서비스가 시작될 때까지 대기합니다..."
    sleep 10
    
    log_info "서비스 상태를 확인합니다..."
    if [ -f "./scripts/docker-health-check.sh" ]; then
        chmod +x ./scripts/docker-health-check.sh
        ./scripts/docker-health-check.sh
    fi
}

# 완료 메시지
print_completion() {
    echo ""
    echo "=================================================="
    log_success "스마트 개발환경 시작이 완료되었습니다!"
    echo "=================================================="
    echo ""
    echo "💡 유용한 명령어:"
    echo "    - 로그 확인: docker-compose logs -f"
    echo "    - 상태 확인: docker-compose ps"
    echo "    - 환경 중지: docker-compose down"
    echo "    - 헬스체크: ./scripts/docker-health-check.sh"
    echo ""
}

# 메인 실행 함수
main() {
    print_header
    check_env_file
    check_docker_service
    
    local compose_files
    compose_files=$(detect_and_resolve_conflicts)
    
    cleanup_containers
    start_development_environment "$compose_files"
    wait_and_health_check
    print_completion
}

# 스크립트 실행
main "$@"