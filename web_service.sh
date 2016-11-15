#!/bin/bash

RESULT=`ps aux | awk '$11="python3"' | awk '$12=="/home/ec2-user/code/ws.py"'`

if [ "${RESULT:-null}" = null ] ; then
echo "not running"
python3 /home/ec2-user/code/ws.py
else 
echo "running"
fi

