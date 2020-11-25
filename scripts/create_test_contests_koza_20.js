var koza20 = db.events.findOne({_id: "koza20"});
var contests = koza20.contests;

for (let contest of contests) {
    contest.id += '-test';
    let new_rights = []
    for (let right of contest.rights) {
        let l = right.length;
        new_rights.push(right.substr(0, l - 1) + '1' + right[l - 1]);
    }
    contest.rights = new_rights;
}

db.events.update(
    {_id: "koza20"},
    {
        $push: {
            contests: {$each: contests}
        }
    }
);
