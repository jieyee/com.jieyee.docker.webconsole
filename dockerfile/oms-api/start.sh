#!/bin/bash
ps -ef|grep app.war  |grep -v grep|awk '{print $2}' |xargs kill -9
nohup /opt/redis-4.0.2/src/redis-server /opt/redis-4.0.2/redis.conf >/dev/null 2>&1 &
sleep 5;
nohup /opt/jdk/bin/java -jar -Dspring.config.location=/root/application.yml /root/app.war  >/dev/null 2>&1 &
