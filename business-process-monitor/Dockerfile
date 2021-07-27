
#########################################################
# Service build stage
#########################################################
FROM ${BASE_IMAGE}:${BASE_IMAGE_TAG} as base
WORKDIR /workspace
COPY . ./
RUN chmod a+x ./gradlew

FROM base as unit-tests
RUN ./gradlew unitTest

FROM unit-tests as service-tests
RUN ./gradlew serviceTest

FROM service-tests as summary-tests
RUN ./gradlew jacocoTestReport

FROM summary-tests as build
RUN ./gradlew assemble
RUN cp build/libs/*.jar app.jar
RUN chmod a+x docker/entrypoint.sh

#########################################################
# Service run stage
#########################################################
FROM ${BASE_IMAGE}:${BASE_IMAGE_TAG}
WORKDIR /app
COPY --from=build /workspace/docker/entrypoint.sh /workspace/app.jar ./
ENTRYPOINT ["./entrypoint.sh"]
