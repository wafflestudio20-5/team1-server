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
### 4. Real-Time Chat (WebSocket)

### 5. SSE notification

### 6. OAuth Code with Redis
