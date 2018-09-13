#生成的新镜像以centos镜像为基础
FROM centos68:redis
MAINTAINER by jieyee 

ENV JAVA_HOME /opt/jdk
ENV LANG en_US.UTF-8
ADD start.sh /root/
ADD app.war /root/app.war
ADD application.yml /root/
#设置工作目录
WORKDIR /root
#开放窗口的22端口
EXPOSE 22 6379 9120
#运行脚本，启动sshd服务
ENTRYPOINT  /usr/sbin/sshd && /root/start.sh && while sleep 60; do date -u +5T; done; 
