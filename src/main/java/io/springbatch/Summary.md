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

- 스프링 배치 초기화 설정 클래스
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

      ### 2.2. 스프링 배치 DB 스키마 생성
- Job 관련 테이블
    - BATCH_JOB_INSTANCE
        - job 이 실행될 때 JobInstance 정보가 저장
        - job_name 과 job_key 를 키로해 하나의 데이터 저장
        - 동일한 job_name 과 job_key 로 저장될 수 없다.

    - BATCH_JOB_EXECUTION
        - job의 실행 정보가 저장되며 job 생성, 시작, 종료 시간, 실행 상태, 메시지 등을 관리

    - BATCH_JOB_EXECUTION_PARAMS
        - job 과 함께 실행되는 JobParameter 정보를 저장

    - BATCH_JOB_EXECUTION_CONTEXT
        - Job 의 실행 동안 여러가지 상태 정보, 공유 데이터를 직렬화 (json 화) 해서 저장
        - Step 간 서로 공유 가능

- Step 관련 테이블
    - BATCH_STEP_EXECUTION
        - step 의 실행 정보가 저장되며 step 생성, 시작, 종료 시간, 실행 상태, 메시지 등을 관리

    - BATCH_STEP_EXECUTION_CONTEXT
        - Step 의 실행 동안 여러가지 상태 정보, 공유 데이터를 직렬화(json 화) 해서 저장
        - Step 별로 저장되며 Step  간 공유 불가


## 3. 스프링 배치 도메인 이해
### 3.1 Job

#### 3.1.1 기본 개념
- 배치 계층 구조에서 가장 상위에 있는 개념
    - 하나의 배치 작업 자체를 의미
- Job Configuration 을 통해 생성되는 객체 단위
    - 배치 작업을 어떻게 구성하고 실행할 것인지 전체적으로 설정하고 명세해 놓은 객체
- 배치 Job 을 구성하기 위한 최상위 인터페이스
    - 스프링 배치가 기본 구현체 제공
- 여러 Step을 포함하고 있는 컨테이너로 반드시 한개 이상의 Step 을 포함해야 한다.

#### 3.1.2 기본 구현체
- SimpleJob
    - 순차적으로 Step 을 실행시키는 Job
    - 모든 Job 에서 사용할 수 있는 표준 기능
- FlowJob
    - 특정 조건과 흐름에 따라 Step을 구성하여 실행
    - Flow 객체를 실행시켜 작업을 진행


- Job - 최상위 인터페이스
    - AbstractJob - Job 을 상속받는 추상 클래스, 다양한 필드 값 존재
        - SimpleJob
        - FlowJob
        - AbstractJob 을 상속 받는 2개 클래스












