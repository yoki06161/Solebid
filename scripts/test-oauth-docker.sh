#!/bin/bash
# OAuth Docker 환경 테스트 스크립트 (Linux/macOS)
# 이 스크립트는 Docker 환경에서 OAuth 소셜 로그인 기능을 테스트합니다.

set -e  # 오류 발생 시 스크립트 중단

echo "==================================="
echo "  OAuth Docker 환경 테스트 시작"
echo "==================================="
echo

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 성공/실패 메시지 함수
success() {
    echo -e "${GREEN}[완료]${NC} $1"
}

error() {
    echo -e "${RED}[오류]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[경고]${NC} $1"
}

# 1. Docker Compose 서비스 상태 확인
echo "1. Docker Compose 서비스 상태 확인..."
if ! docker-compose ps >/dev/null 2>&1; then
    error "Docker Compose 서비스를 찾을 수 없습니다."
    echo "docker-compose up -d 명령으로 서비스를 먼저 시작하세요."
    exit 1
fi
docker-compose ps
success "서비스 상태 확인 완료"
echo

# 2. 백엔드 헬스체크
echo "2. 백엔드 헬스체크..."
if ! curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    error "백엔드 서비스가 응답하지 않습니다."
    echo "백엔드 컨테이너 로그를 확인하세요: docker logs solebid-backend"
    exit 1
fi
success "백엔드 서비스 정상 응답"
echo

# 3. 프론트엔드 접근 확인
echo "3. 프론트엔드 접근 확인..."
if ! curl -f http://localhost:3000 >/dev/null 2>&1; then
    error "프론트엔드 서비스가 응답하지 않습니다."
    echo "프론트엔드 컨테이너 로그를 확인하세요: docker logs solebid-frontend"
    exit 1
fi
success "프론트엔드 서비스 정상 응답"
echo

# 4. Redis 연결 확인
echo "4. Redis 연결 확인..."
if ! docker exec solebid-redis redis-cli ping >/dev/null 2>&1; then
    error "Redis 서비스가 응답하지 않습니다."
    echo "Redis 컨테이너 로그를 확인하세요: docker logs solebid-redis"
    exit 1
fi
success "Redis 서비스 정상 응답"
echo

# 5. Google OAuth URL 생성 테스트
echo "5. Google OAuth URL 생성 테스트..."
GOOGLE_RESPONSE=$(curl -s http://localhost:8080/api/auth/oauth2/google/url)
if [[ $? -ne 0 ]]; then
    error "Google OAuth URL 생성 실패"
    exit 1
fi

# JSON 응답에서 success 필드 확인
if ! echo "$GOOGLE_RESPONSE" | grep -q '"success":true'; then
    error "Google OAuth URL 생성 응답이 올바르지 않습니다."
    echo "응답 내용: $GOOGLE_RESPONSE"
    exit 1
fi
success "Google OAuth URL 생성 성공"
echo

# 6. Kakao OAuth URL 생성 테스트
echo "6. Kakao OAuth URL 생성 테스트..."
KAKAO_RESPONSE=$(curl -s http://localhost:8080/api/auth/oauth2/kakao/url)
if [[ $? -ne 0 ]]; then
    error "Kakao OAuth URL 생성 실패"
    exit 1
fi

# JSON 응답에서 success 필드 확인
if ! echo "$KAKAO_RESPONSE" | grep -q '"success":true'; then
    error "Kakao OAuth URL 생성 응답이 올바르지 않습니다."
    echo "응답 내용: $KAKAO_RESPONSE"
    exit 1
fi
success "Kakao OAuth URL 생성 성공"
echo

# 7. OAuth 환경 변수 확인
echo "7. OAuth 환경 변수 확인..."
echo "Google Client ID:"
docker exec solebid-backend printenv GOOGLE_CLIENT_ID || warning "GOOGLE_CLIENT_ID 환경 변수가 설정되지 않았습니다."
echo "Kakao Client ID:"
docker exec solebid-backend printenv KAKAO_CLIENT_ID || warning "KAKAO_CLIENT_ID 환경 변수가 설정되지 않았습니다."
echo "Google Redirect URI:"
docker exec solebid-backend printenv GOOGLE_REDIRECT_URI || warning "GOOGLE_REDIRECT_URI 환경 변수가 설정되지 않았습니다."
echo "Kakao Redirect URI:"
docker exec solebid-backend printenv KAKAO_REDIRECT_URI || warning "KAKAO_REDIRECT_URI 환경 변수가 설정되지 않았습니다."
echo

# 8. CORS 설정 테스트
echo "8. CORS 설정 테스트..."
CORS_RESPONSE=$(curl -s -H "Origin: http://localhost:3000" \
                      -H "Access-Control-Request-Method: POST" \
                      -H "Access-Control-Request-Headers: Content-Type" \
                      -X OPTIONS \
                      http://localhost:8080/api/auth/oauth2/google/url)
if [[ $? -ne 0 ]]; then
    warning "CORS 프리플라이트 요청 실패 - 브라우저에서 CORS 오류가 발생할 수 있습니다."
else
    success "CORS 설정 정상"
fi
echo

# 9. 네트워크 연결 테스트
echo "9. Docker 네트워크 연결 테스트..."
echo "백엔드에서 프론트엔드로 연결 테스트:"
if docker exec solebid-backend curl -s -I http://frontend:80 | grep -q "HTTP"; then
    success "백엔드 → 프론트엔드 연결 정상"
else
    warning "백엔드 → 프론트엔드 연결 실패"
fi

echo "프론트엔드에서 백엔드로 연결 테스트:"
if docker exec solebid-frontend curl -s -I http://backend:8080/actuator/health | grep -q "HTTP"; then
    success "프론트엔드 → 백엔드 연결 정상"
else
    warning "프론트엔드 → 백엔드 연결 실패"
fi
echo

# 10. OAuth 응답 상세 분석
echo "10. OAuth 응답 상세 분석..."
echo "Google OAuth 응답:"
echo "$GOOGLE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$GOOGLE_RESPONSE"
echo
echo "Kakao OAuth 응답:"
echo "$KAKAO_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$KAKAO_RESPONSE"
echo

echo "==================================="
echo "  OAuth Docker 환경 테스트 완료"
echo "==================================="
echo
success "모든 테스트가 성공했습니다!"
echo
echo "다음 단계:"
echo "1. 브라우저에서 http://localhost:3000 접속"
echo "2. 회원가입 또는 로그인 페이지로 이동"
echo "3. '구글로 시작하기' 또는 '카카오로 시작하기' 버튼 클릭"
echo "4. OAuth 인증 플로우 테스트"
echo
echo "OAuth 제공자 설정 확인사항:"
echo "- Google: http://localhost:3000/auth/callback/google 리다이렉트 URI 등록 필요"
echo "- Kakao: http://localhost:3000/auth/callback/kakao 리다이렉트 URI 등록 필요"
echo
echo "문제가 발생하면 다음 명령으로 로그를 확인하세요:"
echo "- 전체 로그: docker-compose logs -f"
echo "- 백엔드 로그: docker logs -f solebid-backend"
echo "- 프론트엔드 로그: docker logs -f solebid-frontend"