pushd C:\tnserver\tns
start "" "mongod" --config mongoconfig.conf
set PORT=8080
npm start
popd