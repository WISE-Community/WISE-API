#!/bin/bash

if [ ! -e "src/main/resources/application-dockerdev.properties" ]; then
  cp src/main/resources/application-dockerdev-sample.properties src/main/resources/application-dockerdev.properties
fi
