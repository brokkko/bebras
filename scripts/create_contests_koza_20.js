var koza20 = db.events.findOne({_id: "koza20"});
var contests = koza20.contests;

for (let contest of contests) {
    let id = contest.id;
    contest.tables = [];

    contest.rights = [];
    if (/task2-../.test(contest.id))
        contest.rights.push("participant~league=2");
    if (/task5-../.test(contest.id))
        contest.rights.push("participant~league=1");
    if (/.*-ru/.test(contest.id))
        contest.rights.push("participant~language=ru");
    else
        contest.rights.push("participant~language=en");

    contest['page sizes'] = [1];
    contest.start = new Date(2020, 11 - 1, 25, 10, 0, 0, 0);
    contest.finish = new Date(2020, 11 - 1, 25, 12, 0, 0, 0);
    contest.results = new Date(2020, 11 - 1, 26, 23, 0, 0, 0)
    contest.duration = 0;
    contest["allow restart"] = false;
}

db.events.update(
    {_id: "koza20"},
    {
        $set: {
            contests: contests
        }
    }
);
