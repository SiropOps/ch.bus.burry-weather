#!/usr/bin/env python

'''
Created on 12 nov. 2020

@author: SiropOps

'''
import itertools
import json
import logging.handlers
import os
import sys, traceback
import threading
from time import strftime, gmtime
import time
import uuid
import board
import adafruit_dht
import pika

sys.path.insert(0, "/usr/local/bin")

# Deafults
LOG_LEVEL = logging.INFO  # Could be e.g. "DEBUG" or "WARNING"
LOG_FILENAME = "/app/fail/dht-22.log"

FAIL_DIR = "/app/fail/"


class MyLogger(object):
    '''
    Make a class we can use to capture stdout and sterr in the log
    '''

    def __init__(self, logger, level):
        """Needs a logger and a logger level."""
        self.logger = logger
        self.level = level

    def write(self, message):
        # Only log if there is a message (not just a new line)
        if message.rstrip() != "":
            self.logger.log(self.level, message.rstrip())

    def flush(self):
        pass


# Configure logging to log to a file, making a new file at midnight and keeping the last 3 day's data
# Give the logger a unique name (good practice)
logger = logging.getLogger(__name__)
# Set the log level to LOG_LEVEL
logger.setLevel(LOG_LEVEL)
# Make a handler that writes to a file, making a new file at midnight and keeping 3 backups
handler = logging.handlers.RotatingFileHandler(LOG_FILENAME, maxBytes=2000000, backupCount=3)
# Format each log message like this
formatter = logging.Formatter('%(asctime)s %(levelname)-8s %(message)s')
# Attach the formatter to the handler
handler.setFormatter(formatter)
# Attach the handler to the logger
logger.addHandler(handler)

console = logging.StreamHandler()
console.setLevel(LOG_LEVEL)
console.setFormatter(formatter)
logger.addHandler(console)

# Replace stdout with logging to file at INFO level
sys.stdout = MyLogger(logger, logging.INFO)
# Replace stderr with logging to file at ERROR level
sys.stderr = MyLogger(logger, logging.ERROR)


class Data(object):

    def __init__(self, sensor):

        try:
            self.temperature = sensor.temperature - 4
            self.humidity = sensor.humidity
        except Exception as error:
            self.temperature = None
            self.humidity = None

    def __repr__(self):
        return str(self.__dict__)


def failOver(b):
    try:
        logger.info('failOver Started Script')

        for _ in itertools.repeat(None, 1):  # repeat 5 minutes
            with open(FAIL_DIR + str(uuid.uuid1()) + '.dht-22.json', 'w') as outfile:
                json.dump(Data(b).__dict__, outfile)
                time.sleep(300)  # set to whateve
        logger.info("failOver is done");
    except Exception as e:
        logger.error('failOver error: ' + str(e))


def failBack(channel):
    try:
        for file in os.listdir(FAIL_DIR):
            try:
                if file.endswith(".dht-22.json"):
                    data = json.load(open(FAIL_DIR + file, 'r'))
                    channel.basic_publish(exchange='',
                                          routing_key='dht-22',
                                          properties=pika.BasicProperties(content_type='application/json'),
                                          body=json.dumps(data))
                    os.remove(FAIL_DIR + file)
            except Exception as e:
                logger.error('file ' + file + ' error: ' + str(e))
                os.remove(FAIL_DIR + file)
    except Exception as e:
        logger.error('failBack error: ' + str(e))


logger.info('Start Script at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

time.sleep(360)

logger.info('Sleep end at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

if __name__ == '__main__':
    while True:
        try:
            # Initial the dht device, with data pin connected to:
            dhtDevice = adafruit_dht.DHT22(board.D4)

            is_connected = False
            try:
                credentials = pika.PlainCredentials(os.environ['spring.rabbitmq.username'], os.environ['spring.rabbitmq.password'])
                connection = pika.BlockingConnection(pika.ConnectionParameters(host=os.environ['spring.rabbitmq.host'], port=os.environ['spring.rabbitmq.port'], virtual_host='/', credentials=credentials, heartbeat=600))
                if connection.is_open:
                    channel = connection.channel()
                    channel.queue_declare(queue='dht-22', durable=True)
                    channel.basic_publish(exchange='',
                            routing_key='dht-22',
                            properties=pika.BasicProperties(content_type='application/json'),
                            body=json.dumps(Data(dhtDevice).__dict__))
                    is_connected = True
                    logger.info('RabbitMQ is started at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                else:
                    logger.error('RabbitMQ is not connected at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                    failOver(dhtDevice)
            except Exception as e:
                logger.error('RabbitMQ connection is fail at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                traceback.print_exc(2, file=sys.stdout)
                failOver(dhtDevice)

            if is_connected:
                failBack(channel)
                while True:
                    time.sleep(5)
                    channel.basic_publish(exchange='',
                            routing_key='dht-22',
                            properties=pika.BasicProperties(content_type='application/json'),
                            body=json.dumps(Data(dhtDevice).__dict__))

            if is_connected:
                connection.close()

        except Exception as e:
            logger.error('General error: ' + str(e))
            logger.info('Sleep one hour')
            time.sleep(3600)  # one hour

    sys.exit(os.EX_SOFTWARE)
