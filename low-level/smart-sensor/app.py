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

import pika

sys.path.insert(0, "/usr/local/bin")

#Transmit Handle 0x0021
TX_CHAR_UUID = btle.UUID('0000fff5-0000-1000-8000-00805F9B34FB')
#Read Handle 0x0024
RX_CHAR_UUID = btle.UUID('0000fff3-0000-1000-8000-00805F9B34FB')

# Deafults
LOG_LEVEL = logging.INFO  # Could be e.g. "DEBUG" or "WARNING"
LOG_FILENAME = '/app/fail/'+os.environ['spring.rabbitmq.queue']+'.log'

FAIL_DIR = "/app/fail/"

PORT = 1         # RFCOMM port


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
        self.temperature = sensor["temperature"]
        if self.temperature is not None:
            self.temperature = self.temperature - 5
        self.humidity = sensor["humidity"]
        self.battery = 0

    def __repr__(self):
        return str(self.__dict__)

#Function to return a byte array as zero padded, space separated, hex values
def convert_to_text(results):
    hex_results = []
    for v in range(len(results)):
        hex_results.append("{:x}".format(results[v]).zfill(2))
    return " ".join(hex_results)

#Function to send a string to the device as a bytearray and return the results received
def write_bytes(vals,tx ,rx):
    write_val = bytearray.fromhex(vals)
    tx.write(write_val)
    read_val = rx.read()
    return read_val

#Function to convert the readings we get back into temperatures and humidities
def convert_to_readings(response):
    readings = []
    for v in range(6):
        results_position = 6 + (v * 2)
        reading = int.from_bytes(response[results_position:results_position+2],byteorder='little')
        reading = reading * 0.0625
        if reading > 2048:
            reading = -1 * (4096-reading)
        readings.append("{:.2f}".format(reading))
    logger.debug(",".join(readings))
    for w in range(2, 0, -1):
        if float(readings[w]) != 0.00:
           return {"temperature": float(readings[w]), "humidity": float(readings[w+3])}
    return None
#Function to recived data
def getFakeData():
    return {"temperature": -50.0, "humidity": -50.0}

def getDataPoint(available, tx, rx, delta=0):
    #Data is returned as three pairs of temperature and humidity values
    data_point = int(available / 3) + delta
    index = data_point * 3
    #convert index to hex, padded with leading zeroes
    index_hex = hex(index)[2:].zfill(4)
    #reverse the byte order of the hex values
    index_hex_reversed = index_hex[2:] + index_hex[0:2]
    #build the request string to be sent to the device
    hex_string = "07" + index_hex_reversed + "000003"
    #send the request and get the response
    response = write_bytes(hex_string, tx, rx)
    #convert the response to temperature and humidity readings
    return convert_to_readings(response)

#Function to recived data
def recv(dev):
    try:
        #Get handles to the transmit and receieve characteristics
        tx = dev.getCharacteristics(uuid=TX_CHAR_UUID)[0]
        rx = dev.getCharacteristics(uuid=RX_CHAR_UUID)[0]

        #Send initial command to get the number of available data points
        response = write_bytes("0100000000", tx, rx)
        #The number of available values is stored in the second and third bytes of the response, little endian order
        available = int.from_bytes(response[1:3], byteorder='little')

        sensor = getDataPoint(available, tx, rx, 1)
        if sensor is None:
            logger.debug('sensor is none')
            sensor = getDataPoint(available,tx, rx)
        if sensor is None:
            logger.debug('sensor is none')
            sensor = getDataPoint(available,tx, rx, -1)

        dev.disconnect()
        return sensor

    except Exception as e:
        logger.error('General error: ' + str(e))
        return getFakeData()


logger.info('Start Script at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

time.sleep(360)

logger.info('Sleep end at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

if __name__ == '__main__':

    while True:
        dev = None
        try:
            try:
                dev = btle.Peripheral(os.environ['beewi.mac'])
                b = recv(dev)
                logger.info(b)
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
                    channel.basic_publish(exchange='',
                            routing_key=os.environ['spring.rabbitmq.queue'],
                            properties=pika.BasicProperties(content_type='application/json'),
                            body=json.dumps(Data(b).__dict__))
                    is_connected = True
                    logger.info('RabbitMQ is started at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
                else:
                    logger.error('RabbitMQ is not connected at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))
            except Exception as e:
                logger.error('RabbitMQ connection is fail at ' + strftime("%d-%m-%Y %H:%M:%S", gmtime()))

            if is_connected:
                while True:
                    time.sleep(300)
                    dev = btle.Peripheral(os.environ['beewi.mac'])
                    b = recv(dev)
                    logger.info(b)
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

