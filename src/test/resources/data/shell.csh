#!/bin/csh
myfile='cars.txt'

# Create the file
touch $myfile

if [ -f $myfile ]; then
   rm cars.txt
   echo "$myfile deleted"
fi
