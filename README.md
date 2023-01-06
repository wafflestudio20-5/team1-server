# team1-server

## Build and Run
### 1. Local Development
- Load local mysql database
```
% docker-compose -f docker-compose.local.db.yml up -d   
```
- Set ```-Dspring.profiles.active=local``` in VM options
- Run application
- If you want to test local app with docker image, change 'localhost' to 'host.docker.internal' in application-local.yml file and  run below command
```
% docker build --build-arg PROFILES=local -t wafflytime-local . -f local.Dockerfile
```

### 2. Prod Development
- Set ```-Dspring.profiles.active=prod``` in VM options
- prod 로 테스트 해보는 것은 최대한 피하도록 한다
