var koza21 = db.events.findOne({_id: "koza21"});
var contests = koza21.contests;

for (let contest of contests) {
    if (/-test/.test(contest.id))
        continue;

    contest.id += '-test';
    contest.start = new Date(2020, 11 - 1, 25, 10, 0, 0, 0);
    let new_rights = []
    for (let right of contest.rights) {
        let l = right.length;
        new_rights.push(right.substr(0, l - 1) + '1' + right[l - 1]);
    }
    contest.rights = new_rights;
}

db.events.update(
    {_id: "koza21"},
    {
        $push: {
            contests: {$each: contests}
        }
    }
);
