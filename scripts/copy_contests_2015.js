var bebras14 = db.events.findOne({_id: "bebras14"});

var contests = bebras14.contests;

//HG2845H

var contestsToCopyPostfixes = [
    "1-2",
    "3-4",
    "5-6",
    "7-8",
    "9-10",
    "11"
];

var contestsToCopyPrefixes = ["", "bebras13-", "bebras13-trial-"];

var contestsToCopy = [];

for (var i = 0; i < contestsToCopyPrefixes.length; i++)
    for (var j = 0; j < contestsToCopyPostfixes.length; j++)
        contestsToCopy.push(contestsToCopyPrefixes[i] + contestsToCopyPostfixes[j]);

//remove all contests
db.events.update({_id: "bebras15"}, {$set: {contests: []}});

//add all new contests
bebras14.contests.forEach(function(contest) {
    var oldId = contest.id;
    if (contestsToCopy.indexOf(oldId) < 0) {
        print("skipping contest " + oldId);
        return;
    }

    // copy contest
    if (oldId.indexOf("bebras") == 0)
        var newId = oldId;
    else
        newId = "bebras14-" + oldId;

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
        {_id: "bebras15"},
        {$push: {
            contests: contest
        }}
    );
});