#!/bin/bash
ps -ef|grep app.war  |grep -v grep|awk '{print $2}' |xargs kill -9
nohup /opt/jdk/bin/java -jar -Dspring.config.location=/root/application.yml /root/app.war  >/dev/null 2>&1 &
