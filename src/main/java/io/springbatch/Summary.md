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


### 3.2 JobInstance
- 배치 단계, 처리, 결과, 구성하는 도메인들
    - Job, Step, Flow, Tasklet
- 위의 도메인들이 실행될때 실행되는 정보들, 상태정보를 단계마다 데이터 베이스에 저장하는 용도로 생성되는 도메인들 - 메타데이터 저장용 도메인
    - JobInstance, JobExecution, StepExecution 등

#### 3.2.1 기본개념
- Job 이 실행될 때 생성되는 Job의 논리적 실행 단위 객체
    - 고유하게 식별 가능한 작업 실행을 나타냄
- Job 의 설정과 구성은 동일
    - Job 이 실행되는 시점에 처리하는 내용이 다르기 때문에 Job 을 구분해야 함
- JobInstance 생성 및 실행
    - 처음 시작하는 Job + JobParameter 의 경우
        - 새로운 JobInstance 생성
    - 이전과 동일한 Job + JobParameter 의 경우
        - 이미 존재하는 JobInstance 리턴
        - 기존 Job 이 있는 경우 같은 예외가 발생함.
        - 이미 한번 실행된 Job 이기때문.
- JobInstance 는 BATCH_JOB_INSTANCE 테이블과 매핑

### 3.3 JobParameter
#### 3.3.1 기본 개념
- Job 실행시 함께 포함되어 사용되는 파라미터
- 하나의 Job에 존재할 수 있는 여러개의 JobInstance 를 구분하기 위한 용도
- JobParameter 와 JobInstance 는 1:1

#### 3.3.2 생성 및 바인딩
1. 어플리케이션 실행시 주입
    - java -jar spring batch name=sehyeong seq(long)=2L date(date)=2021/01/08 age(double)=16.5
    - 해당 타입에 맞게 괄호 안에 타입을 넣어 주어야 한다
2. 코드로 생성
    - JobParameterBuilder, DefaultJobParametersCoverter
3. SpEL 이용
    - @Value("#{jobParameter[requestDate]}"), @JobScope, @StepScope 선언 필수

#### 3.3.3 BATCH_JOB_EXECUTION_PARAM 테이블과 매핑
- JOB_EXECUTION 과 1: m 관계


### 3.4 JobExecution
#### 3.4.1 기본 개념
- JobInstance 에 대한 한번의 시도를 의미하는 객체
- Job 실행중 발생한 정보를 저장하고 있는 객체

- JobInstance 와의 관계
    - JobExecution 은 FAILED / COMPLETE 등 의 실행 결과를 가지고 있음
    - Complete 일 경우 JobInstance 의 실행이 완료된 것으로 간주 / 재 실행 불가
    - FAILED 의 경우 재 실행이 가능
        - 같은 값의 JobParameter 를 통해 실행 가능
    - JobExecution 의 실행 결과가 COMPLETE 가 될 때까지 하나의 JobInstance 내에서 여러 번의 시도가 생길 수 있음
- JobInstance 와 JobExecution 은 1:M 의 관계로 JobInstance 에 대한 성공 / 실패 내역을 가지고 있음


### 3.5 Step
#### 3.5.1 기본 개념
- Batch Job 을 구성하는 독립적인 단계
- 실제 배치 처리를 정의하고 컨트롤 하는데 필요한 정보를 담고있음
- 단순한 태스크 부터 복잡한 비지니스 로직까지 처리할 수 있는 설정을 담고 있다.
- Job 의 세부 작업을 태스크 기반으로 설정하고 명세해 놓은 객체
- 모든 Job 은 하나 이상의 Step 으로 구성

#### 3.5.2 구성
- TaskletStep
    - 기본이 되는 Step 클래스로 Tasklet 타입의 구현체 제어
- PartitionStep
    - 멀티 스레드 방식의 Step 분리 실행
- JobStep
    - Step 내부에 Job 구성
- FlowStep
    - Step 내부에 Flow 구성

#### 3.5.3. 설계 이해
- 최상단 인터페이스인 Step
    - execute 라는 메소드 존재
        - StepExecution 을 argument 로 받음
        - Step 실행에 대한 정보를 저장
- Step 을 구현한 추상 클래스 AbstractStep
- 추상 클래스를 상속받는 4개의 Step
    - TaskletStep
    - PartitionStep
    - JobStep
    - FlowStep
    

### 3.6 지금까지를 바탕으로 정리해본 BatchJob 실행 내부 구조
1. Configuration 에서 job / step 구성
2. JobBuilderFactory / StepBuilderFactory 클래스를 이용해 Job / Step 타입의 객체를 생성
3. SimpleJobBuilder 클래스에서 실질적으로 Job 을 생성하고 생성된 Step 을 SimpleJobBuilder 내부의 steps 필드에 리스트 저장
4. JobLauncherApplicationRunner 에서 Job 실행함
    1. executeLocalJobs 메소드  -> jobs 에 iterator 를 돌면서 다음 job 이 있으면 하나씩 execute 메소드 실행
    2. execute 메소드에서 JobParameter 를 가져온 후 JobLauncher 의 run 으로 실질적으로 Job 을 수행함
        1. JobLauncher 는 인터페이스, 구현체는 SimpleJobLauncher 를 사용
5. AbstractJob 은 추상 클래스이고 Job 을 구현하고 있음
    1. AbstractJob 의 execute 메소드에서 doExecute 메소드 호출
    2. doExecute 메소드는 추상 메소드로 자식에서 실질적인 구현이 필요
    3. FlowJob / SimpleJob 에 따라 다른 기능
6. Configuration 에서 생성한 Job 은 SimpleJob 으로 구성했기 때문에 SimpleJob 의 doExecution 메소드가 실행됨
    1. StepHandler 에서 step 의 실행을 처리
    2. 이것도 부모의 AbstractJob 에 있는 handleStep 메소드를 호출하고
    3. StepHandler 인터페이스를 구현한 SimpleStepHandler 에 있는 handleStep 이 호출됨
    4. 결국 처리는 SimpleStepHandler 의 handleStep 메소드
7. SimpleJob 에 있는 steps (List<Step>) 에서 각각의 step 을 가져와 execute 메소드 호출
    1. execute 메소드 호출은 AbstractStep 의 execute 메소드로 호출 되어짐
    2. execute 메소드에서 doExecute 메소드를 호출함
        1. job 과 마찬가지로 우리가 Configuration 에서 구성한 Step 의 종류에 따라 (TaskletStep, PartitionStep, JobStep, FlowStep) doExecute 메소드가 호출됨
8. step 내부에 구성된 tasklet 을 execute 함 ( 이부분 좀더 서칭 필요 )
    1. TaskletStep 내부 구성된 ChunkTransactionCallBack 클래스에서 TransactionCallBack 인터페이스를 구현하고 있음
    2. TransactionCallBack 인터페이스 내부 doInTransaction 메소드를 ChunkTransactionCallBack 클래스가 구현
    3. 하나의 트랜젝션 안에서 Tasklet 의 execute 메소드 호출
    4. Configuration 에서 Step 을 구성할 때 등록한 Tasklet 의 execute 메소드 호출크.... 구조...


### 3.6 StepExecution
#### 3.6.1 기본 개념
- Step 에 대한 한번의 시도를 의미하는 객체
- Step 실행 중 발생한 정보를 저장하고 있는 객체
- Job 을 재 시작 하더라도 이미 성공적으로 완료된 Step 은 재실행 되지 않음
    - 이미 성공한 Step 도 재시작이 가능한 설정 옵션이 있음.
- 실제 Step 이 실행되는 시점에 StepExecution 을 생성한다.
- JobExecution 과의 관계
    - StepExecution 이 모두 정상적으로 완료 되어야지 JobExecution 이 완료된다.
    - StepExecution 이 하나라도 실패시 JobExecution 은 실패
- JobExecution 과 StepExecution 과는 1:M 관계


### 3.7 StepContributioin
#### 3.7.1 기본 개념
- 청크 프로세스 (덩어리 단위로 실행되어지는 묶음들로 이해하자) 의 변경 사항을 버퍼링한 후 StepExecution 상태를 업데이트 하는 도메인 객체
- 청크 단위 프로세스 커밋 직전 StepExecution 의 apply 메소드를 호출해 상태를 업데이트 함
- ExitStatus 기본 종료 코드 외 사용자 정의 종료 코드를 생성해 적용할 수 있음


- TaskletStep 에서 시작되는 flow
    - TaskletStep 내부 ChunkTransactionCallBack 클래스를 생성하면서 StepExecution 할당
    - StepExecution 이 StepContribution  생성
    - tasklet 의 execute 메소드 실행
        - 사용자가 Configuration 에 직접 구성한 Tasklet
    - Tasklet 에서 커밋이 발생하기 전 stepExecution 의 apply 메소드 실행
        - apply 메소드의 파라미터로 넘어가는 stepContribution 에 있는 각각의 값들을 step execution 에 적용시키는 과정





