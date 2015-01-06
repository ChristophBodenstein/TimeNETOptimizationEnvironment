pushd C:\tnserver\tns
start "" "mongod" --dbpath data
set PORT=8080
npm start
popd