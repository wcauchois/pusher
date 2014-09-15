
var express = require('express'),
    config = require('config'),
    fs = require('fs'),
    bodyParser = require('body-parser'),
    logger = require('morgan'),
    path = require('path'),
    util = require('util'),
    gcm = require('node-gcm');

var app = express();

app.use(logger('short'));
app.use(bodyParser.urlencoded({extended: false}));

app.get('/', function(req, res) {
  res.send({'hello_bar': true});
});

app.post('/test_send', function(req, res) {
  var msg = req.body['msg'] || 'Test message';
  var message = new gcm.Message({
    data: {key1: msg}
  });
  var sender = new gcm.Sender('SECRET');
  var registrationIds = [testRegId];
  sender.send(message, registrationIds, 4, function(err, result) {
    console.log(err);
    console.log(result);
    res.status(200).end();
  });
});

app.post('/register_device', function(req, res) {
  var regId = req.body['regId'];
  util.log('Received registration ID: ' + regId);
  res.status(200).end();
});

app.listen(config.PORT);
