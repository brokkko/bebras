// old_users = [];
db.users.find({event_id: "bebras20", _role: "SCHOOL_ORG"}).forEach(function (user) {
    user.apps = [];
    user._contests = {};
    delete user._lua;
    delete user._id;
    user._reg_by = [];
    user.event_id = "bebras21";

    // old_users.push(user);

    db.users.insert(user);
});
