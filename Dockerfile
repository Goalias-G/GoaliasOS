# GoaliasOS Docker 镜像 - 适用于 2 核 2G 服务器
# 基于 Eclipse Temurin JRE 17（轻量级）
FROM eclipse-temurin:17-jre-alpine

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建工作目录
WORKDIR /app

# 创建必要的目录结构
RUN mkdir -p /app/logs

# 复制 JAR 文件（需要先在本地打包: mvn clean package -DskipTests）
COPY GoaliasOS.jar /app/GoaliasOS.jar

# 暴露端口
EXPOSE 7000

# JVM 参数优化
# -Xms512m: 初始堆内存
# -Xmx512m: 最大堆内存
ENV JAVA_OPTS="-Xms256m -Xmx384m \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseContainerSupport \
  -XX:+UseParallelGC \
  -XX:MetaspaceSize=64m \
  -XX:MaxMetaspaceSize=128m \
  -XX:MaxDirectMemorySize=64m \
  -XX:+DisableExplicitGC"
# 启动命令
# 定义启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar GoaliasOS.jar"]

#sudo docker build -t goaliasos:latest .
#
#sudo docker run -d \
#        --name goaliasos \
#        --restart always \
#        --network goaliasNet \
#        -p 7000:7000 \
#        -v $(pwd)/config:/app/os-startup/src/main/resources \
#        -v $(pwd)/logs:/app/logs \
#        -e GOALIAS_PASSWORD=$GOALIAS_PASSWORD \
#        -e GOALIAS_SERVER_IP=$GOALIAS_SERVER_IP \
#        -e GOALIAS_DOMAIN_NAME=$GOALIAS_DOMAIN_NAME \
#        -e UAPI_API_KEY=$UAPI_API_KEY \
#        -e GOALIAS_MAIL_USERNAME=$GOALIAS_MAIL_USERNAME \
#        -e GOALIAS_MAIL_PASSWORD=$GOALIAS_MAIL_PASSWORD \
#goaliasos:latest
