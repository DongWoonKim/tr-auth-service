## 🔑 tr-auth-service (인증 서비스)

`tr-auth-service`는 **사용자 인증(Auth)**을 담당하는 서비스로,  
회원가입, 로그인, 토큰 발급 및 검증과 같은 핵심 인증 기능을 제공합니다.  

이 서비스는 사용자 관리 및 인증의 **중앙 진입점(Central Authority)** 역할을 하며,  
각 서비스가 직접 인증 로직을 구현하지 않고 `tr-auth-service`를 통해 **일관된 인증·인가 흐름**을 보장받습니다.  

이를 통해 사용자는 **회원가입 / 로그인**, **Access Token & Refresh Token 발급**,  
**토큰 재발급 및 유효성 검증** 기능을 안정적으로 이용할 수 있습니다.

---

### 🔑 주요 특징 (tr-auth-service)

- **JWT 기반 인증**
  - Access Token과 Refresh Token을 발급하여 **Stateless 아키텍처**를 유지합니다.  
  - 서버에 세션 상태를 저장하지 않음으로써 확장성과 성능을 보장합니다.  

- **회원가입/로그인 API 제공**
  - 신규 사용자는 회원가입 API를 통해 계정을 생성할 수 있습니다.  
  - 로그인 시 토큰이 발급되며, 이후 서비스 접근 시 인증 수단으로 사용됩니다.  

- **토큰 재발급 및 유효성 검증**
  - Refresh Token을 활용하여 Access Token을 재발급합니다.  
  - 토큰의 유효성을 검증하여 만료/위조된 토큰 사용을 차단합니다.  

- **중앙 집중식 인증 관리**
  - 개별 서비스가 인증 로직을 중복 구현하지 않고, Auth Service를 통해 일관된 인증/인가 흐름을 적용할 수 있습니다.

### ⚙️ 내부 아키텍처

```bash
com.trevari.auth
├─ config
│  └─ OpenApiConfig.java
│
├─ domain
│  ├─ user
│  │  ├─ Role.java
│  │  ├─ User.java
│  │  └─ UserRepository.java
│  └─ exception
│     └─ ErrorResponse.java
│
├─ infrastructure
│  ├─ persistence
│  │  ├─ UserEntity.java
│  │  ├─ UserJpaRepository.java
│  │  └─ UserRepositoryAdapter.java
│  └─ security
│     ├─ CustomUserDetails.java
│     ├─ JwtProperties.java
│     ├─ SecurityConfig.java
│     ├─ TokenProvider.java
│     └─ UserDetailsServiceImpl.java
│
├─ interfaces
│  ├─ dto
│  │  ├─ AuthLoginRequestDTO.java
│  │  ├─ AuthLoginResponseDTO.java
│  │  ├─ ReissueTokenRequestDTO.java
│  │  ├─ ReissueTokenResponseDTO.java
│  │  ├─ UserJoinRequestDTO.java
│  │  ├─ UserJoinResponseDTO.java
│  │  ├─ ValidTokenRequestDTO.java
│  │  └─ ValidTokenResponseDTO.java
│  ├─ http
│  │  ├─ AuthController.java
│  │  └─ UserController.java
│  └─ mapper
│     └─ UserMapper.java
│
└─ TrAuthServiceApplication.java
```

- **application package** : 위의 아키텍처에서 `application` 패키지의 하위 항목들은  
  인증/인가와 관련된 **비즈니스 로직**을 담는 서비스가 위치합니다.  
  예: 로그인, 회원가입, 토큰 재발급, 토큰 검증 등 핵심 로직.

- **domain** : `auth-service`의 순수 도메인 모델을 정의하는 영역입니다.  
  - `User`, `Role`, `UserRepository` 등이 포함되어 있으며,  
    `UserRepository`는 인프라 기술(JPA 등)에 의존하지 않는 **순수 POJO 인터페이스**로 작성되었습니다.  
  - 비즈니스 로직은 이 순수 도메인 모델에 의존하도록 설계되어, 외부 기술이나 환경 변화로부터 영향을 최소화합니다.

- **infrastructure/persistence** : DB와 직접 맞닿는 영역으로, 도메인 모델과 영속성 기술(JPA 등)을 연결합니다.  
  - `UserRepositoryAdapter` : domain 계층의 **UserRepository 인터페이스(포트)**를 구현한 어댑터입니다.  
    내부적으로 `UserJpaRepository`를 사용하지만, 도메인 계층은 오직 `UserRepository` 인터페이스만 바라봅니다.  
    -> 덕분에 JPA가 아닌 다른 기술(MyBatis, MongoDB 등)로 교체해도 도메인 로직은 수정할 필요가 없습니다.  

- **infrastructure/security** : 인증/인가와 관련된 보안 설정과 JWT 관련 컴포넌트가 위치합니다.  
  - `TokenProvider` : JWT 토큰 생성/검증을 담당합니다.  
  - `JwtProperties` : 토큰 관련 설정(비밀키, 만료시간 등)을 관리합니다.  
  - `CustomUserDetails` / `UserDetailsServiceImpl` : Spring Security의 인증 흐름과 연결되는 사용자 정보 제공 로직입니다.  
  - `SecurityConfig` : Spring Security 전반적인 보안 설정을 정의합니다.  

- **interfaces** : 외부와의 입출력 경계를 담당하는 영역입니다.  
  - `dto` : 로그인/회원가입/토큰 재발급/토큰 검증 등 요청·응답을 표현하는 DTO들이 포함됩니다.  
    -> 서비스 내부 도메인을 그대로 노출하지 않고, 안전하게 외부와 데이터를 주고받도록 합니다.  
  - `http` : 컨트롤러(API 엔드포인트)가 위치하며, DTO를 통해 외부 요청을 받아 서비스 계층으로 전달합니다.  
  - `mapper/UserMapper` : 도메인 모델(User) ↔ DTO 간 변환을 담당합니다.  
    -> 변환 로직을 별도로 분리하여, 컨트롤러나 서비스 코드의 복잡도를 줄입니다.

### 📌 고민 사항
1. 내부 아키텍처 설계 : 이부분은 tr-auth-infra에서 기재하였으므로 생략하겠습니다.
2. 리프래시 토큰 관리 : 이부분을 어떻게 관리할지 살짝 고민이 되었습니다. 
  대안1 : 별도의 레디스(TTL)나 DB 등 으로 관리
  대안2 : 클라이언트 쿠키로 관리  
✔️ 대안2를 선택 : 마찬가지로 서비스를 구동하기 위해 발생하는 리스크를 최대한 줄이기 위해 대안2를 적용하였습니다.

### 🔖 테스트 커버리지
 - 테스트 코드 비즈니스 계층(application/**)만 적용 : 일정적인 부분에 있어서 조금 빠듯해서 비즈스로직만 적용하였습니다 외부 입출력 영역은 web-service를 통해 확인할 수 있는 부분이기에 넣지 않았습니다.
 - catalog service 테스트 커버리지 결과
<img width="1097" height="132" alt="스크린샷 2025-08-22 오전 11 39 58" src="https://github.com/user-attachments/assets/830b7eee-cabb-4d88-8caa-e89e2ca99d76" />



