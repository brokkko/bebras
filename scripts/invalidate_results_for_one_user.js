var event_id = "bebras17";
var login = "46850635.10";
var contest_id = 'contest-bebras17_5-6';

db = db.getMongo().getDB("dces2");


var userQuery = {event_id: event_id, login: login};
var user = db.users.findOne(userQuery);

printjson(user._contests['5-6'].res);

printjson(db.users.findOne(userQuery, {'_contests.5-6.res' : 1}));
db.users.update(userQuery, {$set: {'_contests.5-6.res' : null}});

// 90608483.1