var bebras20 = db.events.findOne({_id: "bebras20"});
var contests = bebras20.contests;

var contestsToCopy = ["1-2", "3-4", "5-6", "7-8", "9-10", "11"];

//add all new contests
bebras20.contests.forEach(function (contest) {
    var id = contest.id;
    if (contestsToCopy.indexOf(id) < 0)
        return;

    print("copying contest " + id);
    var grades = id.split("-");

    contest.tables = [];
    contest.name = contest.name.substr(0, contest.name.length - 2) + '21';

    contest.rights = [];
    for (let i = 0; i < grades.length; i++)
        contest.rights.push("participant~grade=" + grades[i]);

    contest.start = new Date(2021, 11 - 1, 9, 0, 0, 0, 0);
    contest.finish = new Date(2021, 11 - 1, 22, 23, 59, 59, 0);
    contest.results = new Date(2021, 11 - 1, 30, 23, 0, 0, 0)
    contest["allow restart"] = false;
    contest["only admin"] = true;

    db.events.update(
        {_id: "bebras21"},
        {
            $push: {
                contests: contest
            }
        }
    );
});
