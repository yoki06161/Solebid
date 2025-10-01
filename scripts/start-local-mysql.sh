#!/bin/bash

echo "========================================"
echo "Solebid 로컬 MySQL 환경 시작"
echo "========================================"

echo
echo "1. 환경 설정 확인..."
if [ ! -f .env ]; then
    echo ".env 파일이 없습니다. .env.example을 복사합니다..."
    cp .env.example .env
    echo ".env 파일을 생성했습니다. 필요한 값들을 수정해주세요."
    read -p "계속하려면 Enter를 누르세요..."
fi

echo
echo "2. 기존 컨테이너 정리..."
docker-compose down

echo
echo "3. MySQL 데이터 볼륨 확인..."
if docker volume ls | grep -q solebid-mysql-data; then
    echo "기존 MySQL 데이터 볼륨을 사용합니다."
else
    echo "MySQL 데이터 볼륨이 없습니다. 새로 생성됩니다."
fi

echo
echo "4. Docker 컨테이너 시작..."
docker-compose up -d

echo
echo "5. 서비스 상태 확인..."
sleep 10
docker-compose ps

echo
echo "6. MySQL 연결 테스트..."
sleep 20
if docker exec solebid-mysql mysqladmin ping -h localhost -u solebid -psolebidpassword > /dev/null 2>&1; then
    echo
    echo "========================================"
    echo "로컬 MySQL 환경이 성공적으로 시작되었습니다!"
    echo "========================================"
    echo
    echo "서비스 URL:"
    echo "- Frontend: http://localhost:3000"
    echo "- Backend API: http://localhost:8080"
    echo "- MySQL: localhost:3306"
    echo "- Redis: localhost:6379"
    echo
    echo "MySQL 접속 정보:"
    echo "- 호스트: localhost"
    echo "- 포트: 3306"
    echo "- 데이터베이스: solebid"
    echo "- 사용자: solebid"
    echo "- 비밀번호: solebidpassword"
    echo
    echo "로그 확인: docker-compose logs -f"
    echo "중지: docker-compose down"
    echo
else
    echo
    echo "========================================"
    echo "오류: MySQL 연결에 실패했습니다."
    echo "========================================"
    echo
    echo "문제 해결:"
    echo "1. 로그 확인: docker-compose logs mysql"
    echo "2. 포트 충돌 확인: netstat -an | grep 3306"
    echo "3. 컨테이너 재시작: docker-compose restart mysql"
    echo
fi