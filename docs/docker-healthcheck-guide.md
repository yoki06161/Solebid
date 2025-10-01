# Docker 헬스체크 및 의존성 관리 가이드

## 개요

이 문서는 Solebid 프로젝트의 Docker 컨테이너 헬스체크 및 서비스 간 의존성 관리 설정에 대해 설명합니다.

## 헬스체크 설정

### 1. 서비스별 헬스체크 구성

#### Redis 헬스체크
```yaml
healthcheck:
  test: ["CMD-SHELL", "redis-cli ping | grep PONG || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 3
  start_period: 10s
```

#### Backend (Spring Boot) 헬스체크
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 5
  start_period: 90s
```

#### Frontend (Nginx) 헬스체크
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:80/ || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 30s
```

### 2. 헬스체크 매개변수 설명

- **test**: 헬스체크 명령어
- **interval**: 헬스체크 실행 간격
- **timeout**: 각 헬스체크의 타임아웃
- **retries**: 실패 시 재시도 횟수
- **start_period**: 컨테이너 시작 후 헬스체크 시작까지의 대기 시간

## 의존성 관리 설정

### 1. 서비스 시작 순서

```yaml
services:
  redis:
    # Redis는 의존성이 없으므로 가장 먼저 시작
    
  backend:
    depends_on:
      redis:
        condition: service_healthy  # Redis가 healthy 상태가 될 때까지 대기
        
  frontend:
    depends_on:
      backend:
        condition: service_healthy  # Backend가 healthy 상태가 될 때까지 대기
      redis:
        condition: service_healthy  # Redis도 healthy 상태 확인
```

### 2. 의존성 조건 설명

- **service_started**: 서비스가 시작되면 바로 다음 서비스 시작 (기본값)
- **service_healthy**: 서비스가 healthy 상태가 될 때까지 대기
- **service_completed_successfully**: 서비스가 성공적으로 완료될 때까지 대기

## 자동 재시작 정책

### 1. 재시작 정책 종류

```yaml
restart: unless-stopped  # 프로덕션 환경 (수동으로 중지하지 않는 한 항상 재시작)
restart: on-failure      # 개발 환경 (실패 시에만 재시작)
restart: always          # 항상 재시작
restart: no              # 재시작 안함 (기본값)
```

### 2. 환경별 재시작 정책

#### 프로덕션 환경 (`docker-compose.yml`)
- **unless-stopped**: 서비스가 수동으로 중지되지 않는 한 항상 재시작
- 시스템 재부팅 후에도 자동으로 서비스 시작

#### 개발 환경 (`docker-compose.dev.yml`)
- **on-failure**: 컨테이너가 오류로 종료된 경우에만 재시작
- 개발 중 의도적인 중지 시 자동 재시작 방지

## 헬스체크 모니터링 도구

### 1. 헬스체크 스크립트 사용법

#### Linux/macOS
```bash
# 전체 서비스 헬스체크
./scripts/healthcheck/docker-health-check.sh

# 특정 서비스 대기
./scripts/healthcheck/wait-for-it.sh redis:6379 -- echo "Redis is ready"
./scripts/healthcheck/wait-for-it.sh localhost:8080 -- echo "Backend is ready"
```

#### Windows
```cmd
# 전체 서비스 헬스체크
scripts\healthcheck\docker-health-check.bat
```

### 2. Docker 명령어로 헬스체크 확인

```bash
# 모든 컨테이너의 헬스체크 상태 확인
docker ps --format "table {{.Names}}\t{{.Status}}"

# 특정 컨테이너의 상세 헬스체크 정보
docker inspect solebid-backend --format='{{json .State.Health}}'

# 헬스체크 로그 확인
docker inspect solebid-backend --format='{{range .State.Health.Log}}{{.Output}}{{end}}'
```

## 문제 해결

### 1. 헬스체크 실패 시 대응

#### Backend 헬스체크 실패
```bash
# 백엔드 로그 확인
docker-compose logs backend

# Spring Boot Actuator 엔드포인트 수동 확인
curl http://localhost:8080/actuator/health

# 백엔드 컨테이너 재시작
docker-compose restart backend
```

#### Redis 헬스체크 실패
```bash
# Redis 로그 확인
docker-compose logs redis

# Redis 연결 수동 테스트
docker exec solebid-redis redis-cli ping

# Redis 컨테이너 재시작
docker-compose restart redis
```

#### Frontend 헬스체크 실패
```bash
# 프론트엔드 로그 확인
docker-compose logs frontend

# Nginx 상태 수동 확인
curl http://localhost:3000/

# 프론트엔드 컨테이너 재시작
docker-compose restart frontend
```

### 2. 의존성 문제 해결

#### 서비스 시작 순서 문제
```bash
# 모든 서비스 중지 후 순차적 재시작
docker-compose down
docker-compose up -d

# 특정 서비스만 재시작 (의존성 순서 고려)
docker-compose restart redis backend frontend
```

#### 네트워크 연결 문제
```bash
# Docker 네트워크 상태 확인
docker network ls
docker network inspect solebid-network

# 컨테이너 간 연결 테스트
docker exec solebid-backend ping redis
docker exec solebid-frontend ping backend
```

### 3. 성능 최적화

#### 헬스체크 간격 조정
- **개발 환경**: 더 빠른 간격으로 설정하여 빠른 피드백
- **프로덕션 환경**: 적절한 간격으로 설정하여 시스템 부하 최소화

#### 시작 시간 최적화
- **start_period**: 각 서비스의 실제 시작 시간에 맞게 조정
- **timeout**: 네트워크 상황에 맞게 적절히 설정

## 모니터링 및 알림

### 1. 헬스체크 상태 모니터링

```bash
# 실시간 헬스체크 상태 모니터링
watch -n 5 'docker ps --format "table {{.Names}}\t{{.Status}}"'

# 헬스체크 실패 시 알림 (예시)
while true; do
  if ! ./scripts/healthcheck/docker-health-check.sh; then
    echo "헬스체크 실패 알림을 여기에 추가"
  fi
  sleep 60
done
```

### 2. 로그 모니터링

```bash
# 모든 서비스 로그 실시간 모니터링
docker-compose logs -f

# 특정 서비스 로그만 모니터링
docker-compose logs -f backend

# 오류 로그만 필터링
docker-compose logs | grep -i error
```

## 베스트 프랙티스

### 1. 헬스체크 설계 원칙

- **간단하고 빠른 체크**: 복잡한 로직보다는 기본적인 연결성 확인
- **적절한 타임아웃**: 너무 짧으면 false positive, 너무 길면 느린 감지
- **의미 있는 체크**: 실제 서비스 가용성을 반영하는 체크

### 2. 의존성 관리 원칙

- **최소 의존성**: 꼭 필요한 의존성만 설정
- **순환 의존성 방지**: 서비스 간 순환 의존성 피하기
- **graceful degradation**: 의존 서비스 실패 시 적절한 대응

### 3. 운영 고려사항

- **리소스 사용량 모니터링**: 헬스체크로 인한 추가 부하 고려
- **로그 관리**: 헬스체크 로그의 적절한 보관 및 순환
- **알림 설정**: 중요한 헬스체크 실패에 대한 적절한 알림 체계