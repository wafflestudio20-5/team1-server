# team1-server

## Build and Run
### 1. Local Development
- Load local mysql database
```
% docker-compose -f docker-compose.local.db.yml up -d   
```
- Set ```optional:classpath:/application-local.yml``` in application.yml file
- Run application
- If you want to test local app with docker image, change 'localhost' to 'host.docker.internal' in application-local.yml file and  run below command
```
% docker build -t wafflytime-local . -f local.Dockerfile
```

### 2. Prod Development
- Set ```optional:classpath:/application-prod.yml``` in application.yml file
- prod 로 테스트 해보는 것은 최대한 피하도록 한다
