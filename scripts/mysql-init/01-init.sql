-- Solebid 프로젝트 MySQL 초기화 스크립트
-- 이 스크립트는 Docker MySQL 컨테이너 시작 시 자동으로 실행됩니다.

-- 데이터베이스가 이미 존재하지 않는 경우에만 생성
CREATE DATABASE IF NOT EXISTS solebid CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자가 이미 존재하지 않는 경우에만 생성
CREATE USER IF NOT EXISTS 'solebid'@'%' IDENTIFIED BY 'solebidpassword';

-- 권한 부여
GRANT ALL PRIVILEGES ON solebid.* TO 'solebid'@'%';

-- 권한 적용
FLUSH PRIVILEGES;

-- 데이터베이스 사용
USE solebid;

-- 기본 설정 확인
SELECT 'MySQL 초기화 완료' as status;