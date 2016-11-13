#!/bin/bash
mongod --config mongoconfig.conf &
set PORT=80
export PORT=80
while true; do
npm start
done
