var eid = "bebras16";
var school_plus = db.users.findOne({event_id: eid, login: "school_plus"});

var res_sum = [[0, 0], [0, 0]];

// var event = db.events.findOne({_id: eid});
// var roles = event.roles;
// var school_org_role = find_in_array(roles, 'name', 'SCHOOL_ORG');

for (var grade = 1; grade <= 11; grade++) {
    print("grade = " + grade);
    var res = [[0, 0], [0, 0]];
    for (var with_history = 0; with_history <= 1; with_history++)
        for (var by_school_plus = 0; by_school_plus <= 1; by_school_plus++) {
            res[with_history][by_school_plus] = print_grade(grade, with_history === 1, by_school_plus === 1);
            res_sum[with_history][by_school_plus] += res[with_history][by_school_plus];
        }

    print_beau(res);
}

print("all grades");
print_beau(res_sum);

function left_pad(x, num) {
    x = '' + x;
    while (x.length < num)
        x = ' ' + x;
    return x;
}

function grade_2_contest(grade) {
    if (grade === 11)
        return "11";
    var t = grade + grade % 2;
    return (t - 1) + '-' + t;
}

function print_grade(grade, started_contest, by_school_plus) {
    var query = {event_id: eid, _role: "PARTICIPANT", grade: '' + grade};
    if (by_school_plus)
        query['_reg_by'] = school_plus._id;
    if (started_contest)
        query["_contests." + grade_2_contest(grade) + '.sd'] = {'$ne': null};

    return db.users.count(query);
}

function print_beau(res) {
    print(left_pad(res[0][0], 6), left_pad(res[0][1], 6));
    print(left_pad(res[1][0], 6), left_pad(res[1][1], 6));
}

function find_in_array(array, field, value) {
    for (var i = 0; i < array.length; i++)
        if (array[i].field === value)
            return array[i];
    return null;
}