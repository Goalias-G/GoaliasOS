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
# -XX:+UseG1GC: 使用 G1 垃圾收集器（低延迟）
# -XX:MaxGCPauseMillis=200: 最大 GC 停顿时间 200ms
# -XX:+OptimizeStringConcat: 优化字符串拼接
# -XX:+UseStringDeduplication: 字符串去重，节省内存
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:+DisableExplicitGC"

# 启动命令
# 定义启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar GoaliasOS.jar"]

#docker build -t goaliasos:latest .
#
#docker run -d \
#        --name goaliasos \
#        --restart always \
#        --network goaliasNet \
#        -p 7000:7000 \
#        -v $(pwd)/config:/app/os-startup/src/main/resources \
#        -v $(pwd)/logs:/app/logs \
#        -e GOALIAS_PASSWORD=$GOALIAS_PASSWORD \
#        -e GOALIAS_SERVER_IP=$GOALIAS_SERVER_IP \
#goaliasos:latest
