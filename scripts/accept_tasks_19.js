var event_id = 'bebras19';
var year = 2019;

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

//Хайтул Миша, 4кл: 88367221.3
//  Бобромонеты CH-03b, Домашний зоопарк ID-02b, Снеговики и шапки LT-07, Перекрестки RO-04


//5 кл логины: 85218882.2; 85218881.5
//  Перекрестки RO-04

//83578061.8    flokin92    Глеб    Супранович    3
// 83578062.1    loweal33    Дарья    Фомичёва    6
// Домашний зоопарк ID-02b, Перекрестки RO-04

tasks = {
    'CH-03b': {
        r: 1,
        ans: '{"0":32,"1":22,"2":17,"3":-1,"4":-1,"5":-1,"6":-1,"7":-1}'
    },
    'RO-04': {
        r: 2,
        ans: '{"1":6,"2":5,"3":4}'
    },
    'LT-07': {
        r: 2,
        ans: '{"8":3,"9":-1,"10":1,"11":2}'
    },
    'ID-02b': {
        r: 1,
        ans: '{"6":14,"7":17,"8":16,"9":12,"10":15,"11":13}'
    },
    'RU-02': {
        r: 2,
        ans: '380'
    },
    'KR-04': {
        r: 2,
        ans: '3'
    }
};

var accept = [
    // ['88367221.3', 'CH-03b'],
    // ['88367221.3', 'ID-02b'],
    // ['88367221.3', 'LT-07'],
    // ['88367221.3', 'RO-04'],
    // ['85218882.2', 'RO-04'],
    // ['85218881.5', 'RO-04'],
    // ['83578061.8', 'RO-04'],
    // ['83578062.1', 'RO-04'],
    ['89274172.17', 'RU-02'], // От Сучкова, не записалось решение

    //Мананкова зачесть стрелки
    ['85218881.3', 'RO-04'], //Семен ИЗОТОВ
    ['85218881.1', 'RO-04'], //Даниил Штипс
    ['85218881.5', 'RO-04'], //Семен Изотов

    ['86788581.43', 'KR-04'] //Аманов Азамат точно помнит, что нажал
];

for (var i = 0; i < accept.length; i++) {
    var login = accept[i][0];
    var task = accept[i][1];

    var p = pid(task);
    var u = user(login);

    var submission = {
        "u": u._id,
        "lt": NumberLong(39 * 60 * 1000), //39 Min
        "st": ISODate("2019-12-11T09:28:10.676Z"),
        "pid": p,
        "a": {"r": tasks[task].r, "s": tasks[task].ans}
    };

    var contest = grade_2_contest(u.grade);

    print("inserting to " + contest + ": ");
    printjson(submission);
    db[contest].insert(submission);
}

// от Ольга Мананкова <manankova_ov_25@mail.ru>, какой-то ученик, Перекрестки RO-04
/*
85218883.2 	mallep49 	Илья 	Голодных 	5 	А 	107 	13 	2 	0 	114 	-7 	19 	1
85218883.1 	ridive51 	Данил 	Клиценко 	5 	Б 	83 	11 	3 	1 	93 	-10 	131 	5
85218882.3 	andebr65 	Виктория 	Хан 	5 	Д 	95 	12 	3 	0 	105 	-10 	54 	2
85218882.2 	mateto23 	Виктор 	Карамышев 	5 	А 	36 	6 	5 	4 	51 	-15 	692 	14
85218882.1 	usiali13 	Иван 	Сущенко 	5 	В 	95 	12 	3 	0 	105 	-10 	54 	2
85218881.5 	aldres13 	Давид 	Синогин 	5 	Б 	83 	11 	4 	0 	96 	-13 	131 	5
85218881.4 	ticent15 	Иван 	Шемелин 	5 	А 	95 	12 	3 	0 	105 	-10 	54 	2
85218881.3 	helfre37 	Семён 	Изотов 	5 	Д 	70 	10 	4 	1 	84 	-14 	255 	9
85218881.2 	niesin98 	Даниель 	Банников 	7 	В 	21 	6 	6 	3 	42 	-21 	536 	18
85218881.1 	tficou30 	Даниил 	Штипс 	5 	В 	71 	10 	4 	1 	84 	-13
*/

// ???????????????????????

// Егор Волков 86030611.9, удалить 03:34|RO-03|а
// 00:00|p0 02:45|RO-03|Б 03:34|RO-03|а 04:03|p1 08:23|RU-02|[R380] 08:24|p2 08:27|p14 11:11|KR-04|[w4] 11:11|p13 12:17|KR-05|Б 13:03|p12 14:02|p12 14:04|p12 16:24|p11 16:26|p12 16:27|p11 16:29|p12 23:06|UK-02|[w44] 23:07|p11 24:35|CZ-03c|[R21] 24:36|p10 26:21|SK-04|w 26:22|p9 27:08|US-02|[R11101] 27:09|p8 28:33|TH-08|R 28:33|TH-08|. 28:57|TH-08|R 28:58|p7 29:53|UA-02|R 29:55|UA-02|. 29:57|UA-02|R 29:58|p6 32:12|RO-02|[w39] 32:13|p5 33:43|AT-03|[R0101] 33:44|p4 34:58|DE-03|R 34:59|p3 36:38|LT-06|[R7] 36:39|p2 36:42|p1 36:43|p2 37:54|BE-02|[w9]

// db['contest-bebras19_9-10'].remove({_id: ObjectId("5dc515f898ec006b8178e02f")});