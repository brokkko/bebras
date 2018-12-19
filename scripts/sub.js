var event_id = 'bebras18';
var year = 2018;

function user(login) {
    return db.users.findOne({event_id: event_id, login: login});
}

function uid(login) {
    return user(login)._id;
}

function pid(name) {
    return db.categories.findOne({link: {$regex: year + '/' + name + '$'}}).pid;
}

function grade_2_contest(g) {
    g = +g;
    if (g === 11)
        var gg = '11';
    else if (g % 2 === 0)
        gg = (g - 1) + '-' + g;
    else
        gg = g + '-' + (g + 1);

    return 'contest-' + event_id + '_' + gg;
}

function sub(login, task) {
    var u = user(login);
    var t = pid(task);
    var g = u.grade;
    var c = grade_2_contest(g);

    db[c].find({u: u._id, pid: t}, {lt: 1, st: 1, a: 1}).forEach(function(d) {printjson(d);})
}

function suball(login) {
    var u = user(login);
    var g = u.grade;
    var c = grade_2_contest(g);

    return db[c].find({u: u._id}).toArray();
}