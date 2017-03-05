#!/bin/bash

RESULT=`ps aux | awk '$11="python"' | awk '$12=="/home/ec2-user/Code/ws.py"'`

if [ "${RESULT:-null}" = null ] ; then
echo "not running"
python /home/ec2-user/Code/ws.py
else 
echo "running"
fi

