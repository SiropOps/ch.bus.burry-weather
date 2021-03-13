FROM ubuntu:focal

LABEL  maintainer="SiropOps <Cyril_Boillat@hotmail.com>"

ARG DEBIAN_FRONTEND=noninteractive

ENV TZ=Europe/Zurich

RUN apt-get -y update -qq && \
	apt-get install -y tzdata && \
	apt install python3-pip bluetooth libbluetooth-dev vim -y && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /tmp/* /var/tmp/* && \
	pip3 install pybluez

COPY ./app.py /app/

CMD ["python3", "/app/app.py"]
