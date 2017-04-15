iposov = db.users.findOne({event_id: "babior2015", login: "iposov"});
db.users.update({event_id: "babior2015", login: "iposov-o6"}, {$set: {passhash: iposov.passhash, cfrmd: true}});
