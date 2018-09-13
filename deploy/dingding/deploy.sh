echo ---------------------------------------------
echo 1、请先保证有配置git的用户信息、ssh授权
echo 2、请修改变量以适应不同的项目
echo 3、请将修改好的配置文件放在env/test目录下
echo ---------------------------------------------
giturl=git@47.92.131.134:bright/dingding.git
pwd=/root/docker/webconsole/deploy/dingding
projectname=$pwd/dingding
runtime=$pwd/runtime
#appname=sms-web-0.0.1-SNAPSHOT.war 
buildcmd="gradle clean build -x test"
gitaouth=@qi8jb#1
jmxprot=9128

cd $pwd
if [ ! -d $runtime ];then
    mkdir $runtime    #不存在runtime创建运行时目录
fi
echo 拉起代码
if [ ! -d $projectname ];then
   
    echo 克隆仓库代码
    git clone $giturl 
#    expect -c " 
#        set timeout 300
#        spawn git clone $giturl 
#        expect { 
#	    passphrase { send \"$gitaouth\r\" } 
#        }
#        expect eof;
#        exit
#    "
else
    echo 覆盖更新代码
    cd $projectname
    git reset --hard | git pull -f
#    expect -c " 
#        set timeout 300
#        spawn  git pull -f 
#        expect { 
#	    passphrase { send \"$gitaouth\r\" }
#        }
#        expect eof;
#        exit
#    "
    cd ..
fi
echo 
chmod 755 -R $projectname  #给gitlab授权755权限


cd $projectname
$buildcmd
cd ..
echo  清理并部署
rm -rf $runtime/*  
mv  $projectname/build/libs/* runtime/  #拷贝编译后的文件到runtime目录
chmod 755 -R $runtime #给runtime授权755权限
#ps -ef | grep $appname | awk '{print $2}' | xargs kill -9 #杀掉运行$appname的进程

#echo 制定local配置后台启动并将控制台日志写入nohup.out ，在$jmxprot启动jmx监控
#nohup java -server -Xmx512m -jar runtime/$appname --spring.config.location=env/test/application.yml  -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$jmxprot -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.ssl=fals & 
echo 后台启动并将控制台日志写入nohup.out 
#nohup java -server -Xmx512m  -jar runtime/$appname   & 
echo 跟踪控制台日志
#tail -f nohup.out
echo 'copy app.war'
/usr/bin/docker cp $runtime/*.war $1:/root/app.war
#echo 'copy application.yml'
#/usr/bin/docker cp $projectname/env/dev/application.yml $1:/root/application.yml
echo 'start app.war' $1
/usr/bin/docker exec $1 /root/start.sh

