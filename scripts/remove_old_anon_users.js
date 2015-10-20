//find anon users
var dateSomeAgo = new Date();
var days = 5;
dateSomeAgo.setTime(dateSomeAgo - days * 24 * 60 * 60 * 1000);

allOldAnons = db.users.find({"_role": "ANON", "_lua.d": {"$lt": dateSomeAgo}});
ids = [];
allOldAnons.forEach(function(anon) {
    ids.push(anon._id);
});

print("found users to remove: " + ids.length);

remove("activity", {"u": {"$in": ids}});

collectionNames = db.getCollectionNames();
for (var i = 0; i < collectionNames.length; i++) {
    var collectionName = collectionNames[i];

    var contestNameStart = collectionName.indexOf("contest-");
    if (contestNameStart != 0)
        continue;

    remove(collectionName, {"u" : {"$in": ids}});
}

print("finally, removing the users");

remove("users", {"_role": "ANON", "_id" : {"$in": ids}});

function remove(collectionName, query) {
    print("removing from " + collectionName);
    var c = db[collectionName].count(query);
    db[collectionName].remove(query);
    print("removed : " + c);
    print("compacting...");
    db.runCommand({compact: collectionName});
}