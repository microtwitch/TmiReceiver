#!/bin/bash

gradle clean && gradle build jar && sudo docker build --build-arg JAR_FILE=build/libs/\*.jar -t microtwitch/tmi-receiver .