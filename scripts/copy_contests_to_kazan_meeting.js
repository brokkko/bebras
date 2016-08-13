var bebras15 = db.events.findOne({_id: "bebras15"});

var contests = bebras15.contests;

bebras15.contests.forEach(function(contest) {
    var oldId = contest.id;
    var newId = oldId;

    print("copying contest " + oldId + " to " + newId);

    contest.id = newId;
    contest.tables = [];
    // contest.rights = ["anon"];
//    contest.start = new Date(/*year, month, day, hours, minutes, seconds, milliseconds*/);
    contest.start = new Date(2014, 10 - 1, 7 - 1, 0, 0, 0, 0);
    contest.finish = new Date(2100, 1, 1, 0, 0, 0, 0);
    contest.results = contest.start;
    contest["allow restart"] = true;

    db.events.update(
        {_id: "bebras16kazan"},
        {$push: {
            contests: contest
        }}
    );
});