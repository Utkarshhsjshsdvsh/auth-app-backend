FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# 🔥 ADD THIS LINE (fix permission)
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/auth-app-backend-0.0.1-SNAPSHOT.jar"]