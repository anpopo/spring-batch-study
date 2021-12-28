# 스프링 배치 실행
## 1. 배치 초기화 설정
### 1.1. JobLauncherApplicationRunner
- 스프링 배치 작업을 시작하는 ApplicationRunner -> BatchAutoConfiguration 에서 생성됨
- 스프링 부트에서 제공하는 ApplicationRunner 의 구현체
  - -> 어플리케이션이 정상 구동 되자마자 실행됨
- 기본적으로 빈으로 등록된 모든 JOB 을 실행 시킴
  - -> 스프링 부트 시작시 스캔

### 1.2. BatchProperties
- Spring Batch 의 환경 설정 클래스
- application.properties / yml 에 설정 등록
```yaml
batch:
  job:
    names: ${job.name:NONE}  # 하드 코딩을 해도 상관 없음
    initialize-schema: NEVER
    tablePrefix: SYSTEM
    enable: false  # 어플리케이션 실행 후 자동으로 Batch Job 실행 설정
 ```

### 1.3. JOB 실행 옵션
- 지정한 Batch Job 만 실행하도록 할 수 있음
- spring.batch.job.names: ${job.name:NONE}
- 어플리케이션 실행시 Program Arguments 로 job 과 JobParameter 입력
  - --job.name=hellojob user=user1
  - --job.name=hellojob,simplejob username=name1
  - 띄어쓰기로 각 argument 를 구분
  - 하나의 job.name 은 쉼표(,)로 구분
    - 만약 JobBuilderFactory 에 등록된 이름과 properties 에 등록된 이름과 다른 경우 job 이 skip



## 2. Job 과 Step
- 도메인적인 관계보단 실행적인 측면에서의 구성과 메소드 실행 등에 대해 알아본다.
### 2.1 JobBuilderFactory
- 스프링 배치는 Job 과 Step 을 쉽게 생성 및 설정할 수 있도록 util 성격의 빌더 클래스 제공
- JobBuilderFactory
  - JobBuilder 를 제공하는 Factory 클래스
  - jobBuilderFactory.get("jobName")
    - jobName 은 스프링 배치가 Job 을 실행시킬 때 참조하는 Job 의 이름
    - job 의 이름은 db 에도 저장된다.
- JobBuilder
  - job 을 구성하는 설정 조건에 따라 두 개의 하위 빌더 클래스를 생성하고 실제 Job 생성 위임
  - SimpleJobBuilder
    - SimpleJob 생성
    - Job 실행과 관련된 여러 설정 api 제공
  - FlowJobBuilder
    - FlowJob 생성
    - Flow 실행과 관련된 여러 설정 api 제공