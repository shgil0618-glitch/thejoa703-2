<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
Step1) EC2
1. 인스턴스만들기
2. 1~16까지 셋팅 (java, nginx, pm2, docker,,,,,)

Step2) oracle ( User )
```
SQL> CREATE USER legacy IDENTIFIED BY tiger;
User created.

SQL> GRANT CONNECT, RESOURCE TO legacy;
Grant succeeded.
```
```

CREATE TABLE sboard2 (
   id NUMBER PRIMARY KEY,
   app_user_id NUMBER NOT NULL,
   btitle VARCHAR2(1000) NOT NULL,
   bcontent CLOB NOT NULL,
   bpass VARCHAR2(255) NOT NULL,
   bfile VARCHAR2(255) DEFAULT '0.png',
   bhit NUMBER DEFAULT 0,
   bip VARCHAR2(255) NOT NULL,
   created_at DATE  default sysdate
); 
create sequence sboard2_seq;

CREATE TABLE APPUSER (
    APP_USER_ID   NUMBER(5)       CONSTRAINT PK_APPUSER PRIMARY KEY,
    EMAIL         VARCHAR2(100)   CONSTRAINT NN_APPUSER_EMAIL NOT NULL,
    PASSWORD      VARCHAR2(100),
    MBTI_TYPE_ID  NUMBER(3),
    CREATED_AT    DATE,
    UFILE         VARCHAR2(255),
    MOBILE        VARCHAR2(50),
    NICKNAME      VARCHAR2(50),
    PROVIDER      VARCHAR2(50)    CONSTRAINT NN_APPUSER_PROVIDER NOT NULL,
    PROVIDER_ID   VARCHAR2(100)
);
   create sequence appuser_seq;

-- 이메일 + PROVIDER 조합 유니크 제약
ALTER TABLE APPUSER
ADD CONSTRAINT UK_APPUSER_EMAIL_PROVIDER UNIQUE (EMAIL, PROVIDER);


CREATE TABLE AUTHORITIES (
    AUTH_ID      NUMBER(5)        CONSTRAINT PK_AUTHORITIES PRIMARY KEY,
    EMAIL        VARCHAR2(255),
    AUTH         VARCHAR2(255)    CONSTRAINT NN_AUTHORITIES_AUTH NOT NULL,
    APP_USER_ID  NUMBER(5)
);

create sequence authorities_seq;

-- 동일 사용자에게 같은 권한 중복 방지 (APP_USER_ID + AUTH 유니크)
ALTER TABLE AUTHORITIES
ADD CONSTRAINT UK_AUTHORITIES_USER_AUTH UNIQUE (APP_USER_ID, AUTH);

-- 외래키 설정: AUTHORITIES.APP_USER_ID → APPUSER.APP_USER_ID
ALTER TABLE AUTHORITIES
ADD CONSTRAINT FK_AUTHORITIES_APPUSER FOREIGN KEY (APP_USER_ID)
REFERENCES APPUSER (APP_USER_ID);
```



Step3) 구동동작확인 (파일수정)
[1-3] .env
1.  properties > .env
2.  pom.xml    - dotenv-java
```
      <!-- dotenv -->
      <!-- dotenv -->
      <dependency>
           <groupId>io.github.cdimascio</groupId>
           <artifactId>dotenv-java</artifactId>
           <version>2.3.2</version>
      </dependency>
```
3. @SpringBootApplication
```
      dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
```
[4-5] lombok
4. pom.xml - lombok
```
      <dependency>
          <groupId>org.projectlombok</groupId>
          <artifactId>lombok</artifactId>
          <version>1.18.32</version> <!-- 최신 안정 버전 -->
          <scope>provided</scope>
      </dependency>
```
5.  pom.xml - build
```
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.32</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
```

[6]  mvnw
6. build
```
.\mvnw.cmd clean package -DskipTests
```

..........................................................


Step4) Actions secrets / New secret
```
Name: LEGACY_ENV
Secret: .env파일내용
```


Step5) aws - oracle
1. 유저만들기
2. table 처리
```
sudo docker exec -it oracle-xe  sqlplus system/oracle@XE

CREATE USER legacy IDENTIFIED BY tiger;
GRANT CONNECT, RESOURCE TO legacy;


sudo  docker exec -it oracle-xe  sqlplus legacy/tiger@XE

CREATE TABLE sboard2 (
   id NUMBER PRIMARY KEY,
   app_user_id NUMBER NOT NULL,
   btitle VARCHAR2(1000) NOT NULL,
   bcontent CLOB NOT NULL,
   bpass VARCHAR2(255) NOT NULL,
   bfile VARCHAR2(255) DEFAULT '0.png',
   bhit NUMBER DEFAULT 0,
   bip VARCHAR2(255) NOT NULL,
   created_at DATE  default sysdate
); 
create sequence sboard2_seq;

CREATE TABLE APPUSER (
    APP_USER_ID   NUMBER(5)       CONSTRAINT PK_APPUSER PRIMARY KEY,
    EMAIL         VARCHAR2(100)   CONSTRAINT NN_APPUSER_EMAIL NOT NULL,
    PASSWORD      VARCHAR2(100),
    MBTI_TYPE_ID  NUMBER(3),
    CREATED_AT    DATE,
    UFILE         VARCHAR2(255),
    MOBILE        VARCHAR2(50),
    NICKNAME      VARCHAR2(50),
    PROVIDER      VARCHAR2(50)    CONSTRAINT NN_APPUSER_PROVIDER NOT NULL,
    PROVIDER_ID   VARCHAR2(100)
);
   create sequence appuser_seq;

-- 이메일 + PROVIDER 조합 유니크 제약
ALTER TABLE APPUSER
ADD CONSTRAINT UK_APPUSER_EMAIL_PROVIDER UNIQUE (EMAIL, PROVIDER);


CREATE TABLE AUTHORITIES (
    AUTH_ID      NUMBER(5)        CONSTRAINT PK_AUTHORITIES PRIMARY KEY,
    EMAIL        VARCHAR2(255),
    AUTH         VARCHAR2(255)    CONSTRAINT NN_AUTHORITIES_AUTH NOT NULL,
    APP_USER_ID  NUMBER(5)
);

create sequence authorities_seq;

-- 동일 사용자에게 같은 권한 중복 방지 (APP_USER_ID + AUTH 유니크)
ALTER TABLE AUTHORITIES
ADD CONSTRAINT UK_AUTHORITIES_USER_AUTH UNIQUE (APP_USER_ID, AUTH);

-- 외래키 설정: AUTHORITIES.APP_USER_ID → APPUSER.APP_USER_ID
ALTER TABLE AUTHORITIES
ADD CONSTRAINT FK_AUTHORITIES_APPUSER FOREIGN KEY (APP_USER_ID)
REFERENCES APPUSER (APP_USER_ID);
 
```




Step6) nigix
```

server {
    listen 80;
    server_name 15.165.48.147;


        # legacy-boot 서비스 (포트 8484)
        location / {
                proxy_pass http://localhost:8080;
                proxy_http_version 1.1;
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto $scheme;
        }


        # legacy-boot 업로드 파일 접근
        location /uploads/ {
                alias /home/ubuntu/legacy-boot/target/uploads/;
                autoindex off;
        }

}

```

```
sudo nginx -t
sudo systemctl restart nginx
```

Step7) deploy
.github\workflows
```
name: Deploy Fullstack App   # 워크플로우 이름 정의

on:     # 실행조건 정의
  push:    # push 이벤트 발생시 실행
    branches:   
      - main   # main 브랜치에 push 될 때만 실행     

jobs:       # 실행할 job 정의
  legacy-boot:
    runs-on: ubuntu-latest
    steps:
      # 1. 코드 체크아웃
      - name: Checkout code
        uses: actions/checkout@v3

      # 2. JDK 11 설정
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'

      # 3. Maven 빌드
      - name: Build Legacy Boot App
        run: mvn clean package -DskipTests
        working-directory: legacy-boot

      # 4. Secrets에서 .env 복원 (CI 러너 루트에 파일 생성)   
      - name: Restore .env from Secrets
        run: |
          echo "${{ secrets.LEGACY_ENV }}" > .env
          ls -al .env   # 파일인지 확인용 로그
          head -n 5 .env   # 내용 일부 확인 (마스킹됨)

      # 5. EC2에 target 디렉토리 생성
      - name: Ensure legacy-boot directory exists on EC2
        uses: appleboy/ssh-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: mkdir -p /home/ubuntu/legacy-boot/target

      # 6. 빌드된 JAR 파일 복사 (경로 중첩 방지 → strip_components 사용)
      - name: Copy Legacy Boot JAR to EC2
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          source: "legacy-boot/target/*.jar"
          target: "/home/ubuntu/legacy-boot/target/"
          strip_components: 2   # legacy-boot/target 경로 제거 후 JAR만 복사

      # 7. 기존 .env 디렉토리 삭제 (있으면 제거)
      - name: Remove existing .env directory on EC2
        uses: appleboy/ssh-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: rm -rf /home/ubuntu/legacy-boot/.env

      # 8. .env 파일 복사 (파일로 확실히 반영)   
      - name: Copy Legacy Boot .env to EC2
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          source: ".env"
          target: "/home/ubuntu/legacy-boot/"
          # ⬆️ target을 디렉토리로 지정 → EC2에 /home/ubuntu/legacy-boot/.env 파일 생성

      # 9. pm2로 실행  
      - name: Run Legacy Boot on EC2 with pm2
        uses: appleboy/ssh-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd /home/ubuntu/legacy-boot
            pm2 delete legacy-boot || true

            # 안전하게 .env 로드
            set -a
            . ./.env
            set +a

            JAR_FILE=/home/ubuntu/legacy-boot/target/boot001-0.0.1-SNAPSHOT.jar

            pm2 start java --name legacy-boot -- \
              -Doracle.jdbc.timezoneAsRegion=false \
              -Duser.timezone=Asia/Seoul \
              -jar /home/ubuntu/legacy-boot/target/boot001-0.0.1-SNAPSHOT.jar
```


Step8) Git Actions 
```
git add .
git commit -m "test"
git push origin main
```

aa