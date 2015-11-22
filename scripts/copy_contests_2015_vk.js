var bebras15 = db.events.findOne({_id: "bebras15"});

var contests = bebras15.contests;

//add all new contests
contests.forEach(function(contest) {
    var oldId = contest.id;

    print("copying contest " + oldId);

    contest.id = "vk-" + oldId;
    contest.tables = [];
    contest.rights = [];

    db.events.update(
        {_id: "bebras15"},
        {$push: {
            contests: contest
        }}
    );
});