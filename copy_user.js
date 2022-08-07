adminUser = db.users.findOne({_id: ObjectId("6097dfdfa33b271d598c3559")})
delete adminUser._id
adminUser.event_id = "bebras21"

printjson(adminUser)

db.users.insert(adminUser)
