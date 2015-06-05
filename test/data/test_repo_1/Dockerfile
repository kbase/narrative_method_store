FROM kbase/rtmin:1.2
# User apt-get instructions here
RUN apt-get update
# End of user apt-get instructions
WORKDIR /kb
RUN git clone https://github.com/kbase/dev_container
WORKDIR /kb/dev_container/modules
RUN git clone https://github.com/kbase/jars
RUN git clone https://github.com/kbase/auth
RUN git clone https://github.com/kbase/kbapi_common
COPY ./service /kb/dev_container/modules/GenomeFeatureComparator
WORKDIR /kb/dev_container
RUN ./bootstrap /kb/runtime/
ENV KB_TOP /kb/dev_container
ENV KB_RUNTIME /kb/runtime/
ENV TARGET $KB_TOP
ENV DEPLOY_RUNTIME=$KB_RUNTIME
RUN make
RUN mkdir -p /kb/deployment
RUN mkdir -p /kb/deployment/bin
RUN mkdir -p /kb/deployment/lib
RUN mkdir -p /kb/deployment/services
ENV KB_TOP /kb/deployment
ENV TARGET $KB_TOP
WORKDIR /kb/dev_container/modules/jars
RUN make deploy
WORKDIR /kb/dev_container/modules/auth
RUN make deploy
WORKDIR /kb/dev_container/modules/kbapi_common
RUN make deploy
RUN mkdir /kb/deployment/services/GenomeFeatureComparator
WORKDIR /kb/dev_container/modules/GenomeFeatureComparator
RUN make deploy
RUN mkdir /kb/deployment/services/GenomeFeatureComparator/work
RUN ls -l /kb/deployment/services/GenomeFeatureComparator/
RUN cat /kb/deployment/bin/run_GenomeFeatureComparator_async_job.sh
CMD cat /kb/deployment/services/GenomeFeatureComparator/work/token | xargs bash /kb/deployment/bin/run_GenomeFeatureComparator_async_job.sh /kb/deployment/services/GenomeFeatureComparator/work/input.json /kb/deployment/services/GenomeFeatureComparator/work/output.json