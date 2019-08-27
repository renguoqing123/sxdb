#!/bin/sh
export JAVA_HOME=""/app/jdk18""
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=.:$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH

JAVAS="$JAVA_HOME/bin/java"
update_path="/data/pkg_update";
backup_path="/data/pkg_backup";
now_date=$(date +%Y%m%d_%H%M%S)
kill_count=5
git_url=
BUILD_ID=DontKillMe
#备份保留天数
backup_day=30


####不同项目按需修改
#部署路径，JAR包位置
dep_path=/app/platform-sms;
#项目包名称，带.JAR
pkg_name=platform-sms-service-1.0-SNAPSHOT.jar;
#启动参数,覆盖配置文件中设置参数
start_option=""
#start_option="${start_option} -Dserver.port=2222"
#start_option="${start_option} -Deureka.client.serviceUrl.defaultZone=http://10.201.3.245:5555/eureka"
#end

#配置参数，放在jar文件之后
backend_option=""
backend_option="${backend_option} --config.server.url=10.201.3.251"
backend_option="${backend_option} --config.server.port=7777"
backend_option="${backend_option} --profile=test"
#end





#部署文件路径
dep_files=${dep_path}/${pkg_name}
#上传文件路径
update_files=${update_path}/${pkg_name}
#PID文件路径
lock_file=${0}.pid
#JAR包PID文件
pid_file=${dep_path}/$(basename ${dep_path}).pid
#out文件
out_file=${dep_path}/$(basename ${dep_path}).out

###################### 处理函数 ######################

#锁涵数
shell_lock(){
    echo "当前时间：${now_date}"
    echo "开始锁定脚本 [$pro_name] ..."
    if [[ -f $lock_file ]];then
        echo "lock file [$lock_file] is exists."
        echo "exit pro..."
        exit 0
    else
        echo $$ > $lock_file
    fi
}

#解锁涵数
shell_unlock(){
    echo "开始解锁 ..."
    if [[ -f $lock_file ]];then
        rm -rf $lock_file
        if [[ $? != 0 ]];then
            echo "error: 解锁失败，锁文件未成功删除！！！退出"
            echo "结束时间：${now_date}"

            exit 1
        fi
    else
        echo "lock file [$lock_file] is not exists"
    fi
}

#记录JAR pid
recode_pid(){
    get_process 2

    pid_data_line=$(cat ${pid_file}|wc -l)
    if [[ $pid_data_line != 1 ]];then
        echo "pid file [$pid_file] line -ne 1."
        echo "init pid file old data is 1"
        if [[ $1 == 'stop' ]];then
            echo "1|$(date +%Y%m%d%H%M%S)|${run_pid}|1|1" > ${pid_file}
        else
            echo "1|1|1|$(date +%Y%m%d%H%M%S)|${run_pid}" > ${pid_file}
        fi
    else
        pid_data_row=$(cat ${pid_file}|awk -F '|' '{print NF}')
        if [[ ${pid_data_row} -ne 5 ]];then
            echo "init pid file old data is 2"
            echo "2|$(date +%Y%m%d%H%M%S)|${run_pid}|2|2" > ${pid_file}
        else
            if [[ $1 == 'stop' ]];then

                if [[ -n ${run_pid} ]];then
                    stopPid=$(cat ${pid_file}|cut -d '|' -f 3)

                    if [[ ${stopPid} == ${run_pid} ]];then
                        startTime=$(cat ${pid_file}|cut -d '|' -f 1)
                        stoptime=$(date +%Y%m%d%H%M%S)
                        stopPid=${run_pid}
                        runtime=$(cat ${pid_file}|cut -d '|' -f 4)
                        runpid=$(cat ${pid_file}|cut -d '|' -f 5)
                    else

                        runtime=$(cat ${pid_file}|cut -d '|' -f 4)
                        if [[ ! -n ${runtime} ]];then
                            startTime=0
                            runtime=0
                        else
                            startTime=${runtime}
                            runtime=0
                        fi

                        stoptime=$(date +%Y%m%d%H%M%S)

                        runpid=$(cat ${pid_file}|cut -d '|' -f 5)
                        if [[ -n ${runpid} ]];then
                            if [[ ${runpid} != ${run_pid} ]];then
                                echo "正在运行的进程ID和记录中的ID不一至，进行同步处理，详情请查看${pid_file}_${stoptime}_conflict.txt"
                                cp -rf ${pid_file} ${pid_file}_${stoptime}_conflict.txt
                                echo "run pid is:${run_pid}" >> ${pid_file}
                            fi
                        fi
                        stopPid=${run_pid}
                        runpid=0
                    fi
                    echo "${startTime}|${stoptime}|${stopPid}|${runtime}|${runpid}" > ${pid_file}
                fi
            elif [[ $1 == 'start' ]];then
                startTime=$(cat ${pid_file}|cut -d '|' -f 1)
                stoptime=$(cat ${pid_file}|cut -d '|' -f 2)
                stopPid=$(cat ${pid_file}|cut -d '|' -f 3)

                if [[ ! -n ${stopPid} ]] || [[ ${stopPid} -eq 0 ]];then
                    stopPid=0
                    startTime=0
                    stoptime=0
                fi

                runtime=$(date +%Y%m%d%H%M%S)
                get_process 2
                echo "${startTime}|${stoptime}|${stopPid}|${runtime}|${run_pid}" > ${pid_file}
            else
                echo 'usaged: recode_pid [ start | stop ]'
            fi
        fi
    fi
}

#获取配置
get_config(){
    echo
}

#查检包并部署
check_pkg(){
	if [[ ! -f ${update_files} ]];then
		echo "包 [${update_files}] 上传失败"
		shell_unlock
		exit 0
	fi
}

#部署包
deploy_pkg(){

    if [[ $( echo ${check_arg}|awk -F '.' '{print $NF}') == 'jar' ]];then

        if [[ -f ${run_files} ]];then
            backup_data ${run_files}
            rm -f ${run_files};
        fi

        mv ${update_files} ${dep_files};
        if [[ $? != 0 ]];then
            echo "包 [${update_files}] 部署失败"
            shell_unlock
            exit 0
        fi
    else
        if [[ -d ${dep_path}/webapps ]];then
            backup_data ${dep_path}/webapps
            rm -rf ${dep_path}/webapps/*;
        fi

        mv ${update_files} ${dep_path}/webapps/;
        if [[ $? != 0 ]];then
            echo "包 [${update_files}] 部署失败"
            shell_unlock
            exit 0
        fi
    fi
}

#退出
trap_exit(){
    shell_unlock
    exit 10
}

#判断是JAR还是WAR进程
check_agrs(){
    check_arg=""
    if [[ $(echo ${pkg_name}|awk -F '.' '{print $NF}') == 'jar' ]];then
        check_arg="${dep_files}"
        grep_arg=' \-jar '

    else
        check_arg="${dep_path} "
        grep_arg=' \-Dcatalina.home='
    fi
}

check_process(){
    ps -ef|grep "${check_arg}"|grep ${grep_arg}|grep -v grep > tmp.txt
#    echo "" >> tmp.txt
    cpid=""

    while read line;do
        #echo "共有进程 [$(cat tmp.txt|wc -l)] 个."
        for i in $line;do
            if [[ $i =~ "-Dcatalina.home=" ]];then
                if [[ $(echo $check_arg|awk -F ' ' '{print $1}') == $(echo $i | awk -F '=' '{print $2}') ]];then
                    cpid="$cpid $(echo $line|awk -F ' ' '{print $2}')"
                fi
            elif [[ $i == ${check_arg} ]];then
                cpid="$cpid $(echo $line|awk -F ' ' '{print $2}')"
            fi
        done
    done < tmp.txt
    rm -rf tmp.txt

}

#获取PID或进程详情，接收1查PID，接收0查详情
get_process(){
    #不能添加其它输出
    check_agrs
    check_process
    if [[ ${cpid} != "" ]];then
    #	run_pid=$(ps -ef|grep "${check_arg} "|grep ${grep_arg}|grep -v grep|awk '{print $'"$1"'}');
        run_pid=$(ps -ef|grep "${cpid}"|grep -v grep|awk '{print $'"$1"'}');
    else
        run_pid=""
    fi

    echo "查找pid:${run_pid}"
}

#关闭服务
count_list=""
check_pid(){
    check_user
    show_pid
    run_pid=""
    get_process 2
    pid_count=$(expr ${pid_count} + 1)

	if [[ -n ${run_pid} ]];then
		echo "exist pid ${run_pid}"
		for pid in ${run_pid};do
            for out_pid in ${count_list};do
                if [[ ${out_pid} == ${pid} ]];then
                    out_result=1
                fi
            done
            if [[ ${out_result} -eq 1 ]];then
                out_result=0
                continue;
            fi
            echo "关闭服务 [${pkg_name} ID ${pid}] 进程 ..."

            if [[ $( echo ${check_arg}|awk -F '.' '{print $NF}') == 'jar' ]];then
			    kill ${pid}
            else
                start_file=$(echo ${check_arg}|awk -F ' ' '{print $1}')/bin/catalina.sh
                if [[ -f ${start_file} ]];then
                    ${start_file} stop
                else
                    echo "[${start_file}] not exists !!!"
                fi


            fi
            sleep 1;

			if [[ ${pid_count} -ge 5 ]];then
                pid_count=0
                echo "关闭服务 [${pkg_name} ID ${pid}] 进程失败！！！"
                echo "强制结束进程..."
                kill -9 ${pid}
                if [[ $? -eq 0 ]];then
                    echo "强制关闭服务 [${pkg_name} ID ${pid}] 进程成功！！！"
                else
                    echo "强制关闭服务 [${pkg_name} ID ${pid}] 进程失败！！！"
                fi
                count_list="${pid} ${count_list}"
            else
				check_pid
			fi
		done

	fi
}

#展示进程详情
show_pid(){
    get_process 0
}

#检查启动用户
check_user(){
    echo
#    if [[ ${pkg_name} == 'config-server-1.0-SNAPSHOT.jar' ]];then
#        if [[ $(whoami) != 'root' ]];then
#            echo "项目 [${pkg_name}] 需要使用 root 用户操作 [${backend_option[7]}] 端口 !!!"
#            shell_unlock
#            exit 0
#        fi
#    fi
}

#启动服务
start_svr(){
    check_user
    echo "###########################"
#    get_process 2
    echo $run_pid $check_arg
    echo "###########################"


    if [[ -n ${run_pid} ]];then
        echo "项目运行中... [ ${run_pid}]"
        shell_unlock
        exit 1
    fi

#    echo "开始启动JAR项目 [${check_arg}]"
    if [[ $( echo ${check_arg}|awk -F '.' '{print $NF}') == 'jar' ]];then
        echo "开始启动JAR项目 [${check_arg}] ..."
		echo "nohup ${JAVAS} ${start_option} -jar ${dep_files} ${backend_option} > ${out_file} 2>&1 &"
  	    nohup ${JAVAS} ${start_option} -jar ${dep_files} ${backend_option} > ${out_file} 2>&1 &
    else
        echo "开始启动WAR项目 [${check_arg}] ..."
        start_file=$(echo ${check_arg}|awk -F ' ' '{print $1}')/bin/catalina.sh
        if [[ -f ${start_file} ]];then
            ${start_file} start
        else
            echo "[${start_file}] not exists !!!"
        fi
    fi
    sleep 1;
    recode_pid start
	show_pid
}

#备份
backup_data(){
    if [[ ! -d ${backup_path} ]];then
        mkdir ${backup_path} -p
    fi
    if [[ $? != 0 ]];then
        echo "创建备份目录 [${backup_path}] 失败，启动服务...exit!!!"
        start_svr
        shell_unlock
        exit 2
    fi

    bak_dir=${backup_path}/${now_date}_$(basename ${dep_path})
    if [[ -d ${bak_dir} ]];then
        mkdir ${bak_dir}_1 -p
        if [[ $? != 0 ]];then
            echo "修改 [${bak_dir}] 目录为 [${bak_dir}_old] 失败，启动服务...exit!!!"
            start_svr
            shell_unlock
            exit 3
        fi
        bak_dir=${bak_dir}_1

    else
        mkdir ${bak_dir} -p
        if [[ $? != 0 ]];then
            echo "创建备份子目录 [${bak_dir}] 失败，启动服务...exit!!!"
            start_svr
            shell_unlock
            exit 4
        fi
    fi

    echo "开始备份数据到 $1..."
    rsync -ah $1 ${backup_path}/${now_date}/
    if [[ $? != 0 ]];then
        echo "创建备份数据 [$1] 到 [${backup_path}/${now_date}] 失败，启动服务...exit!!!"
        start_svr
        shell_unlock
        exit 5
    fi

    # 清理过期备份
    $(find ${backup_path} -mtime +${backup_day} -exec rm -rf {} \;)
    if [[ $? != 0 ]];then
        echo "清理 [${backup_day}天前] 备份数据失败!!!"
    fi
}


###################### 入口 ######################
shell_lock

case $1 in
	start)
        get_process 2
		start_svr
	;;
	stop)
        recode_pid stop
		check_pid
	;;
	restart)
        recode_pid stop
	    check_pid
	    start_svr
	;;
	dep)
		check_pkg
        recode_pid stop
		check_pid
		deploy_pkg
		start_svr
	;;
	pid)
	    show_pid
	;;
	*)
		echo "usage: ${0} [ start | stop | restart | dep | pid ]"
	;;
esac
shell_unlock
