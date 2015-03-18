var express = require('express');
var router = express.Router();

/* GET users listing. */
router.get('/userlist', function(req, res) {
  var db=req.db;

  db.collection('usercollection').find().toArray(function(err, items){
    res.json(items);
  });

});

module.exports = router;
