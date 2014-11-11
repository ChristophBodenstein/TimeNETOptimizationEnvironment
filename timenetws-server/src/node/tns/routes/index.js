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

/* GET home page. */
router.get('/', function(req, res) {
  console.log("Delivering index-page.");
  res.render('index', { title: 'TimeNET distribution server' });
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
		
	    fs.readFile(old_path, function(err, data) {
	      fs.writeFile(new_path, data, function(err) {
	        fs.unlink(old_path, function(err) {
	          if (err) {
	            res.status(500);
	            res.json({'success': false});
	          } else {
	            res.status(200);
	            res.json({'success': true});
			  	//Close file handle
			  	/*fs.close(old_path, function(err){
					console.log("Error closing old_path: "+old_path);
				});*/

				//Enter simulation data into Mongodb

				  simlist.insert({name: file_name, simid: simid, path: new_path, distributed: false, simulated: false, logdownloaded: false, logname:"", timestamp: Date.now()}, function(err, result){
					/*if(result) {
						//console.log("Added " + file_name + " to DB.");
					}
					if(err){
						console.log("Error inserting into DB. Maybe it already exists, will update.");
						simlist.findOne({name: file_name}, function(err, result){
						console.log("Found Object in db: "+result);

							if(err){
								console.log("Error updating data for: "+file_name);
							}
						});

						simlist.count(function(err, result){
							console.log("There are "+result + "Entries in db.");
						});

						simlist.update({name: file_name, simid: simid}, {$set: {path: new_path, distributed: false, simulated: false, logdownloaded: false, timestamp: Date.now()}}, function(err, result){
							if(err){
								console.log("Error updating data for: "+file_name);
							}
						});
					}*/

				  });

	          }
	        });
	      });

	    });
		/*fs.close(new_path, function(err){
			console.log("Error closing new_path: "+new_path);
		});*/


	});
  });
});


//Handles simulation-requests from sim-slaves
router.get('/rest/api/downloads/ND', function(req, res){
	var db= req.db;
	var simlist=db.collection('simlist');
	//Update one from not distributed to distributed
	//return this one as file to client


	simlist.findAndModify(
		{distributed:false},
		[],
		{$set: {distributed:true}},
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
					console.log("Will answer with res-code 500. No Simfiles available.");
					res.status(500);
					res.json({'success': false});

				}


		}

	});


	/*simlist.update({name: file_name, simid: simid, path: new_path, distributed: false, simulated: false, logdownloaded: false, timestamp: Date.now()}, function(err, result){
		if(err){
			console.log("Error updating data for: "+file_name);
		}
	});*/

});

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
	console.log("Asking for logfiles for: "+simid);

	simlist.findOne({simid: simid, simulated:true, logdownloaded:false}, function(err, result){

		if(err){
			console.log("Error searching logfiles for: "+req.params.simid);
		}
		if(result) {
			console.log("findOne was sucessful.");
			console.log(result.name);
			var logfilepath=(result.path.split(result.name))[0] +result.logname ;//.split(result.name)[0]+"/"+result.logfilename;
			console.log("Delivering logfile " + logfilepath);

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
			console.log("Will answer with res-code 500. No Logfile available.");
			res.status(500);
			res.json({'success': false});
		}

		//TODO ggf. Counter hochzählen
	});


});




module.exports = router;
