#!/usr/bin/env python

'''
Created on 14 sept. 2019

@author: SiropOps

'''
import bluetooth, subprocess
import itertools
import json
import logging.handlers
import os
import sys, traceback
import threading
from time import strftime, gmtime
import time
import uuid
from bluepy import btle
import struct

import pika

sys.path.insert(0, "/usr/local/bin")

# Read Handle 0x24
RX_CHAR = 0x24

# Deafults
LOG_LEVEL = logging.INFO  # Could be e.g. "DEBUG" or "WARNING"
LOG_FILENAME = '/app/fail/'+os.environ['spring.rabbitmq.queue']+'.log'

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


def float_value(nums):
    logger.info(nums)
    logger.info(struct.unpack('<H', nums[0:2]))
    # check if temp is negative
    num = (nums[1] << 8) | nums[0]
    if nums[1] == 0xfc:
        num = -((num ^ 0xffff) + 1)
    return float(num) / 100


class Data(object):

    def __init__(self, readings):
        self.temperature = float_value(readings[0:2])
        self.humidity = float_value(readings[2:4])
        self.battery = 0

    def __repr__(self):
        return str(self.__dict__)


# Function to recived data
def getFakeData():
    return {"temperature":-50.0, "humidity":-50.0}


# Function to recived data
def recv(dev):
    try:
        readings = dev.readCharacteristic(RX_CHAR)
        dev.disconnect()
        return readings
    except Exception as e:
        logger.error("Error reading BTLE: {}".format(e))
        return getFakeData()


logger.info('Start Script at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

#time.sleep(360)

logger.info('Sleep end at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

if __name__ == '__main__':
    
    while True:
        dev = None
        try:
            try:
                dev = btle.Peripheral(os.environ['sensor.mac'], addrType=btle.ADDR_TYPE_PUBLIC)
                b = recv(dev)
                logger.debug(b)
            except:
                logger.error('Failed to connect at ThermoBeCon ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                b = getFakeData();

            is_connected = False
            try:
                credentials = pika.PlainCredentials(os.environ['spring.rabbitmq.username'], os.environ['spring.rabbitmq.password'])
                connection = pika.BlockingConnection(pika.ConnectionParameters(host=os.environ['spring.rabbitmq.host'], port=os.environ['spring.rabbitmq.port'], virtual_host='/', credentials=credentials, heartbeat=600))
                if connection.is_open:
                    channel = connection.channel()
                    channel.queue_declare(queue=os.environ['spring.rabbitmq.queue'], durable=True)
                    logger.info('Data : ' + json.dumps(Data(b).__dict__))
                    channel.basic_publish(exchange='',
                            routing_key=os.environ['spring.rabbitmq.queue'],
                            properties=pika.BasicProperties(content_type='application/json'),
                            body=json.dumps(Data(b).__dict__))
                    is_connected = True
                    logger.info('RabbitMQ is started at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                else:
                    logger.error('RabbitMQ is not connected at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
            except Exception as e:
                logger.error('General error: ' + str(e))
                logger.error('RabbitMQ connection is fail at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

            if is_connected:
                while True:
                    time.sleep(300)
                    dev = btle.Peripheral(os.environ['sensor.mac'])
                    b = recv(dev)
                    logger.debug(b)
                    if b is not None:
                        channel.basic_publish(exchange='',
                            routing_key=os.environ['spring.rabbitmq.queue'],
                            properties=pika.BasicProperties(content_type='application/json'),
                            body=json.dumps(Data(b).__dict__))

            if is_connected:
                connection.close()
            else:
                time.sleep(60)

        except Exception as e:
            logger.error('General error: ' + str(e))
            logger.info('Sleep one hour')
            time.sleep(3600)  # one hour

    sys.exit(os.EX_SOFTWARE)

