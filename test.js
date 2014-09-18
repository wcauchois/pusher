var http = require('http'); 
var data = {
  "collapseKey":"applice",
  "delayWhileIdle":true,
  "timeToLive":3,
  "data":{
    "message":"My message","title":"My Title"
    },
  "registration_ids":["redacted"]
};

var dataString =  JSON.stringify(data);
var headers = {
  'Authorization' : 'key=redacted',
  'Content-Type' : 'application/json',
  'Content-Length' : dataString.length
};

var options = {
  host: 'android.googleapis.com',
  port: 80,
  path: '/gcm/send',
  method: 'POST',
  headers: headers
};

//Setup the request 
var req = http.request(options, function(res) {
  res.setEncoding('utf-8');

  var responseString = '';

  res.on('data', function(data) {
    responseString += data;
  });

  res.on('end', function() {
    //var resultObject = JSON.parse(responseString);
    console.log(responseString);
    //console.log(resultObject);
  });
  console.log('STATUS: ' + res.statusCode);
  console.log('HEADERS: ' + JSON.stringify(res.headers));

});

req.on('error', function(e) {
  // TODO: handle error.
  console.log('error : ' + e.message + e.code);
});

req.write(dataString);
req.end();

