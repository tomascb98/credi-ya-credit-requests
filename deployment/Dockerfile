#Building stage (Build)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY . .
RUN chmod +x gradlew
RUN ./gradlew :app-service:clean :app-service:bootJar -x test --no-daemon

#Runtime Stage (Run)
From eclipse-temurin:21-jre-alpine AS run
WORKDIR /app

#Para healthchecks (Actuator)
RUN apk add --no-cache wget

#Ajuste seguros de memoria para contenedores (HotSpot)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

#Copiamos el JAR generado del módulo app-service
COPY --from=builder /workspace/applications/app-service/build/libs/credit-requests.jar /app/app.jar

COPY deployment/config/ /config/

EXPOSE 8090
ENTRYPOINT ["sh","-c","java $JAVA_TOOL_OPTIONS -jar /app/app.jar --spring.config.additional-location=file:/config/"]


