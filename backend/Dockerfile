## ============ 阶段一：构建 ============
#FROM maven:3.9-eclipse-temurin-17-alpine AS builder
#WORKDIR /build
## 先复制 pom.xml，利用 Docker 层缓存（依赖不变则跳过下载）
#COPY pom.xml .
#RUN mvn dependency:go-offline -B -q
## 复制源码并打包（跳过测试，加快构建）
#COPY src ./src
#RUN mvn clean package -DskipTests -B -q
## ============ 阶段二：运行 ============
#FROM eclipse-temurin:17-jre-alpine
#WORKDIR /app
## 从构建阶段复制 jar
#COPY --from=builder /build/target/*.jar app.jar
## 创建日志目录（application-prod.yml 需要）
#RUN mkdir -p /var/log/flavor-creator
#EXPOSE 8080
#CMD ["java", "-jar", "app.jar"]

FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /root/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /root/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>central</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>阿里云公共仓库</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B -q

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN mkdir -p /var/log/flavor-creator

EXPOSE 8080

CMD ["java" , "-jar" , "app.jar"]