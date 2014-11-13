var express = require('express');
var router = express.Router();
var mkdirp = require('mkdirp');
var path = require('path');



// Required modules for form handling
var express = require('express'),
    http = require('http'),
    formidable = require('formidable'),
    fs = require('graceful-fs'),
    path = require('path');
var DEFAULT_SLEEPING_TIME=2000;// in ms
var DEFAULT_MINIMUM_TIMEOUT=500;//in sec

/* GET home page. */
router.get('/', function(req, res) {
	var db=req.db;
	var activeclients=db.collection('activeclients');
  	console.log("Delivering index-page.");
  
	//activeclients.count(function(err, count){
	res.render('index', { title: 'TimeNET distribution server', clientcount:Math.round(global.clientcount)});
	//});
	

});

// Handles uploads of simulation files
router.post('/rest/file/upload', function(req, res) {  
  	var form = new formidable.IncomingForm();
	var db= req.db;
	var simlist=db.collection('simlist');

	/*db.collection('simlist').find().toArray(function(err, result) {
		if (err) throw err;
		console.log(result);
	});
	*/

  	form.parse(req, function(err, fields, files) {
    var old_path = files.attachment.path,
        file_size = files.attachment.size,
        file_name = files.attachment.name,
		simid = fields.simid,
		new_dir = './uploads/'+simid,
        new_path = './uploads/'+simid+"/"+file_name;
	mkdirp(new_dir, function(err) { 
	    // path was created unless there was error
		if(err){
			console.log("There was an error creating dir:"+new_path);
		}
		
	    var data = fs.readFile(old_path, function(err, data) {
		//console.log("Will try to write new file.");
	      fs.writeFile(new_path, data, function(err){
			  //console.log("Will try to delete old file.");
	        fs.unlink(old_path, function(err) {
	          if (err) {
				console.log("Error unlinking old file."+old_path);
	            res.status(500);
	            res.json({'success': false});
	          } else {
			  
				//Enter simulation data into Mongodb
				//console.log("Try to enter into db.");
				  	simlist.insert({name: file_name, simid: simid, path: new_path, distributed: false, simulated: false, logdownloaded: false, logname:"", timestamp: Date.now()}, function(err, result){
		            	if (err) {
							console.log("Error entering simulation into db.");
		              	res.status(500);
		              	res.json({'success': false});
		            	} 	else {
		              	  	res.status(200);
		              		res.json({'success': true});
		  					}
			  
					});
				} 
	       });
	      });
		});
  	});
	});
  return true;
});


//Handles simulation-requests from sim-slaves
router.get('/rest/api/downloads/ND', function(req, res){
	var db= req.db;
	var simlist=db.collection('simlist');
	var activeclients=db.collection('activeclients');
	//Update one from not distributed to distributed
	//return this one as file to client

	//Mark Request in DB for Statistics
  	activeclients.insert({ip:req.connection.remoteAddress, timestamp: Date.now()}, function(err, result){
    	if (err) {
			console.log("Error updating client-collection.");
    	} 	else {
			}

	});

	//Remove old client requests from DB
	var borderTimeStamp=Date.now()-DEFAULT_SLEEPING_TIME;
  	activeclients.find(function(err, resultcursor){
    	if (err) {
			console.log("Error reading from client-collection.");
    	} 	else {
					resultcursor.each(function(err, result){
						if(result!=null){
							if(result.timestamp<=borderTimeStamp){
							activeclients.remove({timestamp: result.timestamp},function(err, result){
								if(err){
									console.log("Error removing client from activeclientlist.");
								}
							});
							}
						}
					});
			}

	});
	activeclients.count(function(err, count){
	if(global.clientcount==null)global.clientcount=0;
	if(global.clientcount==NaN)global.clientcount=0;
	global.clientcount=(global.clientcount*140 + count*10)/150;
	});

	simlist.findAndModify(
		{distributed:false},
		[],
		{$set: {distributed:true, timestamp:Date.now()}},
		false,
		true,
		function(err, result){

			if(err){
			console.log("Error updating a non-distributed simulation-entry.");
		}else{
				if(result) {
					console.log("Delivering file " + result.path);

					var options = {
						headers: {
							'filename': result.name,
							'simid': result.simid
						}
					};

					var fileName = path.resolve(result.path);
					res.sendFile(fileName, options, function (err) {
						if (err) {
							console.log(err);
							res.status(err.status).end();
						}
						else {
							console.log('Sent:', fileName);
						}
					});
				}else{
					//console.log("Will answer with res-code 500. No Simfiles available. Will check for timedoutsims.");
					res.status(500);
					res.json({'success': false});
					//TODO check for timeouts only here!

					checkForTimedOutSimulations(simlist, function(err){
						if(err){
							console.log("Error checking for TimedOutSimulations.");
						}

					});
				}


		}

	});



});

//Checks if any simulation is longer distributed then in MaxTime allowed.
//If yes, then rest it to undistributed
function checkForTimedOutSimulations(simlist, cb){
	//Check if timeout for one or more sims was, then reset it to undistributed
	simlist.find({distributed:true, simulated:false}, function(err, result){
		if(err){
			console.log("Error finding distributed and unsimulated simulations in db.");
		}else{
			if(result!=null){
				result.each(function(err, element){
					if(element!=null) {
						//todo get timeout from filename
						getMaxTimeFromFileName(element.name, function(maxtime){

							var localtimeout = maxtime;//sec
							var now=Date.now();//ms

							//Rest of logic is same
							if (localtimeout < DEFAULT_MINIMUM_TIMEOUT) {
								localtimeout = DEFAULT_MINIMUM_TIMEOUT;
							}
							localtimeout = localtimeout * 1000;
							localtimeout = element.timestamp + localtimeout;
							/*console.log("------------------");
							 console.log("ELM:" + element.timestamp);
							 console.log("Now:" + now);
							 console.log("Tim:" + localtimeout);
							 console.log("------------------");*/
							if (localtimeout <= now) {
								//console.log("Try to reset entry to undistributed.");
								//set it to undistributed
								simlist.findAndModify(
									{_id: element._id},
									[],
									{$set: {distributed: false, timestamp: Date.now()}},
									false,
									true,
									function (err, result) {
										if (err) {
											console.log("Error resetting to undistributed.");
											cb(err);
										}
									});

							}

						});

					}
				});
			}
		}
	});
}

//Extracts the maximum time from filename of simulation
function getMaxTimeFromFileName(filename, cb){
	var maxtime=500;
	var tmpString=filename.split('_MaxTime_')[1];
	tmpString=tmpString.split('_EndTime_')[0];
	tmpString=tmpString.split('.')[0];
	maxtime=tmpString;
	cb(maxtime);
}

//Handles uploads of log-files (simulation results) from sim-slaves
router.post('/rest/log/upload', function(req, res){
	var form = new formidable.IncomingForm();
	var db= req.db;
	var simlist=db.collection('simlist');

	form.parse(req, function(err, fields, files) {
		var old_path = files.attachment.path,
			file_size = files.attachment.size,
			file_name = files.attachment.name,
			simid = fields.simid,
			new_dir = './uploads/'+simid,
			new_path = './uploads/'+simid+"/"+file_name;

			mkdirp(new_dir, function(err) {
			// path was created unless there was error
			if(err){
				console.log("There was an error creating dir:"+new_path);
			}

			fs.readFile(old_path, function(err, data) {
				fs.writeFile(new_path, data, function(err) {
					fs.unlink(old_path, function(err) {
						if (err) {
							res.status(500);
							res.json({'success': false});
						} else {
							res.status(200);
							res.json({'success': true});

							//Update Entry in db
							var xmlname=file_name.split("_simTime")[0]+"_.xml";//  slice(0,-4)+".xml"
							simlist.findAndModify(
								{simid:simid, name:xmlname},
								[],
								{$set: {simulated:true, logname:file_name}},
								false,
								true,
								function(err, result){
									if(err){
									console.log("Error updating "+xmlname+" to simulated:true.");
									}
								});


						}
					});
				});
			});

		});
	});

});


router.get('/rest/api/downloads/log/:simid', function(req, res){
	var db= req.db;
	var simlist=db.collection('simlist');
	var simid=req.params.simid;
	//console.log("Asking for logfiles for: "+simid);

	simlist.findOne({simid: simid, simulated:true, logdownloaded:false}, function(err, result){

		if(err){
			console.log("Error searching logfiles for: "+req.params.simid);
		}
		if(result) {
			//console.log("findOne was sucessful.");
			//console.log(result.name);
			var logfilepath=(result.path.split(result.name))[0] +result.logname ;//.split(result.name)[0]+"/"+result.logfilename;
			//console.log("Delivering logfile " + logfilepath);

			var options = {
				headers: {
					'filename': result.logname,
					'simid': result.simid
				}
			};

			var fileName = path.resolve(logfilepath);
			var xmlPath = path.resolve(result.path);
			res.sendFile(fileName, options, function (err) {
				if (err) {
					console.log(err);
					res.status(err.status).end();
				}
				else {
					console.log('Sent:', fileName+". Try to update db.");
					simlist.updateById(result._id, {$set: {logdownloaded: true}}, function(err, res){
						if (err){
							console.log("Error updating"+ result.name);
						}
						if(res){
							console.log("Will delete file:"+fileName);
							var fs = require('fs');
							fs.unlink(fileName);
							console.log("Will delete file:"+xmlPath);
							fs.unlink(xmlPath);
						}
					});
				}
			});
		}else{
			//console.log("Will answer with res-code 500. No Logfile available.");
			res.status(500);
			res.json({'success': false});
		}

		//TODO ggf. Counter hochzählen
	});


});




module.exports = router;
