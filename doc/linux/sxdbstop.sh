now_date=$(date +%Y%m%d_%H%M%S)
pid_6609=$(netstat -nlp | grep :6609 | awk '{print $7}' | awk -F"/" '{ print $1 }')
echo "当前时间：${now_date}"
echo "进程pid_6609: ${pid_6609}"
if [ ! $pid_6609 ]; then
	echo "6609端口没有占用"
else
	echo "关闭6609进程"
	kill -9 $pid_6609
fi
