#!/bin/bash

export HOME=/home/ubuntu
export BUILD_DIR=$HOME/build-folder
export CATALINA_HOME=/opt/tomcat

exec &>> $HOME/deploy.log

echo "Changing to $BUILD_DIR directory"
cd $BUILD_DIR

echo "Injecting application.properties into wise.war"
zip -g wise.war WEB-INF/classes/application.properties

echo "Moving wise.war to $CATALINA_HOME/webapps/ROOT.war"
mv wise.war $CATALINA_HOME/webapps/ROOT.war
chown tomcat:tomcat $CATALINA_HOME/webapps/ROOT.war

echo "Deleting build folder"
rm -rf $BUILD_DIR

echo "Finishing deployment at $(date)"