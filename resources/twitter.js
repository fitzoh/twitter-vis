
var projection
var svg
var state
var county
    var ws = new SockJS('http://127.0.0.1:15674/stomp');
    var client = Stomp.over(ws);
    var on_error =  function() {
        console.log('error');
    };

    var on_connect = function() {
        client.subscribe("/amq/queue/twitter", on_message);
        console.log('connected');
    };

function validCoords(coords){
    lon = coords[0]
    longValid = -84.49 <= lon && lon <= -80.31
    lat = coords[1]
    latValid = 38.24 <= lat && lat <= 41.59
    return longValid && latValid
}
    var on_message = function(message){
        var coords = JSON.parse(message.body).coordinates.reverse()
        if (validCoords(coords)){
        var loc = projection(coords)
        var circle = svg.append('circle').attr("r", 2).attr("stroke", "steelblue").attr("cx", loc[0]).attr("cy", loc[1]).attr("fill", "steelblue")
        d3.select(circle).transition().duration(10000).attr("opacity", 0)
}
    };
    client.connect('guest', 'guest', on_connect, on_error, '/');


var width = 960,
    height = 500;

projection = d3.geo.albersUsa()
    .scale(5000)
    .translate([width*(-.50) , height* (.75)]);

var path = d3.geo.path()
    .projection(projection);

svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

d3.json("us.json", function(error, us) {
  window.us = us
  svg.insert("path", ".graticule")
      .datum(topojson.feature(us, us.objects.land))
      .attr("class", "land")
      .attr("d", path);

  county = svg.insert("path", ".graticule")
      .datum(topojson.mesh(us, us.objects.counties, function(a, b) { return a !== b && !(a.id / 1000 ^ b.id / 1000); }))
      .attr("class", "county-boundary")
      .attr("d", path);

  state = svg.insert("path", ".graticule")
      .datum(topojson.mesh(us, us.objects.states, function(a, b) { return a !== b; }))
      .attr("class", "state-boundary")
      .attr("d", path);
});

d3.select(self.frameElement).style("height", height + "px");
