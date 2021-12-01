# 스프링 배치
***
## 1. 스프링 배치 탄생 배경
### 1. 1 탄생 배경
- 자바 기반 표준 배치 기술의 부재
- Pivotal + Accenture 의 합작품

### 1. 2 배치 핵심 패턴
- Read
    - 데이터 베이스, 파일, 큐에서 다량의 데이터를 조회(추출)한다.
    - DB 모델의 Extract 와 같다.
- Process
    - 특정 방법으로 데이터를 가공한다.
    - DB 모델의 Transform 와 같다.
- Write
    - 데이터를 수정된 양식으로 다시 저장(적재)한다.
    - DB 모델의 Load 와 같다.
- 읽고 -> 필요한 데이터를 가공해서 -> 다시 저장하는 패턴

### 1.3 배치 시나리오
- 배치 프로세스를 주기적으로 커밋
- 동시 다발적인 job 의 배치 처리, 대용량 병렬 처리
- 실패 후 수동 또는 스케줄링에 의한 재시작
- 의존 관계가 있는 step 여러 개를 순차적으로 처리
- 조건적 Flow 구성을 통해 체계적이고 유연한 배치 모델 구성
- 반복, 재시도, skip 처리

### 1.4 아키텍쳐
- 어플리케이션
    - 스프링 배치 프레임워크를 통해 개발자가 만든 모든 배치 job 과 커스텀 코드
- Batch Core
    - Job 을 실행, 모니터링, 관리하는 API
    - JobLauncher, Job, Step, Flow 등이 있다.
- Batch Infrastructure
    - Application, Core 모두 공통의 Infrastructure 위에서 빌드
    - Job 의 실행 흐름과 처리를 위한 틀 제공
    - Reader, Processor Writer, Skip, Retry 등이 속함.

***
## 2. 스프링 배치 시작
### 2. 1 스프링 배치 프로젝트 구성

- 스프링 배치를 사용하기 위해선 @EnableBatchProcessing 을 붙여줘야 한다.
    - 4개의 설정 클래스 실행
    - 배치의 모든 초기화 및 실행 구성
    - 스프링 배치 자동 설정 클래스가 실행되고 빈으로 등록된 모든 job을 검색해 초기화와 동시에 job을 수행하도록 구성

-  스프링 배치 초기화 설정 클래스
    - BatchAutoConfiguration
        - 스프링 배치가 초기화 될 때 자동으로 실행되는 설정 클래스
        - Job을 수행하는 JobLauncherApplicationRunner 빈을 생성
            - 자동으로 배치 job을 수행하는 클래스

    - SimpleBatchConfiguration
        - JobBuilderFactory 와 StepBuilderFactory 생성
        - 스프링 배치의 주요 구성 요소 생성 - 프록시 객체로 생성

    - BatchConfigurerConfiguration
        - BasicBatchConfigurer
            - SimpleBatchConfiguration 에서 생성한 프록시 객체의 실제 대상 객체를 생성하는 설정 클래스
        - JpaBatchConfigurer
            - Jpa 관련 객체를 생성하는 설정 클래스
            - BasicBatchConfigurer 상속 받음
        - **추가로 사용자 정의 BatchConfigurer 인터페이스를 구현해 사용할 수 있음**

    - 실제 수행되는 순서
        - @EnableBatchProcessing
        - SimpleBatchConfiguration ( 프록시 객체 생성)
        - BatchConfigurerConfiguration (실제 객체 생성)
        - BatchAutoConfiguration (JobLauncherApplicationRunner)
		










