FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app
COPY . /app
RUN /app/gradlew build -x test
ENTRYPOINT ["/app/gradlew"]
CMD ["bootRun" , "--args='--server.port=8080'"]