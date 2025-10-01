# Solebid

## 프로젝트 소개
Solebid는 희소성과 수집 가치를 중시하는 스니커즈를 경매 방식으로 거래할 수 있는 이커머스 서비스입니다. 실시간 경매 진행, 포인트 기반 결제, 위시리스트와 장바구니, 사용자 프로필 관리 등 주요 커머스 플로우를 React 기반 프런트엔드에서 제공합니다. 본 문서는 프런트엔드 개발자가 빠르게 온보딩하고 일관된 개발 경험을 유지할 수 있도록 핵심 구조와 운영 방법을 정리합니다.

## 기술 스택
- React 19, TypeScript, Vite 7
- Tailwind CSS 4 (with @tailwindcss/typography)
- TanStack Query 5 (서버 상태 및 캐싱)
- React Router 7, React Hook Form, React Toastify
- date-fns, PortOne JavaScript SDK (포인트 충전 결제)
- ESLint + TypeScript ESLint, npm (package-lock.json 기반)

## 설치 및 실행 방법
### 사전 요구 사항
- Node.js 20 LTS 이상 권장
- npm 10 이상 (프로젝트 루트 `frontend/`에서 실행)
- Docker & Docker Compose (로컬 MySQL 환경용)

### 로컬 MySQL 환경 (권장)
프로젝트가 AWS RDS에서 로컬 MySQL로 변경되었습니다. Docker를 사용하여 쉽게 설정할 수 있습니다.

```bash
# 빠른 시작 (Windows)
scripts\start-local-mysql.bat

# 빠른 시작 (Linux/Mac)
./scripts/start-local-mysql.sh

# 수동 실행
docker-compose up -d
```

**서비스 URL:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- MySQL: localhost:3306
- Redis: localhost:6379

**MySQL 접속 정보:**
- 데이터베이스: solebid
- 사용자: solebid
- 비밀번호: solebidpassword

자세한 설정 방법은 [로컬 MySQL 설정 가이드](docs/local-mysql-setup.md)를 참고하세요.

### 로컬 개발 서버 (프론트엔드만)
```bash
cd frontend
npm install
npm run dev
```
- 기본 개발 서버 포트는 `http://localhost:5173`
- 백엔드 API 프록시는 `vite.config.ts`의 설정을 참고해 환경에 맞게 조정합니다.

### 빌드 및 기타 스크립트
```bash
npm run build    # 프로덕션 번들 생성
npm run preview  # 로컬에서 번들 미리보기
npm run lint     # ESLint 규칙 검사
npm run feature  # scripts/create_feature.sh 실행 (기능 스캐폴딩)
```

## 프로젝트 구조
```
frontend/
|-- docs/
|   |-- AUTH_GUIDE.md
|   |-- PROJECT_STRUCTURE.md
|   `-- readme.md
|-- public/
|   `-- index.html 외 정적 자원
|-- scripts/
|   `-- create_feature.sh (기능 폴더 템플릿 생성)
|-- src/
|   |-- assets/              # 이미지, 아이콘 등 정적 리소스
|   |-- components/          # 전역 공용 UI 컴포넌트 및 레이아웃
|   |-- constants/           # 전역 상수 (환경 변수, 스타일 등)
|   |-- context/             # Toast, Modal, Notification 등 전역 상태 컨텍스트
|   |-- features/            # 도메인 단위 기능 모듈 (auction, user, payment 등)
|   |-- hooks/               # 재사용 가능한 커스텀 훅
|   |-- router.tsx           # 라우트 구성
|   |-- types/               # 전역 타입 정의
|   `-- utils/               # HTTP 래퍼, 공통 유틸리티
|-- tests/                   # 통합/엔드투엔드 테스트용 폴더
|-- package.json
|-- package-lock.json
|-- tailwind.config.js
`-- vite.config.ts
```

## 주요 기능
- **메인/카테고리 홈**: 경매 진행 중인 상품 카드, 주요 브랜드, 카테고리 진입점을 제공하는 랜딩 페이지(`MainPage`).
- **경매 상세 & 실시간 스트림**: `/auction/:auctionId`에서 SSE(`/api/auctions/{id}/stream`) 기반 실시간 입찰 정보 갱신, 입찰 액션 및 타이머 연장 표시.
- **상품 등록 및 이미지 업로드**: `/products/new`에서 S3 프리사인 URL 발급(`/api/uploads/presign`, `/api/uploads/download-urls`)과 업로드를 지원.
- **장바구니 · 위시리스트 · 주문**: 보호된 라우트(`/cart`, `/wish`, `/order`)로 장바구니 조회(`/api/cart`), 낙찰 주문 생성(`/api/orders`) 및 주문 상세 열람.
- **결제 & 포인트 관리**: PortOne 연동 포인트 충전(`/api/payments/charge/prepare`, `/api/portone/approve`), 결제 내역 조회(`/api/payments/records`), 사용자 포인트 요약(`/api/users/me/points`).
- **회원 인증 & 프로필**: 이메일/소셜 로그인, 닉네임 설정, 비밀번호 재설정, 프로필 정보 변경, 회원 온도/통계 조회(`/api/users/me`, `/api/users/profile`, `/api/users/password`).
- **검색 및 알림**: 키워드 기반 상품 검색(`/api/products/search`)과 개인 알림 스트림 토큰 발급(`/api/stream/token`), 알림 센터 제공.

## API 문서 (프런트엔드 연동 기준)
### 인증 & 사용자
| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| POST | /api/users/signup | 회원가입 | 이메일, 닉네임, 비밀번호 입력 |
| POST | /api/users/login | 이메일/비밀번호 로그인 | `credentials: include` |
| POST | /api/users/reactivate | 휴면 계정 복구 | 토큰 기반 |
| GET | /api/users/me | 내 프로필 조회 | 기본 프로필 + 온도 |
| GET | /api/users/me/points | 포인트 요약 | 잔액, 누적 내역 |
| POST | /api/users/nickname | 닉네임 업데이트 | 중복 검사 선행 필요 |
| GET | /api/users/nickname/available | 닉네임 중복 확인 | `nickname` 쿼리 |
| PUT | /api/users/profile | 일반 프로필 수정 | 이름, 닉네임 등 |
| PUT | /api/users/profile/sensitive | 민감 정보 수정 | 이메일, 전화번호 등 |
| PUT | /api/users/password | 비밀번호 변경 | 현재/새 비밀번호 검증 |
| POST | /api/auth/verify-code | 이메일 인증 코드 검증 | 일반 인증 |
| POST | /api/auth/verify-signup-code | 회원가입 인증 코드 검증 | 가입 프로세스 |
| POST | /api/auth/send-verification | 인증 메일 발송 | 최초 발송 |
| POST | /api/auth/resend-verification | 인증 메일 재발송 | |
| POST | /api/auth/password/request-reset | 비밀번호 재설정 OTP 발급 | 이메일 입력 |
| POST | /api/auth/password/verify-otp | OTP 검증 | |
| POST | /api/auth/password/verify-and-reset | OTP 검증 + 비밀번호 재설정 | |
| POST | /api/auth/password/resend-otp | OTP 재발송 | |
| GET | /api/auth/oauth2/{provider}/url | 소셜 로그인 URL 요청 | provider: kakao, google 등 |
| POST | /api/auth/oauth2/{provider}/callback | 소셜 로그인 콜백 처리 | code/state 전달 |
| POST | /api/auth/refresh | 토큰 갱신 | 쿠키 기반 |
| GET | /api/auth/status | 토큰 유효성 점검 | 상태 및 만료 정보 |
| POST | /api/auth/logout | 로그아웃 | 전 세션 무효화 |

### 경매 & 상품
| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| GET | /api/auction-events/cards | 메인 경매 카드 조회 | `limit` 쿼리 지원 |
| GET | /api/auctions/{id} | 경매 상세 정보 | 입찰/남은시간 포함 |
| POST | /api/auctions/{id}/bids | 경매 입찰 | `X-Idempotent-Key` 헤더 필요 |
| POST | /api/auctions | 신규 경매 생성 | 상품 정보 + 스케줄 |
| GET | /api/auctions/{id}/stream | 경매 SSE 스트림 | bid, extended, status 이벤트 |
| POST | /api/stream/token | 개인 알림 스트림 토큰 발급 | SSE 연결 전 요청 |
| POST | /api/uploads/presign | 이미지 업로드용 프리사인 URL 발급 | S3 Put URL 반환 |
| POST | /api/uploads/download-urls | 업로드한 이미지 접근 URL 조회 | key 배열 입력 |
| PUT | (S3 putUrl) | 프리사인 URL로 이미지 업로드 | 백엔드 통제 밖 |

### 주문 · 결제 · 포인트
| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| POST | /api/orders | 낙찰 상품 주문 생성 | 배송지 포함 |
| GET | /api/orders/winnings | 내 낙찰 주문 목록 | |
| GET | /api/orders/{orderId} | 주문 상세 | 배송, 결제 상태 |
| GET | /api/cart | 장바구니 조회 | 성공 여부 확인 필요 |
| GET | /api/bids/winning | 내 낙찰 이력 | 프로필 > 낙찰 탭 |
| GET | /api/bids/selling | 내 판매 이력 | 프로필 > 판매 탭 |
| POST | /api/payments/charge/prepare | 포인트 충전 사전 승인 | orderId 반환 |
| GET | /api/portone/approve | PortOne 결제 승인 | `impUid` 쿼리 |
| GET | /api/payments/records | 결제 내역 페이지 | page/size/status 등 쿼리 |

### 검색 · 기타
| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| GET | /api/products/search | 상품 키워드 검색 | `keyword` 쿼리 |
| POST | /api/uploads/presign | 업로드 프리사인 | 업로드 기능과 공유 |
| PUT | /api/users/password | 프로필 보안 변경 | 위 테이블과 중복, 재기재 |

