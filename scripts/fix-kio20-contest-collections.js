
function heatTask(level) {
    return [
        {
            name: 'e',
            title: 'Вычисление',
            view(v) {
                if (v === 0)
                    return "...";
                return "вычислено";
            },
            ordering: 'maximize'
        },
        {
            name: 'p',
            title: 'Процент нагретого',
            ordering: 'maximize',
            view(v) {
                if (v === -1)
                    return "?";
                return v.toString()
            }
        },
        {
            name: 't',
            title: 'Время',
            ordering: 'minimize',
            view(v) {
                if (v === 10000)
                    return "?";
                return v.toString()
            }
        }
    ];
}

function tailorsTask(level) {

    let _totalLenResult = {
        name: '_totalLenResult',
        title: 'Общая длина:',
        ordering: 'maximize'
    }

    let _totalReloads = {
        name: '_totalReloads',
        title: 'Количество перевдеваний:',
        ordering: 'minimize'
    }

    let _tailorsCount = {
        name: '_tailorsCount',
        title: 'Количество портных:',
        ordering: 'minimize'
    }

    let _waitCount = {
        name: '_waitCount',
        title: 'Количество ожиданий:',
        ordering: 'minimize'
    }

    if (level === 1) {
        return [_totalLenResult, _totalReloads, _tailorsCount];
    } else if (level === 2) {
        return [_totalLenResult, _totalReloads, _tailorsCount, _waitCount];
    } else {
        return [_totalLenResult, _tailorsCount];
    }
}

function datacentersTask(level) {
    /*
    1.Количество пропущенных вершин (надо как можно меньше)
    2.Количество вершин, рядом с которыми 2 датацентра (надо как можно больше, только второй уровень)
    3.суммарная длина расстояний от вершин до датацентров. Для каждой вершины, рядом с которой есть датацентры,
    выбирается ровно один ближайший, и расстояние скаладывается до него (надо как можно больше)
    */

    let _dcBadCount = {
        name: '_dcBadCount',
        title: 'Пропущено:',
        ordering: 'minimize'
    }

    let _dcPoints2 = {
        name: '_dcPoints2',
        title: 'Дублированные:',
        ordering: 'maximize'
    }

    let _dcSelectedCount = {
        name: '_dcSelectedCount',
        title: 'Количество:',
        ordering: 'minimize'
    }

    let _len = {
        name: '_len',
        title: 'Расстояние:',
        ordering: 'minimize'
    }

    if (level === 2)
        return [_dcBadCount, _dcSelectedCount, _dcPoints2, _len];
    else
        return [_dcBadCount, _dcSelectedCount, _len];
}

class Submission {
    constructor(submission, problemId, params) {
        this.raw = submission;
        this.params = params;

        this._id = submission._id;
        this.u = submission.u;
        this.lt = submission.lt;
        this.st = submission.st;
        this.pid = problemId;

        if (submission.pid === null) {
            this.type = 'save';
            let v = JSON.parse(submission.a.v);
            this.sol = v.solution;
            this.res = v.result;
        } else {
            this.type = 'record';
            this.sol = JSON.parse(submission.a.sol);
            this.res = JSON.parse(submission.a.res);
        }
        if (!this.res)
            throw new Error('this.res is undefined');
    }

    toString() {
        let result = this.type[0];
        for (let param of this.params) {
            let name = param.name;
            let value = this.res[name];
            let view = param.view ? param.view : v => '' + v;
            let view_value = typeof(value) != "undefined" ? view(value) : "undefined";
            result += ` ${param.title}: ${view_value}`;
        }
        return result;
    }

    compare(other) {
        for (let param of this.params) {
            let name = param.name;
            let value1 = this.res[name];
            let value2 = other.res[name];
            let dif = value1 - value2;
            if (param.ordering === 'minimize')
                dif = -dif;

            if (dif > 0)
                return 1;
            else if (dif < 0)
                return -1;
        }

        let t1 = this.type === 'save' ? 0 : 1;
        let t2 = other.type === 'save' ? 0 : 1;
        return t1 - t2;
    }

    clone() {
        return {
            u: this.u,
            lt: this.lt,
            st: this.st,
            cloned: true,
            pid: this.pid,
            a: {
                sol: JSON.stringify(this.sol),
                res: JSON.stringify(this.res)
            }
        };
    }
}

let collection_prefix = 'contest-kio20_';
let tasks = {
    'heat0': heatTask(0),
    'heat1': heatTask(1),
    'heat2': heatTask(2),

    'datacenters0': datacentersTask(0),
    'datacenters1': datacentersTask(1),
    'datacenters2': datacentersTask(2),

    'tailors0': tailorsTask(0),
    'tailors1': tailorsTask(1),
    'tailors2': tailorsTask(2)
};

let tasksIds = {
    'heat0': ObjectId("5e67062a98ec00d7bb01c12e"),
    'heat1': ObjectId("5e67063198ec00d7bb01c130"),
    'heat2': ObjectId("5e67063598ec00d7bb01c132"),

    'datacenters0': ObjectId("5e5930c198ec002d5209eff0"),
    'datacenters1': ObjectId("5e5942cf98ec00d7bbfe2718"),
    'datacenters2': ObjectId("5e5942de98ec00d7bbfe2721"),

    'tailors0': ObjectId("5e54423498ec004805916161"),
    'tailors1': ObjectId("5e544e5198ec005bd4ae408e"),
    'tailors2': ObjectId("5e544e9598ec005bd4ae4091")
}

for (let taskName in tasks) {
    let params = tasks[taskName];
    let id = tasksIds[taskName];
    processTask(taskName, params, id, db[collection_prefix + taskName]);
}

function processTask(taskName, params, problemId, collection) {
    print('processing task', taskName);

    //remove all external check (field 'c')
    collection.updateMany({}, {$unset: {c: ""}});

    let submissions = {};

    collection.find().sort({lt: 1}).forEach(submission => {
        let uid = submission.u.str;
        if (!(uid in submissions))
            submissions[uid] = [];

        try {
            submissions[uid].push(new Submission(submission, problemId, params));
        } catch (e) {
            if (submission.a.v === '') {
                //do nothing
            } else {
                print("Failed to process submission");
                printjson(submission);
            }
        }
    });

    for (let uid in submissions)
        processUser(uid, submissions[uid], collection);
}

function processUser(uid, submissions, collection) {
    print("user", uid);

    let i_prev = -1;
    let not_saved_submissions_count = 0;
    for (let i = 0; i < submissions.length; i++) {
        let s = submissions[i];
        let goes_down = false;
        if (i_prev >= 0 && s.type !== 'save') {
            let s1 = submissions[i_prev];
            if (s.compare(s1) < 0)
                goes_down = true;
        }
        if (s.type !== 'save') {
            i_prev = i;
            not_saved_submissions_count++;
        }
        if (goes_down)
            print(s.toString(), "GOES DOWN");
        else
            print(s.toString());
    }

    let best_is_from_save = false;
    let best_from_save = null;

    let n = submissions.length;
    if (n > 0) {
        let sorted_submissions = submissions.slice();
        sorted_submissions.sort((s1, s2) => s1.compare(s2));
        if (sorted_submissions[n - 1] !== submissions[n - 1])
            print("LAST IS NOT BEST");
        if (sorted_submissions[n - 1].type === "save") {
            best_is_from_save = true;
            best_from_save = sorted_submissions[n - 1];
            print("BEST IS FROM SAVE");
        }
    }
    if (n > 0 && not_saved_submissions_count === 0)
         print("ALL SUBMISSIONS ARE JUST SAVES");

    if (best_is_from_save) {
        print('trying to insert');
        printjson(best_from_save.clone());
        collection.insertOne(best_from_save.clone());
        // printjson(best_from_save.raw);
        // printjson(best_from_save.sol);
        // printjson(best_from_save.res);
    }
}

/*
> u = db['contest-kio20_heat1'].findOne()
{
	"_id" : ObjectId("5e671a2998ec00a245c7df54"),
	"u" : ObjectId("5e1c132198ec003649b8a1e1"),
	"lt" : NumberLong(93500),
	"st" : ISODate("2020-03-10T04:40:09.210Z"),
	"pid" : ObjectId("5e67063198ec00d7bb01c130"),
	"a" : {
		"sol" : "\"------------------------------------\"",
		"res" : "{\"e\":1,\"p\":0,\"t\":10000}"
	}
}
> u = db['contest-kio20_heat1'].findOne({pid: null})
{
	"_id" : ObjectId("5e72f89398ec0050a2eca05f"),
	"u" : ObjectId("5e3987a198ec00038e5c6858"),
	"lt" : NumberLong(80140),
	"st" : ISODate("2020-03-19T04:44:03.972Z"),
	"pid" : null,
	"a" : {
		"f" : "pdata0-save-XkmHkbGlk6",
		"v" : "{\"name\":\"2\",\"solution\":\"----6f-1-i9h24lm8g05kjac-3-n7d----be\",\"result\":{\"e\":1,\"p\":100,\"t\":1234}}"
	}
}
>

 */
