var event_id = 'bebras20';
var year = 2020;

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

    db[c].find({u: u._id, pid: t}, {lt: 1, st: 1, a: 1}).forEach(function (d) {
        printjson(d);
    })
}

function suball(login) {
    var u = user(login);
    var g = u.grade;
    var c = grade_2_contest(g);

    return db[c].find({u: u._id}).toArray();
}

tasks = {
    'NZ-02': {
        r: 2,
        ans: '43'
    },
    'US-04': {
        r: 1,
        ans: '{"12":3,"13":1,"14":4,"15":8,"16":11,"17":2,"18":9,"19":5,"20":6,"21":7,"22":10,"23":-1}'
    },
    'IE-01b': {
        r: 2,
        ans: '{"1":7,"2":6,"3":10,"4":8,"5":9}'
    },
    'LT-08': {
        r: 2,
        ans: 'ЛППЛЛП'
    },
    'LT-04': {
        r: 2,
        ans: '245315677'
    },

    'PT-02a': {
        a: 3
    },
    'LT-12': {
        r: 2,
        ans: '10000010'
    },
    'UY-01': {
        a: 2
    },
    'PT-06': {
        a: 2
    },
    'SK-04': {
        a: 3
    },
    'RS-04': {
        a: 0
    },
    'IS-02': {
        a: 0
    },
    'IE-08': {
        a: 3
    },
    'RO-03': {
        a: 1
    },
    'IE-09': {
        a: 0
    }
};

var accept = [
    ['80579415.1', 'NZ-02'], // Кириленко Георгий от Ольга Мананкова
    ['82570091.16', 'US-04'], // Максим Ли
    ['82570091.16', 'IE-01b'],
    ['85229501.32', 'IE-01b'], // Нешев
    ['89303163.2', 'LT-08'], // Казахи
    ['89303163.2', 'LT-04'],
    ['89303163.1', 'LT-08'],
    ['89303163.1', 'LT-04'],
    ['89303162.2', 'LT-04'],
    ['89303162.3', 'LT-08'],

    ['80185471.4', 'US-04'], // Ева Кузьменко
    ['80185471.4', 'PT-02a'],
    ['80185471.4', 'LT-12'],
    ['80185471.4', 'UY-01'],
    ['80185471.4', 'PT-06'],
    ['80185471.4', 'SK-04'],
    ['80185471.4', 'RS-04'],
    ['80185471.4', 'IS-02'],
    ['80185471.4', 'IE-08'],
    ['80185471.4', 'RO-03'], // ошибка
    ['80185471.4', 'IE-09'],
    ['80185471.4', 'IE-01b']
];

for (var i = 0; i < accept.length; i++) {
    var login = accept[i][0];
    var task = accept[i][1];

    var p = pid(task);
    var u = user(login);

    var task_answer_info = tasks[task];

    var task_answer;
    if ('a' in task_answer_info)
        task_answer = {"a": task_answer_info.a};
    else
        task_answer = {"r": tasks[task].r, "s": tasks[task].ans};

    var submission = {
        "u": u._id,
        "lt": NumberLong(39 * 60 * 1000), //39 Min
        "st": ISODate("2019-12-11T09:28:10.676Z"),
        "pid": p,
        "a": task_answer
    };

    var contest = grade_2_contest(u.grade);

    print("inserting to " + contest + ": ");
    printjson(submission);
    db[contest].insert(submission);
}
