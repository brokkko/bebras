var bebras15 = db.events.findOne({_id: "bebras15"});

var contests = bebras15.contests;

//add all new contests
contests.forEach(function(contest) {
    var oldId = contest.id;

    print("copying contest " + oldId);

    contest.id = "vk-" + oldId;
    contest.tables = [];
    contest.rights = [];
    contest.start = new Date(2014, 10 - 1, 7 - 1, 0, 0, 0, 0);
    contest.finish = new Date(2100, 1, 1, 0, 0, 0, 0);
    contest.results = contest.start;

    db.events.update(
        {_id: "bebras15"},
        {$push: {
            contests: contest
        }}
    );
});