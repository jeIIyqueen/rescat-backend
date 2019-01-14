~~~
██████╗   ███████╗  ███████╗     █████╗    ████╗    ████████╗
██╔══██╗  ██╔════╝  ██╔════╝    ██╔═══╝   ██══██╗   ╚══██╔══╝
██████╔╝  █████╗    ███████    ██╗       ████████╗     ██║
██╔══██╗  ██╔══╝    ╚════██║   ╚██╗      ██╔═══██╗     ██║
██║  ██║  ███████╗  ███████║    ╚█████╗  ██║   ██║     ██║
╚═╝  ╚═╝  ╚══════╝  ╚══════╝     ╚════╝  ╚═╝   ╚═╝     ╚═╝
~~~


## INTRODUCTION
Social information service, 'Rescat' aims at diffision of care-taker's proper caring culture and coexistence between human being and stray cats.  
You can also be with us :) Contact us!

Kakaotalk : @rescat  
E-mail : iamrescat@gmail.com  

## ARCHITECTURE
![ARCHITECTURE](ARCHITECTURE.png)

## MAIN FEATURE
* ##### RESTFul API
* ##### Spring AOP
* ##### String DATA JPA
* ##### use AWS EC2, RDS, S3
* ##### push alarm with FCM
* ##### encode password using BCryptPasswordEncoder
* ##### apply XSS-filter
* ##### apply SWAGGER2
* ##### create admin page (See [this repository](https://github.com/kwonhyeona/rescat-adminweb). you should request the permission)

## HOW TO START
```
$ git clone https://github.com/kwonhyeona/rescat-backend.git
$ cd rescat-backend
$ vi src/main/resources/application.properties
```
  
And then copy below code.
Don`t forget you should enter your server information.  

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=RDS_URL
spring.datasource.username=RDS_USERNAME
spring.datasource.password=RDS_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL55Dialect
spring.jpa.generate-ddl=false
spring.jpa.show-sql=true

#SWAGGER
logging.level.io.swagger.models.parameters.AbstractSerializableParameter=ERROR

#AWS key
cloud.aws.credentials.accessKey=AWS_ACCESSKEY
cloud.aws.credentials.secretKey=AWS_SECRETKEY
cloud.aws.stack.auto=false
cloud.aws.s3.bucket=rescat
cloud.aws.region.static=AWS_REGION
cloud.aws.s3.bucket.url=AWS_S)BUCKET_URL

#JWT
JWT.ISSUER=USERNAME
JWT.SECRET=SECRET_KEY

#GABIA
GABIA.APIKEY=GABIA_APIKEY
GABIA.SMSID=GABIA_SMSID
GABIA.SMSPHONENUMBER=GABIA_SMS_PHONE_NUMBER

#FCM
FCM.SERVERKEY=FCM_SERVERKEY
FCM.APIURL=FCM_APIURL

#NAVER
NAVER.MAP.REVERSE.CLIENTID=NAVER_CLIENTID
NAVER.MAP.REVERSE.CLIENTSECRETE=NAVER_SECRET
```

Are you ready to run the server? Follow the command below.
(*You should download gradle before following*)

```
$ gradle clean
$ gradle build


There will be a jar file in the ./build/libs/ directory.

$ cd build/libs/
$ java -jar jar_file_name
```

Finally, you can test our server RESTFul API 💯

## TEAM MEMBER
* ##### [권현아](https://github.com/kwonhyeona)
* ##### [백예은](https://github.com/bye0520)
* ##### [임수정](https://github.com/SujungRim)
* ##### [황유선](https://github.com/hyuseoni)
* ##### Be taught by [배다슬](https://github.com/bghgu)

## SNS
* ##### [FACEBOOK](https://www.facebook.com/iamRescat/)
* ##### [INSTAGRAM](https://www.instagram.com/iam_rescat/)
