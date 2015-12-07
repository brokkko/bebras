var event_id = "babior2015";
var contests_ids = ["1-2", "3-4", "5-6", "7-8", "9-10", "11"];
for (var i = 0; i < contests_ids.length; i++) {
    var cid = contests_ids[i];

    print("processnig " + cid);

    var from_cid = "vk-" + cid;
    var collection_from_name = "contest-" + event_id + "_" + from_cid;
    var collection_to_name = "contest-" + event_id + "_" + cid;

    var part_ids = db[collection_from_name].distinct("u");

    for (var j = 0; j < part_ids.length; j++) {
        var part_id = part_ids[j];

        var user = db.users.findOne({_id: part_id});

        if ((db[collection_to_name].count({u: part_id})) > 0) {
            print("user " + part_id + " " + user.login + " participated both in vk and normal contests!");
            continue;
        }

        //copy this user
        print("copy user " + part_id + " " + user.login);

        var seed = user._contests[from_cid].seed;
        var sd = user._contests[from_cid].sd;
        var fd = user._contests[from_cid].fd;

        var seed_field = "_contests." + cid + ".seed";
        var sd_field = "_contests." + cid + ".sd";
        var fd_field = "_contests." + cid + ".fd";
        var updater = {};
        updater["$set"] = {};
        updater["$set"][seed_field] = seed;
        updater["$set"][sd_field] = sd;
        updater["$set"][fd_field] = fd;

        db.users.update({_id: part_id}, updater);
        db[collection_from_name].find({u: part_id}).forEach(function (d) {
            db[collection_to_name].insert(d);
        });
    }
}
