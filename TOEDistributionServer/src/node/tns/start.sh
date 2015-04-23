#!/bin/bash
mongod --config mongoconfig.conf
set PORT=8080
export PORT
npm start
