#!/bin/bash

# Solebid Docker 초기 설정 자동화 스크립트
# 프로젝트를 처음 설정하는 개발자를 위한 원클릭 설정 스크립트

set -e  # 오류 발생 시 스크립트 중단

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
    echo "🚀 Solebid Docker 환경 초기 설정"
    echo "=================================================="
    echo ""
}

# 시스템 요구사항 확인
check_requirements() {
    log_info "시스템 요구사항을 확인합니다..."
    
    # Docker 설치 확인
    if ! command -v docker &> /dev/null; then
        log_error "Docker가 설치되지 않았습니다."
        echo "Docker 설치 가이드: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    # Docker Compose 설치 확인
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose가 설치되지 않았습니다."
        echo "Docker Compose 설치 가이드: https://docs.docker.com/compose/install/"
        exit 1
    fi
    
    # Docker 서비스 실행 확인
    if ! docker info &> /dev/null; then
        log_error "Docker 서비스가 실행되지 않고 있습니다."
        echo "Docker Desktop을 시작하거나 Docker 서비스를 시작하세요."
        exit 1
    fi
    
    log_success "시스템 요구사항 확인 완료"
    echo "  - Docker: $(docker --version)"
    echo "  - Docker Compose: $(docker-compose --version)"
    echo ""
}

# 환경 변수 파일 설정
setup_env_files() {
    log_info "환경 변수 파일을 설정합니다..."
    
    # 개발환경 .env 파일 생성
    if [ ! -f ".env" ]; then
        if [ -f ".env.example" ]; then
            cp .env.example .env
            log_success ".env 파일이 생성되었습니다."
        else
            log_error ".env.example 파일을 찾을 수 없습니다."
            exit 1
        fi
    else
        log_warning ".env 파일이 이미 존재합니다. 건너뜁니다."
    fi
    
    # 프로덕션 환경 .env.prod 파일 생성 (선택사항)
    if [ ! -f ".env.prod" ]; then
        if [ -f ".env.prod.example" ]; then
            read -p "프로덕션용 .env.prod 파일을 생성하시겠습니까? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                cp .env.prod.example .env.prod
                log_success ".env.prod 파일이 생성되었습니다."
            fi
        fi
    fi
    
    echo ""
}

# 필수 디렉토리 생성
create_directories() {
    log_info "필수 디렉토리를 생성합니다..."
    
    # 데이터 디렉토리 생성
    mkdir -p data/redis
    mkdir -p data/prometheus
    mkdir -p logs
    mkdir -p backups
    
    # 권한 설정 (Linux/Mac)
    if [[ "$OSTYPE" == "linux-gnu"* ]] || [[ "$OSTYPE" == "darwin"* ]]; then
        chmod 755 data/redis
        chmod 755 data/prometheus
        chmod 755 logs
        chmod 755 backups
    fi
    
    log_success "디렉토리 생성 완료"
    echo "  - data/redis: Redis 데이터 저장"
    echo "  - data/prometheus: Prometheus 메트릭 저장"
    echo "  - logs: 애플리케이션 로그"
    echo "  - backups: 백업 파일"
    echo ""
}

# Docker 이미지 사전 다운로드
pull_base_images() {
    log_info "기본 Docker 이미지를 다운로드합니다..."
    
    # 기본 이미지들 다운로드
    docker pull redis:7-alpine
    docker pull nginx:alpine
    docker pull openjdk:17-jdk-slim
    docker pull node:18-alpine
    
    log_success "기본 이미지 다운로드 완료"
    echo ""
}

# 환경 변수 검증
validate_env() {
    log_info "환경 변수를 검증합니다..."
    
    if [ -f "scripts/validate-env.sh" ]; then
        chmod +x scripts/validate-env.sh
        if ./scripts/validate-env.sh; then
            log_success "환경 변수 검증 완료"
        else
            log_warning "환경 변수 검증에서 경고가 발생했습니다."
            echo "계속 진행하려면 .env 파일을 확인하고 필요한 값을 설정하세요."
        fi
    else
        log_warning "환경 변수 검증 스크립트를 찾을 수 없습니다."
    fi
    echo ""
}

# 개발환경 테스트 빌드
test_build() {
    log_info "개발환경 테스트 빌드를 수행합니다..."
    
    read -p "테스트 빌드를 수행하시겠습니까? (시간이 다소 걸릴 수 있습니다) (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if docker-compose -f docker-compose.yml -f docker-compose.dev.yml build; then
            log_success "테스트 빌드 완료"
        else
            log_error "테스트 빌드 실패"
            echo "빌드 로그를 확인하고 문제를 해결하세요."
            exit 1
        fi
    else
        log_info "테스트 빌드를 건너뜁니다."
    fi
    echo ""
}

# 스크립트 실행 권한 설정
setup_script_permissions() {
    log_info "스크립트 실행 권한을 설정합니다..."
    
    # 실행 권한 부여
    chmod +x scripts/*.sh
    
    log_success "스크립트 실행 권한 설정 완료"
    echo ""
}

# 완료 메시지 및 다음 단계 안내
print_completion() {
    echo "=================================================="
    log_success "Solebid Docker 환경 초기 설정이 완료되었습니다!"
    echo "=================================================="
    echo ""
    echo "🎯 다음 단계:"
    echo ""
    echo "1. 환경 변수 설정:"
    echo "    - .env 파일을 편집하여 필요한 환경 변수를 설정하세요"
    echo "    - 특히 데이터베이스, AWS, OAuth2 설정을 확인하세요"
    echo ""
    echo "2. 개발환경 시작:"
    echo "    ./scripts/dev-start.sh"
    echo ""
    echo "3. 접속 URL:"
    echo "    - 프론트엔드: http://localhost:3000"
    echo "    - 백엔드 API: http://localhost:8080"
    echo "    - 백엔드 헬스체크: http://localhost:8080/actuator/health"
    echo ""
    echo "📚 추가 문서:"
    echo "    - Docker 가이드: README-Docker.md"
    echo "    - 환경 변수 가이드: docs/environment-variables-guide.md"
    echo "    - 시스템 테스트 가이드: docs/docker-system-testing-guide.md"
    echo ""
    echo "❓ 문제가 발생하면:"
    echo "    - 로그 확인: docker-compose logs -f"
    echo "    - 환경 변수 검증: ./scripts/validate-env.sh"
    echo "    - 컨테이너 정리: docker-compose down --volumes"
    echo ""
}

# 메인 실행 함수
main() {
    print_header
    check_requirements
    setup_env_files
    create_directories
    setup_script_permissions
    pull_base_images
    validate_env
    test_build
    print_completion
}

# 스크립트 실행
main "$@"