//remove 'answer undo' for user 07774521.99 in problem CH-2 (ObjectId("563da988a96f595479f521df")
db["contest-bebras15_3-4"].remove({_id : ObjectId("564424f7a96f594c5820d6cf")});

//make coorect answer for 38301008009 5638c814a96f59d8ed00f731 for problem CZ-01 563d808fa96f595479f4ef83
var correct = { "r" : 2, "s" : "{\"2\":7,\"4\":1,\"6\":3,\"8\":5}" };
db["contest-bebras15_5-6"].update({_id: ObjectId("5643e703a96f594c581e6df0")}, {$set: {a: correct}});