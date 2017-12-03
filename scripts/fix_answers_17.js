// fix answers for users 54056202608, 46211323.9
// 54056202608: ObjectId("59fc62e66276eea19127425e")
// 46211323.9:  ObjectId("5a082b93fdb1771e6fbfe5fa")


var logins = ['54056202608', '46211323.9'];
var contest_collection = 'contest-bebras17_5-6';
var pid = ObjectId('59f4b9b36276eea19121b830');
var correct = '{"0":15,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}';

for (var i = 0; i < logins.length; i++) {
    var login = logins[i];
    var user = db.users.findOne({event_id: 'bebras17', login: login});
    print(user._id);

    var query = {'u': user._id, 'pid': pid, 'a.r': 2};

    db[contest_collection].find(query).forEach(function(d) {
        printjson(d);
    });

    // db[contest_collection].update({'u': user._id, 'pid': pid}, {'$set': {'a.s'}});
}

var c = '{"0":-1,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}';
var res = c;
for (var i = 9; i <= 15; i++) {
    res += '{{{OR}}}' + c.replace(/-1/g, '' + i);
}
print(res);

// {"0":-1,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}{{{OR}}}{"0":9,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}{{{OR}}}{"0":10,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}{{{OR}}}{"0":11,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}{{{OR}}}{"0":12,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}{{{OR}}}{"0":13,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}{{{OR}}}{"0":14,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}{{{OR}}}{"0":15,"1":12,"2":9,"3":11,"4":14,"5":13,"6":10}