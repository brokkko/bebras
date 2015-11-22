var bebras15 = db.events.findOne({_id: "bebras15"});

var contests = bebras15.contests;

//remove all contests
db.events.update({_id: "babior20"}, {$set: {contests: []}});

//add all new contests
contests.forEach(function(contest) {
    var oldId = contest.id;

    print("copying contest " + oldId);

    contest.id = oldId;
    contest.tables = [];
    contest.rights = ["anon"];
    contest.start = new Date(2014, 10 - 1, 7 - 1, 0, 0, 0, 0);
    contest.finish = new Date(2100, 1, 1, 0, 0, 0, 0);
    contest.results = contest.start;
    contest["allow restart"] = true;

    db.events.update(
        {_id: "babior2015"},
        {$push: {
            contests: contest
        }}
    );
});