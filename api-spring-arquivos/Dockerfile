# Etapa 1: Build com Maven
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Copia apenas o pom.xml e baixa dependências primeiro (cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do projeto e gera o jar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime com JDK mais leve
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia o jar gerado da etapa build
COPY --from=build /app/target/*.jar app.jar

# Define a porta que o Render irá expor
EXPOSE 8080

# Permite passar parâmetros via variáveis de ambiente
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]