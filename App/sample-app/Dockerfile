FROM openjdk:19-alpine
ARG DEPENDENCY=target/dependency

# Creating non-root user and running as non-root user
RUN addgroup -S nonprivileged && adduser -S dev -G nonprivileged
USER dev

COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.cs598ccc.sampleapp.SampleAppApplication"]