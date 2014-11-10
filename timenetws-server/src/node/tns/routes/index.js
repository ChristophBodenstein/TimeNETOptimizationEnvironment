var express = require('express');
var router = express.Router();
var mkdirp = require('mkdirp');


// Required modules for form handling
var express = require('express'),
    http = require('http'),
    formidable = require('formidable'),
    fs = require('fs'),
    path = require('path');

/* GET home page. */
router.get('/', function(req, res) {
  console.log("Delivering index-page.");
  res.render('index', { title: 'TimeNET distribution server' });
});

// Upload route.
router.post('/rest/file/upload', function(req, res) {  
  var form = new formidable.IncomingForm();
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
	          }
	        });
	      });
	    });


	});
  });
});

module.exports = router;
