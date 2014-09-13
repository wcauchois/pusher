
var express = require('express'),
    config = require('config'),
    fs = require('fs');

var app = express();

app.get('/', function(req, res) {
  res.send({'hello_bar': true});
});

app.listen(config.PORT);
