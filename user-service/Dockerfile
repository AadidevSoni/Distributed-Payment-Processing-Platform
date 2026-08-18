# ---------- Stage 1: Build ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy only what's needed to resolve dependencies first —
# this layer only gets invalidated when pom.xml actually changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Now copy the actual source and build.
# This layer changes on every code edit — placed last on purpose.
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jre AS run

WORKDIR /app

# Copy only the built jar from the build stage — nothing else.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]