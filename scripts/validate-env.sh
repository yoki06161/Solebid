#!/bin/bash

# Solebid 환경 변수 검증 스크립트
# 이 스크립트는 Docker 환경에서 필요한 환경 변수들이 올바르게 설정되었는지 확인합니다.

echo "=========================================="
echo "  Solebid 환경 변수 검증 스크립트"
echo "=========================================="
echo

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 검증 결과 카운터
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0

# .env 파일 존재 확인
check_env_file() {
    echo "1. .env 파일 존재 확인..."
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f ".env" ]; then
        echo -e "${GREEN}✓${NC} .env 파일이 존재합니다."
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}✗${NC} .env 파일이 존재하지 않습니다."
        echo -e "${YELLOW}  해결방법: cp .env.example .env${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# 환경 변수 검증 함수
check_env_var() {
    local var_name=$1
    local var_description=$2
    local is_required=${3:-true}
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    # .env 파일에서 변수 값 읽기
    local var_value=$(grep "^${var_name}=" .env 2>/dev/null | cut -d'=' -f2- | sed 's/^"//' | sed 's/"$//')
    
    if [ -z "$var_value" ] || [ "$var_value" = "your-${var_name,,}" ] || [[ "$var_value" == *"your-"* ]]; then
        if [ "$is_required" = true ]; then
            echo -e "${RED}✗${NC} ${var_name}: ${var_description} - 설정되지 않음"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
        else
            echo -e "${YELLOW}⚠${NC} ${var_name}: ${var_description} - 선택적 설정"
            WARNING_CHECKS=$((WARNING_CHECKS + 1))
        fi
        return 1
    else
        echo -e "${GREEN}✓${NC} ${var_name}: ${var_description} - 설정됨"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    fi
}

# JWT 시크릿 길이 검증
check_jwt_secret_length() {
    echo
    echo "2. JWT 시크릿 보안 검증..."
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    local jwt_secret=$(grep "^JWT_SECRET=" .env 2>/dev/null | cut -d'=' -f2- | sed 's/^"//' | sed 's/"$//')
    
    if [ ${#jwt_secret} -ge 64 ]; then
        echo -e "${GREEN}✓${NC} JWT_SECRET 길이가 충분합니다 (${#jwt_secret} 바이트)"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}✗${NC} JWT_SECRET 길이가 부족합니다 (${#jwt_secret} 바이트, 최소 64바이트 필요)"
        echo -e "${YELLOW}  보안을 위해 더 긴 시크릿을 사용하세요${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
}

# 메인 검증 로직
main() {
    # .env 파일 존재 확인
    if ! check_env_file; then
        echo
        echo -e "${RED}환경 변수 검증을 계속할 수 없습니다.${NC}"
        exit 1
    fi
    
    echo
    echo "2. 필수 환경 변수 검증..."
    
    # 필수 환경 변수들 검증
    check_env_var "DB_URL" "데이터베이스 연결 URL"
    check_env_var "DB_USERNAME" "데이터베이스 사용자명"
    check_env_var "DB_PASSWORD" "데이터베이스 비밀번호"
    
    check_env_var "JWT_SECRET" "JWT 토큰 시크릿"
    
    check_env_var "AWS_ACCESS_KEY" "AWS 액세스 키"
    check_env_var "AWS_SECRET_KEY" "AWS 시크릿 키"
    check_env_var "S3_BUCKET" "S3 버킷 이름"
    
    check_env_var "GOOGLE_CLIENT_ID" "Google OAuth2 클라이언트 ID"
    check_env_var "GOOGLE_CLIENT_SECRET" "Google OAuth2 클라이언트 시크릿"
    
    check_env_var "KAKAO_CLIENT_ID" "Kakao OAuth2 클라이언트 ID"
    check_env_var "KAKAO_CLIENT_SECRET" "Kakao OAuth2 클라이언트 시크릿"
    check_env_var "KAKAO_ADMIN_KEY" "Kakao API 관리자 키"
    
    check_env_var "PORTONE_API_KEY" "PortOne API 키"
    check_env_var "PORTONE_API_SECRET" "PortOne API 시크릿"
    
    # JWT 시크릿 길이 검증
    check_jwt_secret_length
    
    echo
    echo "3. 선택적 환경 변수 검증..."
    
    # 선택적 환경 변수들 검증
    check_env_var "MAIL_USERNAME" "이메일 사용자명" false
    check_env_var "GOOGLE_MAIL_REFRESH_TOKEN" "Gmail OAuth2 리프레시 토큰" false
    check_env_var "REDIS_PASSWORD" "Redis 비밀번호" false
    
    # 결과 요약
    echo
    echo "=========================================="
    echo "  검증 결과 요약"
    echo "=========================================="
    echo -e "총 검사 항목: ${TOTAL_CHECKS}"
    echo -e "${GREEN}통과: ${PASSED_CHECKS}${NC}"
    echo -e "${RED}실패: ${FAILED_CHECKS}${NC}"
    echo -e "${YELLOW}경고: ${WARNING_CHECKS}${NC}"
    echo
    
    if [ $FAILED_CHECKS -eq 0 ]; then
        echo -e "${GREEN}✓ 모든 필수 환경 변수가 올바르게 설정되었습니다!${NC}"
        echo -e "${GREEN}  Docker Compose를 실행할 수 있습니다.${NC}"
        echo
        echo "실행 명령어:"
        echo "  개발 환경: docker-compose -f docker-compose.yml -f docker-compose.dev.yml up"
        echo "  프로덕션: docker-compose up"
        exit 0
    else
        echo -e "${RED}✗ ${FAILED_CHECKS}개의 필수 환경 변수가 설정되지 않았습니다.${NC}"
        echo -e "${YELLOW}  .env 파일을 수정한 후 다시 실행하세요.${NC}"
        echo
        echo "도움말:"
        echo "  1. .env.example 파일을 참고하여 .env 파일을 수정하세요"
        echo "  2. docs/environment-variables-guide.md 문서를 참고하세요"
        exit 1
    fi
}

# 스크립트 실행
main