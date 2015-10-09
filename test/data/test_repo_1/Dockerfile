FROM kbase/devmin:1.1
# User apt-get instructions here
RUN DEBIAN_FRONTEND=noninteractive apt-get update;apt-get -y upgrade;apt-get install -y libblas3gf liblapack3gf libhdf5-serial-dev
RUN pip install tables
# End of user apt-get instructions
WORKDIR /kb/dev_container/modules
COPY ./service /kb/dev_container/modules/GenomeFeatureComparator
WORKDIR /kb/dev_container/modules/GenomeFeatureComparator
RUN make
ENV KB_TOP /kb/deployment
ENV TARGET $KB_TOP
RUN mkdir /kb/deployment/services/GenomeFeatureComparator
WORKDIR /kb/dev_container/modules/GenomeFeatureComparator
RUN make deploy
RUN mkdir /kb/deployment/services/GenomeFeatureComparator/work
RUN cat /kb/deployment/bin/run_GenomeFeatureComparator_async_job.sh
CMD cat /kb/deployment/services/GenomeFeatureComparator/work/token | xargs bash /kb/deployment/bin/run_GenomeFeatureComparator_async_job.sh /kb/deployment/services/GenomeFeatureComparator/work/input.json /kb/deployment/services/GenomeFeatureComparator/work/output.json
