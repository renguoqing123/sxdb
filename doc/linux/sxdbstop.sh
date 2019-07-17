now_date=$(date +%Y%m%d_%H%M%S)
pid_8089=$(netstat -nlp | grep :8089 | awk '{print $7}' | awk -F"/" '{ print $1 }')
echo "当前时间：${now_date}"
echo "进程pid_8089: ${pid_8089}"
if [ ! $pid_8089 ]; then
	echo "8089端口没有占用"
else
	echo "关闭8089进程"
	kill -9 $pid_8089
fi
