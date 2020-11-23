var koza20 = db.events.findOne({_id: "koza20"});
var contests = koza20.contests;

for (let contest of contests) {
    let id = contest.id;
    contest.tables = [];

    let right = "participant";
    if (/task1-../.test(contest.id))
        right += "~league=2";
    if (/task5-../.test(contest.id))
        right += "~league=1";

    if (/.*-ru/.test(contest.id))
        right += "~language=ru";
    else
        right += "~language=en";

    contest.rights = [right];

    contest['page sizes'] = [1];
    contest.start = new Date(2020, 11 - 1, 25, 10, 0, 0, 0);
    contest.finish = new Date(2020, 11 - 1, 25, 12, 0, 0, 0);
    contest.results = new Date(2020, 11 - 1, 26, 23, 0, 0, 0)
    contest.duration = 0;
    contest["allow restart"] = false;
    contest["only admin"] = false;
    contest["results translators"] = [{"type" : "kiojs"}];
}

db.events.update(
    {_id: "koza20"},
    {
        $set: {
            contests: contests
        }
    }
);
