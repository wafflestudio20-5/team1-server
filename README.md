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

## API Document
https://foregoing-venom-883.notion.site/WafflyTime-API-b6aaf2b096924d2fbb251c443d66ba15

## Tech Stack
- Spring Data Jpa
- Spring Security
- Spring Mail
- Spring MVC
- Spring Webflux (for OAuth)
- Spring Cloud
- Redis
- WebSocket
- Aws EC2, ECR, RDS, S3
- Github Action (CI/CD)

## Contributions
### 1. Email Authentication
에타는 해당 대학교 메일을 갖고 있는 유저만 가입할 수 있기 때문에, 메일 인증이 필요하다. 메일 인증을 위해 Spring Mail과 redis를 사용하였다.
- 유저의 SNU 메일을 request body로 받는다.
- 랜덤 코드를 생성해 유저의 메일로 코드를 전송한다
- 랜덤 코드를 redis에 저장하고, 유효시간은 3분+a 로 설정한다
- 유저는 받은 코드를 3분 안에 인증 확인 화면에 코드를 입력한다
- 클라이언트는 유저가 적은 코드를 서버로 전송해주고, 서버는 코드를 redis에서 꺼내와 일치하는지 비교한다

### 2. Image by Presigned URL
유저 프로필 이미지, 게시물 사진들을 aws S3에 저장하고, s3 url를 db 엔티티에 기록한다. 직접적인 s3 url이 외부로 유출 되는 것을 막고, 사진을 업로드하고 다운로드 하는 과정을 서버가 아닌 클라이언트에게 부담하기 위해 aws S3에서 제공하는 Presiged URL을 사용했다. 이미지 업로드, 다운로드를 서버가 책임지면 서버에 큰 부하가 걸리기 떄문에 각각의 클라이언트가 처리한다.
- 클라이언트는 이미지 파일 이름을 서버로 전송한다
- 서버는 이미지의 유효시간이 설정된 s3 presiged url을 생성해 클라이언트에게 전송해준다. 실제 s3 url은 db entity로 저장한다
- 클라이언트는 response로 받은 presigned url로 이미지를 업로드한다
- 이미지를 GET 해야 하는 경우, 서버는 db enttiy에 기록된 s3 url을 통해 presigned url을 생성해 클라이언트에게 전달한다. 클라이언트는 presigend url을 통해 이미지를 get 해온다.
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


|       | 3-1   | 3-2  | 3-3  | 3-4  |
|-------|-------|------|------|------|
| Takes | 140ms | 60ms | 30ms | 15ms | 

#### 3-1. For loop을 통한 각 게시판 마다 4개의 post를 찾는 query 날리기
- 특정 카테고리에 속한 게시판들을 알기 위한 query 한 개를 날린다
- 각 게시판 id 마다 최근 4개의 게시물을 얻어내는 query를 날린다
#### 3-2. 첫번째 방법 + coroutine
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
#### 3-3. Redis 적용
- 여전히 속도가 느리다고 판단했고, 속도를 높이기 위해 redis를 활용하였다.
- 각 board가 redis의 key가 되고 value는 최근 4개의 게시물을 리스트가 된다. 특정 카테고리에 해당 하는 게시판에 게시물이 생성, 수정, 삭제 될 때마다 redis를 업데이트 해준다 

#### 3-4. Redis + coroutine
- 3번쨰 방법에 coroutine까지 적용하였다. 우리는 local redis를 쓰기 때문에 coroutine을 사용했을 때 시간 차이가 크게 나지 않았다. 만약 실제 환경같이 remote redis 서버를 사용한다면, coroutine의 효과가 더 커질 것이라 추측한다

**=> 기존에 약 140ms 정도 걸리던 처리 과정이 15ms까지 줄어들었다**

### 4. Real-Time Chat (WebSocket)
Spring boot에서 제공하는 STOMP를 sub protocol로 사용해 구현하려고 했지만\
(1) 테스트 하기 어렵고 커스텀에 관한 정보가 부족했고\
(2) 1:1 채팅만이 성립하는 에타 쪽지에서 publish-subscribe 모델까지는 필요하지 않아서\
`TextWebSocketHandler`와 `HttpSessionHandshakeInterceptor`를 상속받은 구현체를 만들어서 구현했다.

한 유저당 서버측에서 유지하는 웹소켓 세션은 하나로 한정했고,\
유저 웹소켓 세션이 존재하면 (즉, 쪽지 페이지에 유저가 접속중이면) 웹소켓을 통해 db에 작성된 쪽지 정보를 보내주고,\
아니라면 SSE 알림을 보내 유저가 실시간으로 쪽지 수신 여부를 알 수 있게 했다.

메세지는 아래의 간단한 sub protocol을 통해 주고 받는다.
- 모든 메세지 형식은 json 형식을 따른다.
- 프론트 -> 서버 메세지 형식은 유저가 송신하는 메세지 정보, 한 종류이다.
```json
{
  "chatId": Long, // 쪽지를 보내는 채팅방 db id
  "contents": String // 쪽지 내용
}
```
- 서버 -> 프론트 메세지 형식은 전송/수신한 쪽지 정보인 `MESSAGE` 타입, rest api 요청을 통해 생성된 새 채팅방 정보인 `NEWCHAT` 타입, 그리고 한정된 상황에서 db 데이터에 변화가 있어 추가적인 rest api 요청을 통해 정보 최신화 할 것을 요청하는 `NEED_UPDATE` 타입, 3 종류이다.
```json
{
  "chatId": Long, // 쪽지가 생성된 채팅방 db id
  "messageId": Long, // 생성된 쪽지의 db id
  "sentAt": { // 쪽지 작성 시각
    "year": Int,
    "month": Int,
    "day": Int,
    "hour": Int,
    "minute": Int
  },
  "received": Boolean, // true이면 받은 메세지, false이면 보낸 메세지
  "contents": String, // 쪽지 내용
  "type": "MESSAGE"
}
```
```json
{
  "chatId": Long, // 생성된 채팅방 db id
  "target": String, // 채팅 상대 이름
  "type": "NEWCHAT"
}
```
```json
{
  "chatId": [Long, ...],
  "unread": [Int, ...],
  "type": "NEED_UPDATE"
}
```

### 5. SSE notification
현재 에타 웹에서는 자신이 작성한 게시물에 댓글이 달리는 경우, 알림이 오지만 이 알림은 새로고침을 해야만 확인할 수 있다. SSE(Server-Sent-Event)를 이용하여 유저가 화면을 보고 있는 중에는 알림이 뜰 수 있게 구현 하였다. (TODO: 프론트가 구현 완료되면 추후 알림 뜨는 사진 첨부)

### 6. OAuth Code with Redis
소셜 로그인/회원 가입은 클라이언트에서 받아온 OAuth Authorization Code로 서버에서 access token을 받아온 후 로그인/회원가입 처리를 진행한다.
기존 계정이 없어서 로그인 실패하는 경우 회원가입을 진행하려 하였으나, OAuth Authorization code를 재사용 할 수 없어 회원가입이 실패하는 문제가 있었다.

#### 6-1. 임시 닉네임 적용
소셜 로그인 실패시 임시 닉네임을 생성하여 가입시키는 방법으로 해결을 시도하였다.  
이 경우 임시 닉네임이 다른 닉네임과 중복되면 안되고, 닉네임이 임시 닉네임이면 게시글/댓글을 못 쓰게 따로 설정해야하는 등 추가로 고려해야 할 요소가 많아지는 문제가 있다.

#### 6-2. Redis 적용
- 소셜 로그인 실패시 클라이언트로부터 받아온 OAuth Authorization Code를 key로, OAuth Authorization 측에서 얻어온 social email를 value로 하여 redis에 저장해두었다.
- 회원가입 처리를 해야하는 경우, redis에서 OAuth Authorization Code에 해당하는 social email을 찾고, 닉네임을 받아서 최종적인 회원가입 처리를 진행하였다.  
- 한번 사용한 OAuth Authorization Code는 더 사용할 일이 없으므로 바로 삭제 시켰다.
- 회원가입을 진행하다 말고 종료하는 유저들의 데이터를 계속 쌓아둘 수 없기에, 저장된지 10분이 지난 데이터는 삭제시켰다.

### 7. Cursor, Double-Cursor Pagination
게시판 내 게시물을 열람할 때, 에타 웹에서는 이전/다음을 통해 이동하는 페이지 방식이 사용되고, 앱에서는 무한스크롤 방식이 사용된다.\
전자에 적합한 것은 offset 기반 페이지네이션, 후자에 적합한 것은 cursor 기반 페이지네이션이므로 Generic 클래스를 사용해 pagination이 적용된 한 api에 두 방식의 요청이 가능하도록 했다.
```kotlin
data class CursorPage<T>(
    val contents: List<T>,
    val page: Long? = null,
    val cursor: Long? = null, // DoubleCursorPage의 경우 Pair<Long, Long>? 타입
    val size: Long,
    val isLast: Boolean
)
```
query parameter에 page, cursor, size가 존재하고, page가 offset기반 방식에, cursor가 cursor기반 방식에 사용된다.\
offset기반 방식에서는 첫 페이지가 0임을 당연히 알 수 있지만, cursor기반 방식에서는 첫 페이지를 요청하기 위해 필요한 cursor가 무엇인지 알 수 없기 때문에 cursor가 주어지지 않은 (null 인) 경우에 첫 페이지를 제공한다.\
따라서 offset기반 방식 요청인지 cursor기반 방식 요청인지는 page query parameter의 nullity를 기준으로 한다. 

댓글은 대댓글의 존재 때문에 작성 시간 만으로 정렬할 수 없다.\
따라서 원댓글의 작성시간이 최신이면 더 큰 값을 할당 받는 `replyGroup`의 순서를 1순위, 작성시간을 2순위로 정렬한다.\
이에 따라 cursor기반 페이지네이션에도 커서가 두개 필요해 이때는 `DoubleCursorPage`를 사용해 페이지 사이에 누락되는 데이터가 없게 한다.\
마찬가지로, 현재 구현의 best게시물은 공감 개수를 1순위, 작성시간을 2순위로 정렬하기 때문에 `DoubleCursorPage`를 사용한다.
