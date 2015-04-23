#!/bin/bash
mongod --config mongoconfig.conf
set PORT=8080
export PORT=8080
npm start
