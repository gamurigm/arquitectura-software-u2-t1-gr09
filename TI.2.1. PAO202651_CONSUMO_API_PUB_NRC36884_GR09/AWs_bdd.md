rest-countries.c3qwaa48yvby.us-east-2.rds.amazonaws.com

mysql -h rest-countries.c3qwaa48yvby.us-east-2.rds.amazonaws.com
      -P 3306
      -u admin
      -p



String url = "jdbc:mysql://rest-countries.c3qwaa48yvby.us-east-2.rds.amazonaws.com:3306/TU_BASE_DE_DATOS";
String user = "admin";
String password = "TU_CONTRASEÑA";

Connection conn = DriverManager.getConnection(url, user, password);

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>



version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://rest-countries.c3qwaa48yvby.us-east-2.rds.amazonaws.com:3306/TU_BASE_DE_DATOS
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: TU_CONTRASEÑA
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.cj.jdbc.Driver


spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=${SPRING_DATASOURCE_DRIVER_CLASS_NAME}
