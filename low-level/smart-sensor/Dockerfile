FROM ubuntu:focal

LABEL  maintainer="SiropOps <Cyril_Boillat@hotmail.com>"

ARG DEBIAN_FRONTEND=noninteractive

ENV TZ=Europe/Zurich

RUN apt-get -y update -qq && \
	apt-get install -y tzdata && \
	apt-get -y install python3 python3-pip bluetooth libbluetooth-dev make build-essential libglib2.0-dev libffi-dev python-dev python3-setuptools vim && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /tmp/* /var/tmp/* && \
	pip3 install pika && \
	pip3 install pybluez

COPY ./app.py /app/

CMD ["python3", "/app/app.py"]
