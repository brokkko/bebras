var bebras13 = db.events.findOne({_id: "bebras13"});

var contests = bebras13.contests;

var contestsToCopy = [
    "bobrik-1-2",
    "bebras-3-4",
    "bebras-5-6",
    "bebras-7-8",
    "bebras-9-10",
    "bebras-11",
    "1-2",
    "3-4",
    "5-6",
    "7-8",
    "9-10",
    "11"
];

bebras13.contests.forEach(function(contest) {
    var oldId = contest.id;
    if (contestsToCopy.indexOf(oldId) < 0)
        return;

    // copy contest
    if (oldId.indexOf("b") == 0) {
        var newId = "bebras13-" + oldId.substr(7);
        var trial = false;
    } else {
        newId = "bebras13-trial-" + oldId;
        trial = true;
    }

    print("copying contest " + oldId + " to " + newId);

    contest.id = newId;
    contest.tables = [];
    contest.rights = ["anon"];
//    contest.start = new Date(/*year, month, day, hours, minutes, seconds, milliseconds*/);
    contest.start = new Date(2014, 10 - 1, 7 - 1, 0, 0, 0, 0);
    contest.finish = new Date(2100, 1, 1, 0, 0, 0, 0);
    contest.results = contest.start;
    contest["allow restart"] = true;

    db.events.update(
        {_id: "bebras14"},
        {$push: {
            contests: contest
        }}
    );
});