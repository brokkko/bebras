var bebras15 = db.events.findOne({_id: "babior2015"});

var contests = bebras15.contests;

var rights = [];
for (var i = 1; i <= 240; i++)
    rights.push("participant~login=" + "81653523." + i);
for (i = 1; i <= 50; i++)
    rights.push("participant~login=" + "63840241." + i);


//add all new contests
contests.forEach(function(contest) {
    var oldId = contest.id;
    if (['1-2', '3-4', '5-6', '7-8', '9-10', '11'].indexOf(contest.id) < 0)
        return;

    print("copying contest " + oldId);

    contest.id = "vk-" + oldId;
    contest.tables = [];
    contest.rights = [];
    contest.start = new Date(2014, 10 - 1, 7 - 1, 0, 0, 0, 0);
    contest.finish = new Date(2100, 1, 1, 0, 0, 0, 0);
    contest.results = contest.start;
    contest.rights = rights;

    db.events.update(
        {_id: "babior2015"},
        {$push: {
            contests: contest
        }}
    );
});