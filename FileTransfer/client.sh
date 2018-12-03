#!/bin/sh
DATAPORT=30910
rm test.txt
rm tester.txt
echo '---Test for list'
python3 ftclient.py localhost 30050 -l $DATAPORT
echo '---Test for short file'
python3 ftclient.py localhost 30050 -g tester.txt $DATAPORT
echo '---Test for long file'
python3 ftclient.py localhost 30050 -g test.txt $DATAPORT
echo '---Test for file not found'
python3 ftclient.py localhost 30050 -g testdd.txt $DATAPORT
echo '---Test for -l filename ERROR'
python3 ftclient.py 30050 -l text.txt $DATAPORT
echo '---Test for bad port numer ERROR'
python3 ftclient.py localhost 30053 -l $DATAPORT
echo '---Test for no options ERROR'
python3 ftclient.py localhost 30050 $DATAPORT
echo '---Test for no dataport ERROR'
python3 ftclient.py localhost -l
echo '---Test for invalid command option'
python3 ftclient.py localhost 30050 -t $DATAPORT