var koza21 = db.events.findOne({_id: "koza21"});
var contests = koza21.contests;

for (let contest of contests) {
    let id = contest.id;
    contest.tables = [];

    let right = "participant";

    if (/.*-ru/.test(contest.id))
        right += "~language=ru";
    else
        right += "~language=en";

    if (/elasticity1-../.test(contest.id)) {
        right += "~league=1";
        contest.rights = [right];
    } else if (/elasticity2-../.test(contest.id)) {
        right += "~league=2";
        contest.rights = [right];
    } else {
        right += "~league=";
        contest.rights = [right + 1, right + 2];
    }

    contest['page sizes'] = [1];
    contest.start = new Date(2021, 11 - 1, 25, 10, 0, 0, 0);
    contest.finish = new Date(2021, 11 - 1, 25, 12, 0, 0, 0);
    contest.results = new Date(2021, 11 - 1, 26, 23, 0, 0, 0)
    contest.duration = 1;
    contest["allow restart"] = false;
    contest["only admin"] = false;
    contest["results translators"] = [{"type": "kiojs"}];

    print("processed contest " + contest.id + " as a usual contest");
}

db.events.update(
    {_id: "koza21"},
    {
        $set: {
            contests: contests
        }
    }
);
