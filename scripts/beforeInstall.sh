#!/bin/bash

export HOME=/home/ubuntu
export BUILD_DIR=$HOME/build-folder
export LEGACY_BUILD_DIR=$HOME/legacy-build-folder
export BUILD_FILES=$HOME/wise-build-files
export CATALINA_HOME=/opt/tomcat

if [[ "$DEPLOYMENT_GROUP_NAME" == "qa-wise-api-deployment-group" ]]; then
  export ENV="qa"
else
  export ENV="prod"
fi

sudo -u ubuntu -g ubuntu touch $HOME/deploy.log
exec &>> $HOME/deploy.log

echo "Starting deployment at $(date)"

echo "Updating Ubuntu"
apt-get update
apt-get upgrade -y

echo "Setting server timezone to Los Angeles"
timedatectl set-timezone America/Los_Angeles

echo "Installing AWS CLI"
apt-get install awscli -y

echo "Downloading files from wise-build-files S3 bucket"
sudo -u ubuntu -g ubuntu mkdir $BUILD_FILES
sudo -u ubuntu -g ubuntu aws s3 sync s3://wise-build-files $BUILD_FILES
chmod u+x $BUILD_FILES/sync.sh

echo "Installing Java 11"
apt-get install openjdk-11-jdk-headless -y

echo "Create tomcat group"
groupadd -g 1001 tomcat

echo "Create tomcat user"
useradd -u 1001 -g tomcat -c "Apache Tomcat" -d $CATALINA_HOME -s /usr/sbin/nologin tomcat

echo "Adding ubuntu user to tomcat group"
usermod -a -G tomcat ubuntu

echo "Creating tomcat directory"
mkdir $CATALINA_HOME

echo "Making tomcat the owner of the tomcat directory"
chown tomcat:tomcat $CATALINA_HOME

echo "Downloading Tomcat 9"
wget -P /tmp https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.81/bin/apache-tomcat-9.0.81.tar.gz

echo "Unpackaging Tomcat 9 to $CATALINA_HOME"
tar xzvf /tmp/apache-tomcat-9.0.81.tar.gz -C $CATALINA_HOME --strip-components=1

echo "Giving tomcat user ownership of tomcat directory contents"
chown -R tomcat:tomcat $CATALINA_HOME

echo "Giving tomcat user execute permission on tomcat bin folder"
chmod -R u+x $CATALINA_HOME/bin

echo "Copying tomcat service file to /etc/systemd/system"
cp $BUILD_FILES/api/tomcat.service /etc/systemd/system/tomcat.service

echo "Removing Tomcat ROOT folder"
rm -rf $CATALINA_HOME/webapps/ROOT

echo "Add https to Tomcat server.xml"
sed 's/<Connector port="8080"/<Connector port="8080" scheme="https"/' -i $CATALINA_HOME/conf/server.xml

echo "Reloading daemon"
systemctl daemon-reload

echo "Starting Tomcat"
systemctl start tomcat

echo "Enabling Tomcat on startup"
systemctl enable tomcat

echo "Creating Tomcat curriculum and studentuploads folders"
sudo -u tomcat -g tomcat mkdir $CATALINA_HOME/webapps/curriculum
sudo -u tomcat -g tomcat mkdir $CATALINA_HOME/webapps/studentuploads

echo "Installing Nginx"
apt-get install nginx -y

echo "Adding Nginx www-data user to tomcat group"
usermod -a -G tomcat www-data

echo "Adding ip to nginx.conf"
sed 's/http {/http {\n        add_header ip $server_addr;/' -i /etc/nginx/nginx.conf

echo "Adding gzip_types to nginx.conf"
sed 's/gzip on;/gzip on;\n        gzip_types text\/plain text\/xml image\/gif image\/jpeg image\/png image\/svg+xml application\/json application\/javascript application\/x-javascript text\/javascript text\/css;/' -i /etc/nginx/nginx.conf

echo "Remove TLS 1.0 from nginx.conf"
sed 's/TLSv1 //g' -i /etc/nginx/nginx.conf

echo "Remove TLS 1.1 from nginx.conf"
sed 's/TLSv1.1 //g' -i /etc/nginx/nginx.conf

echo "Copying WISE Nginx config file to Nginx sites-enabled folder"
rm -f /etc/nginx/sites-enabled/*
cp $BUILD_FILES/api/$ENV/wise.conf /etc/nginx/sites-enabled/wise.conf
systemctl restart nginx

echo "Creating additional folders for WISE"
mkdir -p $BUILD_DIR/WEB-INF/classes
mkdir -p $LEGACY_BUILD_DIR/WEB-INF/classes
sudo -u ubuntu -g ubuntu mkdir $HOME/backup
sudo -u ubuntu -g tomcat mkdir $HOME/googleTokens

echo "Copying application.properties file to the build folder"
cp $BUILD_FILES/api/$ENV/application.properties $BUILD_DIR/WEB-INF/classes/application.properties

echo "Copying application-legacy.properties file to the legacy build folder"
cp $BUILD_FILES/api/$ENV/application-legacy.properties $LEGACY_BUILD_DIR/WEB-INF/classes/application.properties

echo "Installing network drive package"
apt-get install nfs-common -y

echo "Mounting network drive folders"
if [[ "$ENV" == "qa" ]]; then
  cp $BUILD_FILES/api/qa/fstab /etc/fstab
else
  cp $BUILD_FILES/api/fstab /etc/fstab
fi
mount -a

echo "Copying .vimrc file to the ubuntu home folder"
sudo -u ubuntu -g ubuntu cp $BUILD_FILES/.vimrc $HOME/.vimrc

echo "Appending text to .bashrc"
if [[ "$ENV" == "qa" ]]; then
  cat $BUILD_FILES/api/qa/append-to-bashrc.txt >> ~/.bashrc
else
  cat $BUILD_FILES/api/append-to-bashrc.txt >> ~/.bashrc
fi
source ~/.bashrc

echo "Copying message of the day file to update-motd.d folder to display notes on login"
if [[ "$ENV" == "qa" ]]; then
  cp $BUILD_FILES/api/qa/99-notes /etc/update-motd.d/99-notes
else
  cp $BUILD_FILES/api/99-notes /etc/update-motd.d/99-notes
fi
chmod 755 /etc/update-motd.d/99-notes

echo "Copying backup-nginx-logs script to /etc/cron.daily folder"
cp $BUILD_FILES/api/backup-nginx-logs /etc/cron.daily
chmod +x /etc/cron.daily/backup-nginx-logs

echo "Installing mysql client"
apt-get install mysql-client-core-8.0 -y

echo "Installing redis client"
apt-get install redis-tools -y

echo "Installing tree"
apt-get install tree -y

echo "Installing sysstat"
apt-get install sysstat -y

echo "Configuring sysstat parameters"
sed 's/ENABLED="false"/ENABLED="true"/' -i /etc/default/sysstat
sed 's/HISTORY=7/HISTORY=30/' -i /etc/sysstat/sysstat
sed 's/SADC_OPTIONS="-S DISK"/SADC_OPTIONS="-D -S DISK"/' -i /etc/sysstat/sysstat

echo "Enabling sysstat on startup"
systemctl enable sysstat

echo "Starting sysstat"
systemctl start sysstat

echo "Set log file max size to 1G"
sed 's/{/{\n        maxsize 1G/g' -i /etc/logrotate.d/rsyslog
sed 's/{/{\n        maxsize 1G/g' -i /etc/logrotate.d/nginx
sed 's/{/{\n  maxsize 1G/g' -i /etc/logrotate.d/tomcat

echo "Installing Clam AV"
apt-get install clamav clamav-daemon -y
systemctl stop clamav-freshclam

echo "Updating Clam AV database"
freshclam

echo "Updating Clam AV configuration"
echo -e "TCPSocket 3310\nTCPAddr 127.0.0.1" | tee -a /etc/clamav/clamd.conf

echo "Starting Clam AV"
systemctl enable clamav-freshclam
systemctl start clamav-freshclam
systemctl enable clamav-daemon
systemctl start clamav-daemon