FROM resin/rpi-raspbian:buster

LABEL  maintainer="SiropOps <Cyril_Boillat@hotmail.com>"

RUN apt-get -y update -qq && \
	apt-get -y install python3 python3-pip make build-essential libglib2.0-dev libffi-dev python-dev python3-setuptools vim && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /tmp/* /var/tmp/* && \
	pip3 install pika && \
	pip3 install wheel && \
	pip3 install beewi-smartclim
	
COPY ./app.py /app/

CMD ["python3", "/app/app.py"]
