#!/bin/bash

# Docker 컨테이너 정리 및 재시작 자동화 스크립트
# 포트 충돌 및 Docker 환경 문제 해결을 위한 종합 정리 스크립트

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
    echo "🧹 Docker 환경 정리 및 재시작 스크립트"
    echo "=================================================="
    echo ""
}

# 현재 Docker 상태 확인
check_docker_status() {
    log_info "현재 실행 중인 Docker 컨테이너를 확인합니다..."
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
}

# 기본 정리 (Solebid 컨테이너만)
basic_cleanup() {
    echo ""
    log_info "기본 정리를 시작합니다..."
    echo ""
    
    # Solebid 컨테이너 중지 및 제거
    log_info "Solebid 컨테이너를 중지하고 제거합니다..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml down --remove-orphans
    
    # 사용하지 않는 이미지 정리
    log_info "사용하지 않는 Docker 이미지를 정리합니다..."
    docker image prune -f
    
    log_success "기본 정리가 완료되었습니다."
}

# 전체 정리 (모든 Docker 리소스)
full_cleanup() {
    echo ""
    log_info "전체 정리를 시작합니다..."
    echo ""
    
    # 모든 컨테이너 중지
    log_info "모든 Docker 컨테이너를 중지합니다..."
    if [ "$(docker ps -q)" ]; then
        docker stop $(docker ps -q)
    fi
    
    # 모든 컨테이너 제거
    log_info "모든 Docker 컨테이너를 제거합니다..."
    docker container prune -f
    
    # 사용하지 않는 이미지 정리
    log_info "사용하지 않는 Docker 이미지를 정리합니다..."
    docker image prune -a -f
    
    # 사용하지 않는 네트워크 정리
    log_info "사용하지 않는 Docker 네트워크를 정리합니다..."
    docker network prune -f
    
    log_success "전체 정리가 완료되었습니다."
}

# 포트 충돌 해결
resolve_port_conflicts() {
    echo ""
    log_info "포트 충돌 문제를 해결합니다..."
    echo ""
    
    # 포트 3000 사용 프로세스 확인
    log_info "포트 3000을 사용하는 프로세스를 확인합니다..."
    if lsof -ti:3000 >/dev/null 2>&1; then
        log_warning "포트 3000을 사용하는 프로세스가 발견되었습니다."
        lsof -i:3000
        read -p "해당 프로세스를 종료하시겠습니까? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            lsof -ti:3000 | xargs kill -9
            log_success "포트 3000 프로세스를 종료했습니다."
        fi
    else
        log_success "포트 3000은 현재 사용되지 않고 있습니다."
    fi
    
    # 포트 8080 사용 프로세스 확인
    log_info "포트 8080을 사용하는 프로세스를 확인합니다..."
    if lsof -ti:8080 >/dev/null 2>&1; then
        log_warning "포트 8080을 사용하는 프로세스가 발견되었습니다."
        lsof -i:8080
        read -p "해당 프로세스를 종료하시겠습니까? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            lsof -ti:8080 | xargs kill -9
            log_success "포트 8080 프로세스를 종료했습니다."
        fi
    else
        log_success "포트 8080은 현재 사용되지 않고 있습니다."
    fi
    
    # 포트 6379 사용 프로세스 확인
    log_info "포트 6379를 사용하는 프로세스를 확인합니다..."
    if lsof -ti:6379 >/dev/null 2>&1; then
        log_warning "포트 6379를 사용하는 프로세스가 발견되었습니다."
        lsof -i:6379
        read -p "해당 프로세스를 종료하시겠습니까? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            lsof -ti:6379 | xargs kill -9
            log_success "포트 6379 프로세스를 종료했습니다."
        fi
    else
        log_success "포트 6379는 현재 사용되지 않고 있습니다."
    fi
    
    log_success "포트 충돌 해결이 완료되었습니다."
}

# 네트워크 및 볼륨 정리
cleanup_network_volumes() {
    echo ""
    log_info "네트워크 및 볼륨 정리를 시작합니다..."
    echo ""
    
    # Docker 네트워크 정리
    log_info "사용하지 않는 Docker 네트워크를 정리합니다..."
    docker network prune -f
    
    # Docker 볼륨 정리 (주의: 데이터 손실 가능)
    log_warning "사용하지 않는 Docker 볼륨을 정리합니다. (데이터가 삭제될 수 있습니다)"
    read -p "계속하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker volume prune -f
        log_success "볼륨 정리가 완료되었습니다."
    else
        log_info "볼륨 정리를 건너뜁니다."
    fi
    
    log_success "네트워크 및 볼륨 정리가 완료되었습니다."
}

# 전체 시스템 정리
system_cleanup() {
    echo ""
    log_warning "전체 시스템 정리는 모든 Docker 데이터를 삭제합니다!"
    echo "    - 모든 컨테이너"
    echo "    - 모든 이미지"
    echo "    - 모든 네트워크"
    echo "    - 모든 볼륨"
    echo "    - 모든 빌드 캐시"
    echo ""
    read -p "정말로 계속하시겠습니까? (yes/no): " -r
    if [[ $REPLY == "yes" ]]; then
        log_info "전체 시스템 정리를 시작합니다..."
        docker system prune -a --volumes -f
        log_success "전체 시스템 정리가 완료되었습니다."
    else
        log_info "전체 시스템 정리를 취소했습니다."
    fi
}

# 메뉴 표시
show_menu() {
    echo "정리 옵션을 선택하세요:"
    echo "1. 기본 정리 (Solebid 컨테이너만)"
    echo "2. 전체 정리 (모든 Docker 리소스)"
    echo "3. 포트 충돌 해결"
    echo "4. 네트워크 및 볼륨 정리"
    echo "5. 전체 시스템 정리 (주의: 모든 Docker 데이터 삭제)"
    echo "0. 취소"
    echo ""
}

# 완료 메시지
print_completion() {
    echo ""
    echo "=================================================="
    log_success "Docker 정리 작업이 완료되었습니다!"
    echo "=================================================="
    echo ""
    echo "💡 다음 단계:"
    echo "    1. 개발환경 시작: ./scripts/dev-start.sh"
    echo "    2. 상태 확인: docker ps"
    echo "    3. 로그 확인: docker-compose logs -f"
    echo ""
}

# 메인 실행 함수
main() {
    print_header
    check_docker_status
    show_menu
    
    read -p "선택하세요 (0-5): " -n 1 -r
    echo
    
    case $REPLY in
        1)
            basic_cleanup
            ;;
        2)
            full_cleanup
            ;;
        3)
            resolve_port_conflicts
            ;;
        4)
            cleanup_network_volumes
            ;;
        5)
            system_cleanup
            ;;
        0)
            echo "작업이 취소되었습니다."
            exit 0
            ;;
        *)
            log_error "잘못된 선택입니다."
            exit 1
            ;;
    esac
    
    print_completion
}

# 스크립트 실행
main "$@"