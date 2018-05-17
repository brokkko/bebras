var rdk = db.users.findOne({event_id: "kio18", login: "rdk"});
// var rdk = db.users.findOne({event_id: "kio18", login: "ofima"});

printjson(rdk._contests.lamps0);

// var submissions = db['contest-kio18_elasticity0'].find({u: rdk._id}).sort({lt: 1});
var submissions = db['contest-kio18_lamps0'].find({u: rdk._id}).sort({lt: 1});
submissions.forEach(function (submission) {
    print('--- next submission ---');
    printjson(submission);
    printjson(submission.c);
});

print('--------------------------------');
printjson(rdk._contests.lamps0);

print("rdk id is " + rdk._id);
// db['contest-kio18_elasticity0'].update({u: rdk._id}, {$unset: {c: 1}}, {multi: true});
// db['contest-kio18_elasticity0'].update({}, {$unset: {c: 1}}, {multi: true});