# team1-server
<img width="100%" src="https://user-images.githubusercontent.com/60849888/216391327-08f296d4-2b24-4ae2-90e0-674efe09bebd.png"/>


## Build and Run
### 1. Local Development
- Load local mysql database
```
% docker-compose -f docker-compose.local.db.yml up -d   
```
- Set ```-Dspring.profiles.active=local``` in VM options
- Run application
- If you want to test local app with docker image, change 'localhost' to 'host.docker.internal' in application.yml file and  run below command
```
% docker build --build-arg PROFILES=local -t wafflytime-local .
```

### 2. Prod Development
- Set ```-Dspring.profiles.active=prod``` in VM options

## Tech Stack
- Spring Data Jpa
- Spring Security
- Spring Mail
- Spring MVC
- Spring Webflux (for OAuth)
- Spring Cloud
- Redis
- WebSocket
- Aws EC2, ECR, RDS
- Github Action (CI/CD)

## Contribution
### 1. Email Authentication

### 2. Image Control

### 3. SubQuery -> Redis & Coroutine
에타 홈 화면에서 특정 category에 속한(16개) 게시판 별 최근 4개의 게시물을 보여주는 api를 구현해야 한다. 이를 sql 문으로 바꾸면 다음과 같다
```sql
select * from (
    select *, RANK() over (partition by j.board_title order by j.created_at DESC ) as a
    from (select p.*, b.category, b.title as "board_title" from post p left join board b on p.board_id = b.id where b.category in ('BASIC', 'CAREER')) as j
) as f where f.a <=4;
```
서브쿼리 지옥에 빠지게 되고, mysql 특수 함수들이 있어, Querydsl로 구현할 수 없었다. 또한 이렇게 복잡한 쿼리인 경우 유지보수가 어렵기 때문에, 다른 방법의 필요성을 느꼈다.
다음 3가지 과정을 통해 개선된 방법을 찾아나갔다. 각 방법 별 요청 처리 시간을 기록하였다.

#### 3-1. for loop을 통한 각 게시판 마다 4개의 post를 찾는 query 날리기 (Takes 140ms)
- 특정 카테고리에 속한 게시판들을 알기 위한 query 한 개를 날린다
- 각 게시판 id 마다 최근 4개의 게시물을 얻어내는 query를 날린다
#### 3-2. 첫번째 방법 + coroutine (Takes 60ms)
- 한 번의 요청마다 17번의 쿼리를 날려야 하기 때문에 속도가 느리다. 17개의 요청이 네트워크를 타기 때문에 coroutine을 이용하면 더 빨라질 것이라 추측했다
- 코루틴을 적용하니 2배 이상 빨라졌다
```kotlin
boards.forEach {
  future.add(CoroutineScope(Dispatchers.Default).async {
    findLatestPosts(
      boardId = it.id,
      limit = if (it.type.name.startsWith("CUSTOM")) 2 else 4)
   })
}
runBlocking { future.forEach { it.await() } }
```
#### 3-3. Redis 적용 (Takes 30ms)
- 여전히 속도가 느리다고 판단했고, 속도를 높이기 위해 redis를 활용하였다.
- 각 board가 redis의 key가 되고 value는 최근 4개의 게시물을 리스트로가 된다. 특정 카테고리에 해당 하는 게시판에 게시물이 생성, 수정, 삭제 될 때마다 redis를 업데이트 해준다 

#### 3-4. Redis + coroutine (Takes 15ms)
- 3번쨰 방법에 coroutine까지 적용하였다. 우리는 local redis를 쓰기 때문에 coroutine을 사용했을 때 시간 차이가 크게 나지 않았다. 만약 실제 환경같이 remote redis 서버를 사용한다면, coroutine의 효과가 더 커질 것이라 추측한다

**=> 기존에 약 140ms 정도 걸리던 처리 과정이 15ms까지 줄어들었다**

### 4. Real-Time Chat (WebSocket)

### 5. SSE notification

### 6. OAuth Code with Redis
