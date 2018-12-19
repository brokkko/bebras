//Дубровская? HR-08
db['contest-bebras18_9-10'].find({
    u: ObjectId("5befab9698ec007cce36a996"),
    pid: ObjectId("5bc3998c98ec00bf56194806")
});

db['contest-bebras18_9-10'].insert({
    u: ObjectId("5befab9698ec007cce36a996"),
    pid: ObjectId("5bc3998c98ec00bf56194806"),
    a: {"r": 2, "s": "1110000001000001100000100000010000001101110011101"},
    "lt": NumberLong(2287739),
    "st": ISODate("2018-11-17T07:05:25.580Z")
});

//Истомин HR-08 86627231.4 5be978d398ec003a3d65b2a8
db['contest-bebras18_9-10'].find({
    u: ObjectId("5be978d398ec003a3d65b2a8"),
    pid: ObjectId("5bc3998c98ec00bf56194806")
});

db['contest-bebras18_9-10'].update({
        u: ObjectId("5be978d398ec003a3d65b2a8"),
        pid: ObjectId("5bc3998c98ec00bf56194806")
    },
    {
        $set: {
            a: {"r": 2, "s": "1110000001000001100000100000010000001101110011101"}
        }
    });


//Яна Савикова 81124241.4
var i = user("iposov");
var s = user("81124241.4");
var seed = s._contests['3-4'].seed;
db.users.update({_id: i._id}, {$set: {'_contests.3-4.seed': seed}});
//copy all from iposov
var c = grade_2_contest(s.grade);
db[c].update({u: i._id}, {$set: {u: s._id}});
db.users.update({_id: s._id}, {$set: {'_contests.3-4.sd': i._contests['3-4'].sd}});
db.users.update({_id: s._id}, {$set: {'_contests.3-4.fd': i._contests['3-4'].fd}});

//from artem.uliana@bk.ru  Марина Корнева
/*
Засчитать пустыню и Мост из Камешков

88253122.1 Тихонов Олег
88253121.7 Елизавета Ильина
88253121.6 Владимир  Вознюк
88253121.4 Тимерхан Зарипов
88253121.3 Ринат Фаттахов

Tакже не загрузилось задание Мост из камешков у них и у  участников:
(Засчитать Мост из Камешков)
88253121.10  Саттаров Амир
88253121.9 Ханипов Адель
88253121.8 Усанов Богдан
*/

var desert = ['88253122.1', '88253121.7', '88253121.6', '88253121.4', '88253121.3'];
var bridge = desert.slice();
bridge.splice(0, 0, '88253121.10', '88253121.9', '88253121.8');

bridge.forEach(function(login) {
    var u = user(login);
    print(u.name + ' ' + u.surname + ' ' + login + ' grade:' + u.grade);
});

var desert_pid = pid('HR-08');
var bridge_pid = pid('KR-05');

var desert_correct = '1110000001000001100000100000010000001101110011101';
var bridge_correct = '479';

function create_submission(uid, pid, answer) {
    return {
        "u": uid,
        "lt": NumberLong(116714),
        "st": ISODate("2018-12-11T09:28:10.676Z"),
        "pid": pid,
        "a": {"r": 2, "s": answer}
    };
}

desert.forEach(function(login) {
    var u = user(login);
    var contest = grade_2_contest(u.grade);
    db[contest].insert(create_submission(
        u._id,
        desert_pid,
        desert_correct
    ));
});

bridge.forEach(function(login) {
    var u = user(login);
    var contest = grade_2_contest(u.grade);
    db[contest].insert(create_submission(
        u._id,
        bridge_pid,
        bridge_correct
    ));
});