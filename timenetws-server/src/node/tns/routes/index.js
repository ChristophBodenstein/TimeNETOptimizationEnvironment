var express = require('express');
var router = express.Router();

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
  console.log("Incomming Post-Request.");
  var form = new formidable.IncomingForm();
  form.parse(req, function(err, fields, files) {
    // `file` is the name of the <input> field of type `file`
    console.log("Parsing incomming Post-Request.");
    var old_path = files.file.path,
        file_size = files.file.size,
        file_ext = files.file.name.split('.').pop(),
        index = old_path.lastIndexOf('/') + 1,
        file_name = old_path.substr(index),
        new_path = path.join(process.env.PWD, '/uploads/', file_name + '.' + file_ext);
    console.log("Filename: "+file_name);
    console.log("Will be stored: "+new_path);
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

module.exports = router;
