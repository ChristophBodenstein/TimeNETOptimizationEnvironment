pushd C:\tnserver\tns
start "" "mongod" --config mongoconfig.conf
set PORT=80
npm start
popd
