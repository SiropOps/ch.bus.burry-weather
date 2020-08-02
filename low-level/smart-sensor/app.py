#!/usr/bin/env python

'''
Created on 14 sept. 2019

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

from beewi_smartclim import BeewiSmartClimPoller
import pika

sys.path.insert(0, "/usr/local/bin")

# Deafults
LOG_LEVEL = logging.INFO  # Could be e.g. "DEBUG" or "WARNING"
LOG_FILENAME = "/app/fail/smart-sensor.log"

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
        self.temperature = sensor.get_temperature() - 4
        self.humidity = sensor.get_humidity()
        self.battery = sensor.get_battery()
    
    def __repr__(self):
        return str(self.__dict__)

def failOver(b):
    try:
        logger.info('failOver Started Script')

        for _ in itertools.repeat(None, 1):  # repeat 5 minutes
            with open(FAIL_DIR + str(uuid.uuid1()) + '.smart-sensor.json', 'w') as outfile:
                json.dump(Data(b).__dict__, outfile)
                time.sleep(300)  # set to whateve
        logger.info("failOver is done.\nExiting. at " + strftime("%d-%m-%Y %H:%M:%S", gmtime()));
    except Exception as e:
        logger.error('failOver error: ' + str(e))

def failBack(channel):
    try:
        for file in os.listdir(FAIL_DIR):
            try:
                if file.endswith(".smart-sensor.json"):
                    data = json.load(open(FAIL_DIR + file, 'r'))
                    channel.basic_publish(exchange='',
                                          routing_key='smart-sensor',
                                          properties=pika.BasicProperties(content_type='application/json'),
                                          body=json.dumps(data)) 
                    os.remove(FAIL_DIR + file)
            except Exception as e:
                logger.error('file ' + file + ' error: ' + str(e))
                os.remove(FAIL_DIR + file)
    except Exception as e:
        logger.error('failBack error: ' + str(e))


logger.info('Start Script at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

time.sleep(15)

logger.info('Sleep end at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

if __name__ == '__main__':
    b = BeewiSmartClimPoller(os.environ['beewi.mac'])
    try:
        while True:
            b.update_sensor()
            try:
                credentials = pika.PlainCredentials(os.environ['spring.rabbitmq.username'], os.environ['spring.rabbitmq.password'])
                connection = pika.BlockingConnection(pika.ConnectionParameters(os.environ['spring.rabbitmq.host'], os.environ['spring.rabbitmq.port'], '/', credentials))
                if connection.is_open:
                    channel = connection.channel()
                    channel.queue_declare(queue='smart-sensor')
                    failBack(channel)
                    channel.basic_publish(exchange='',
                            routing_key='smart-sensor',
                            properties=pika.BasicProperties(content_type='application/json'),
                            body=json.dumps(Data(b).__dict__)) 
                else:
                    logger.error('RabbitMQ is not connected at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                    failOver(b)
                    sys.exit(os.EX_SOFTWARE)
            except Exception as e:
                logger.error('RabbitMQ connection is fail at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                logger.error(str(e))
                failOver(b)
                sys.exit(os.EX_SOFTWARE)

            time.sleep(300)  # set to whatever
    
        connection.close()
        logger.info("Done.\nExiting. at " + strftime("%d-%m-%Y %H:%M:%S", gmtime()));

    except Exception as e:
        logger.error('General error: ' + str(e))
        sys.exit(os.EX_SOFTWARE)
        