#!/bin/bash

# Solebid 프로덕션 배포 스크립트
# 프로덕션 환경에서 Docker Compose를 사용한 안전한 배포

set -e  # 에러 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
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

# 프로덕션 배포 전 체크리스트
check_prerequisites() {
    log_info "프로덕션 배포 전 필수 조건 확인 중..."
    
    # Docker 및 Docker Compose 설치 확인
    if ! command -v docker &> /dev/null; then
        log_error "Docker가 설치되지 않았습니다."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose가 설치되지 않았습니다."
        exit 1
    fi
    
    # .env.prod 파일 존재 확인
    if [ ! -f ".env.prod" ]; then
        log_error ".env.prod 파일이 존재하지 않습니다."
        log_info ".env.prod.example을 복사하여 .env.prod를 생성하고 값을 설정하세요."
        exit 1
    fi
    
    # 필수 디렉토리 생성
    mkdir -p data/redis data/prometheus logs
    
    log_success "필수 조건 확인 완료"
}

# 환경 변수 검증
validate_environment() {
    log_info "환경 변수 검증 중..."
    
    source .env.prod
    
    # 필수 환경 변수 목록
    required_vars=(
        "DB_URL"
        "DB_USERNAME" 
        "DB_PASSWORD"
        "JWT_SECRET"
        "REDIS_PASSWORD"
        "AWS_ACCESS_KEY"
        "AWS_SECRET_KEY"
        "GOOGLE_CLIENT_ID"
        "GOOGLE_CLIENT_SECRET"
        "KAKAO_CLIENT_ID"
        "KAKAO_CLIENT_SECRET"
        "PORTONE_API_KEY"
        "PORTONE_API_SECRET"
    )
    
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            log_error "필수 환경 변수 $var가 설정되지 않았습니다."
            exit 1
        fi
    done
    
    # JWT 시크릿 길이 확인 (최소 64자)
    if [ ${#JWT_SECRET} -lt 64 ]; then
        log_error "JWT_SECRET은 최소 64자 이상이어야 합니다."
        exit 1
    fi
    
    # Redis 비밀번호 길이 확인 (최소 32자)
    if [ ${#REDIS_PASSWORD} -lt 32 ]; then
        log_error "REDIS_PASSWORD는 최소 32자 이상이어야 합니다."
        exit 1
    fi
    
    log_success "환경 변수 검증 완료"
}

# 백업 생성
create_backup() {
    log_info "기존 데이터 백업 생성 중..."
    
    backup_dir="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    # Redis 데이터 백업
    if [ -d "data/redis" ]; then
        cp -r data/redis "$backup_dir/"
        log_success "Redis 데이터 백업 완료: $backup_dir/redis"
    fi
    
    # Prometheus 데이터 백업
    if [ -d "data/prometheus" ]; then
        cp -r data/prometheus "$backup_dir/"
        log_success "Prometheus 데이터 백업 완료: $backup_dir/prometheus"
    fi
    
    # 로그 백업
    if [ -d "logs" ]; then
        cp -r logs "$backup_dir/"
        log_success "로그 백업 완료: $backup_dir/logs"
    fi
    
    log_success "백업 생성 완료: $backup_dir"
}

# 이미지 빌드
build_images() {
    log_info "프로덕션 이미지 빌드 중..."
    
    # 기존 컨테이너 중지 (있는 경우)
    docker-compose -f docker-compose.prod.yml down --remove-orphans || true
    
    # 이미지 빌드 (캐시 사용하지 않음)
    docker-compose -f docker-compose.prod.yml build --no-cache --parallel
    
    log_success "이미지 빌드 완료"
}

# 서비스 배포
deploy_services() {
    log_info "프로덕션 서비스 배포 중..."
    
    # 환경 변수 파일 지정하여 서비스 시작
    docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
    
    log_success "서비스 배포 완료"
}

# 헬스체크
health_check() {
    log_info "서비스 헬스체크 수행 중..."
    
    # 서비스 시작 대기
    sleep 30
    
    # Redis 헬스체크
    if docker-compose -f docker-compose.prod.yml exec -T redis redis-cli -a "$REDIS_PASSWORD" ping | grep -q "PONG"; then
        log_success "Redis 헬스체크 통과"
    else
        log_error "Redis 헬스체크 실패"
        return 1
    fi
    
    # 백엔드 헬스체크
    max_attempts=30
    attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:8080/actuator/health &> /dev/null; then
            log_success "백엔드 헬스체크 통과"
            break
        else
            log_info "백엔드 시작 대기 중... ($attempt/$max_attempts)"
            sleep 10
            ((attempt++))
        fi
    done
    
    if [ $attempt -gt $max_attempts ]; then
        log_error "백엔드 헬스체크 실패"
        return 1
    fi
    
    # 프론트엔드 헬스체크
    if curl -f http://localhost:80 &> /dev/null; then
        log_success "프론트엔드 헬스체크 통과"
    else
        log_error "프론트엔드 헬스체크 실패"
        return 1
    fi
    
    log_success "모든 서비스 헬스체크 통과"
}

# 배포 후 정리
cleanup() {
    log_info "배포 후 정리 작업 수행 중..."
    
    # 사용하지 않는 이미지 정리
    docker image prune -f
    
    # 사용하지 않는 볼륨 정리 (주의: 데이터 손실 가능)
    # docker volume prune -f
    
    # 오래된 백업 정리 (30일 이상)
    find backups -type d -mtime +30 -exec rm -rf {} + 2>/dev/null || true
    
    log_success "정리 작업 완료"
}

# 배포 상태 확인
show_status() {
    log_info "배포 상태 확인"
    
    echo ""
    echo "=== 컨테이너 상태 ==="
    docker-compose -f docker-compose.prod.yml ps
    
    echo ""
    echo "=== 서비스 URL ==="
    echo "프론트엔드: http://localhost:80"
    echo "백엔드 API: http://localhost:8080"
    echo "백엔드 헬스체크: http://localhost:8080/actuator/health"
    echo "Prometheus: http://localhost:9090"
    
    echo ""
    echo "=== 로그 확인 명령어 ==="
    echo "전체 로그: docker-compose -f docker-compose.prod.yml logs -f"
    echo "백엔드 로그: docker-compose -f docker-compose.prod.yml logs -f backend"
    echo "프론트엔드 로그: docker-compose -f docker-compose.prod.yml logs -f frontend"
    echo "Redis 로그: docker-compose -f docker-compose.prod.yml logs -f redis"
}

# 롤백 함수
rollback() {
    log_warning "배포 롤백을 수행합니다..."
    
    # 현재 서비스 중지
    docker-compose -f docker-compose.prod.yml down
    
    # 가장 최근 백업 찾기
    latest_backup=$(ls -1t backups/ | head -n1)
    
    if [ -n "$latest_backup" ]; then
        log_info "백업에서 데이터 복원 중: $latest_backup"
        
        # 데이터 복원
        if [ -d "backups/$latest_backup/redis" ]; then
            rm -rf data/redis
            cp -r "backups/$latest_backup/redis" data/
        fi
        
        if [ -d "backups/$latest_backup/prometheus" ]; then
            rm -rf data/prometheus
            cp -r "backups/$latest_backup/prometheus" data/
        fi
        
        log_success "데이터 복원 완료"
    else
        log_warning "복원할 백업이 없습니다."
    fi
}

# 메인 배포 함수
main() {
    log_info "Solebid 프로덕션 배포를 시작합니다..."
    
    # 배포 전 확인
    check_prerequisites
    validate_environment
    
    # 사용자 확인
    echo ""
    read -p "프로덕션 배포를 계속하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "배포가 취소되었습니다."
        exit 0
    fi
    
    # 배포 실행
    create_backup
    
    if build_images && deploy_services && health_check; then
        cleanup
        show_status
        log_success "프로덕션 배포가 성공적으로 완료되었습니다!"
    else
        log_error "배포 중 오류가 발생했습니다."
        read -p "롤백을 수행하시겠습니까? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            rollback
        fi
        exit 1
    fi
}

# 스크립트 인자 처리
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "rollback")
        rollback
        ;;
    "status")
        show_status
        ;;
    "health")
        health_check
        ;;
    *)
        echo "사용법: $0 [deploy|rollback|status|health]"
        echo "  deploy   : 프로덕션 배포 (기본값)"
        echo "  rollback: 이전 버전으로 롤백"
        echo "  status   : 현재 배포 상태 확인"
        echo "  health   : 헬스체크 수행"
        exit 1
        ;;
esac