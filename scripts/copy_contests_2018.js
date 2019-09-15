var bebras17 = db.events.findOne({_id: "bebras17"});
var contests = bebras17.contests;

var contestsToCopyPostfixes = [
    "1-2",
    "3-4",
    "5-6",
    "7-8",
    "9-10",
    "11"
];

var contestsToCopyPrefixes = ["", "bebras13-", "bebras13-trial-", "bebras14-", "bebras15-", "bebras16-"];

var contestsToCopy = [];

for (var i = 0; i < contestsToCopyPrefixes.length; i++)
    for (var j = 0; j < contestsToCopyPostfixes.length; j++)
        contestsToCopy.push(contestsToCopyPrefixes[i] + contestsToCopyPostfixes[j]);

//remove all contests
db.events.update({_id: "bebras18"}, {$set: {contests: []}});

//add all new contests
bebras17.contests.forEach(function(contest) {
    var oldId = contest.id;
    if (contestsToCopy.indexOf(oldId) < 0) {
        print("skipping contest " + oldId);
        return;
    }

    // copy contest
    if (oldId.indexOf("bebras") === 0)
        var newId = oldId;
    else
        newId = "bebras17-" + oldId;

    print("copying contest " + oldId + " to " + newId);

    contest.id = newId;
    contest.tables = [];
    contest.rights = ["anon"];
//    contest.start = new Date(/*year, month, day, hours, minutes, seconds, milliseconds*/);
    contest.start = new Date(2016, 11 - 1, 13 - 1, 0, 0, 0, 0);
    contest.finish = new Date(2117, 11 - 1, 24 - 1, 23, 59, 59, 0);
    contest.results = contest.start; //new Date(2017, 12 - 1, 4 - 1, 0, 0, 0);
    contest["allow restart"] = true;

    db.events.update(
        {_id: "bebras18"},
        {$push: {
                contests: contest
            }}
    );
});
