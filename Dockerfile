# Etapa 1: Compilación (Usamos Maven y Java 22)
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Java 22 para correr la app)
FROM eclipse-temurin:22-jdk-slim
WORKDIR /app
# Copiamos el .jar generado en la etapa 1
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto
EXPOSE 8080
# Comando para iniciar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]