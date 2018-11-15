FROM kbase/sdkbase2:latest as build


COPY . /tmp/narrative_method_store
COPY deployment /kb/deployment

RUN pip install configobj && \
    cd /tmp && \
    git clone https://github.com/kbase/jars && \
    cd /tmp/narrative_method_store && \
    # do an end run around git submodule commands in makefile - breaks in docker hub
    ant compile -Djardir=/tmp/jars/lib/jars  && \ 
    make deploy-service && \
    mkdir -m 777 /kb/deployment/services/narrative_method_store/logs 

FROM kbase/kb_jre:latest
# These ARGs values are passed in via the docker build command
ARG BUILD_DATE
ARG VCS_REF
ARG BRANCH 
ENV KB_DEPLOYMENT_CONFIG "/kb/deployment/conf/deployment.cfg"

COPY --from=build /kb/deployment /kb/deployment/

RUN apt-get update -y && apt-get install -y git

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
