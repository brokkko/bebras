var bebras17 = db.events.findOne({_id: "bebras17"});

var contests = bebras17.contests;

//add all new contests
contests.forEach(function(contest) {
    var oldId = contest.id;

    if (
        oldId !== '1-2' &&
        oldId !== '3-4' &&
        oldId !== '5-6' &&
        oldId !== '7-8' &&
        oldId !== '9-10' &&
        oldId !== '11'
    )
        return;

    print("copying contest " + oldId);

    contest.id = "vk-" + oldId;
    contest.tables = [];

    for (var i = 0; i < contest.rights.length; i++)
        contest.rights[i] = contest.rights[i].replace(/participant~/g, 'participant vk~');

    contest.start = new Date(2017, 11 - 1, 27, 0, 0, 0, 0);
    contest.finish = new Date(2017, 12 - 1, 6, 23, 59, 0, 0);
    contest.results = contest.finish;

    printjson(contest);

    db.events.update(
        {_id: "bebras17"},
        {$push: {
            contests: contest
        }}
    );
});