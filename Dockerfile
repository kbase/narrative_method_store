FROM kbase/sdkbase2:latest as build

WORKDIR /tmp/nms

# dependencies take a while to D/L, so D/L & cache before the build so code changes don't cause
# a new D/L
# can't glob *gradle because of the .gradle dir
COPY build.gradle gradlew settings.gradle /tmp/nms/
COPY gradle/ /tmp/nms/gradle/
RUN ./gradlew dependencies

# Now build the code
# copy the deployment dir first since it's unlikely to change often
COPY deployment/ /kb/deployment/
COPY src /tmp/nms/src/
COPY war /tmp/nms/war/
RUN ./gradlew war

# Build the deployment directory
ENV DEPL=/kb/deployment/services/narrative_method_store
RUN mkdir -p $DEPL/webapps
RUN mkdir -m 777 $DEPL/logs
RUN cp /tmp/nms/build/libs/narrative_method_store.war $DEPL/webapps/root.war

FROM kbase/kb_jre:latest

# These ARGs values are passed in via the docker build command
ARG BUILD_DATE
ARG VCS_REF
ARG BRANCH 

# TODO BUILD we really need to switch to a newer image
RUN echo "deb http://archive.debian.org/debian stretch main" > /etc/apt/sources.list

RUN apt update -y && apt install -y git

ENV KB_DEPLOYMENT_CONFIG "/kb/deployment/conf/deployment.cfg"

COPY --from=build /kb/deployment /kb/deployment/

LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.vcs-url="https://github.com/kbase/narrative_method_store.git" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.schema-version="1.0.0-rc1" \
      us.kbase.vcs-branch=$BRANCH \
      maintainer="Steve Chan sychan@lbl.gov"

EXPOSE 7058
ENTRYPOINT [ "/kb/deployment/bin/dockerize" ]
CMD [ "-template", "/kb/deployment/conf/.templates/deployment.cfg.templ:/kb/deployment/conf/deployment.cfg", \
      "-template", "/kb/deployment/conf/.templates/http.ini.templ:/kb/deployment/services/narrative_method_store/start.d/http.ini", \
      "-template", "/kb/deployment/conf/.templates/server.ini.templ:/kb/deployment/services/narrative_method_store/start.d/server.ini", \
      "-template", "/kb/deployment/conf/.templates/start_server.sh.templ:/kb/deployment/bin/start_server.sh", \
      "-stdout", "/kb/deployment/services/narrative_method_store/logs/request.log", \
      "/kb/deployment/bin/start_server.sh" ]

WORKDIR /kb/deployment/services/narrative_method_store
