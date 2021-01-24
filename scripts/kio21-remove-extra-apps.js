all_orgs = [];

db.users.find({event_id: "kio21", _role: "SCHOOL_ORG"}).forEach(org => all_orgs.push(org));

for (let org of all_orgs) {
    print("processing org " + org.login);
    let new_apps = [];
    for (let app of org.apps) {
        let app_is_extra = app.type === 'bk' && app.created.getFullYear() === 2021;
        if (!app_is_extra) {
            new_apps.push(app);
            continue;
        }

        let logins = app.logins;
        print("removing users from app " + app.name);
        for (let login of app.logins)
            remove_user(login);
    }
    update_apps(org.login, new_apps);
}

function remove_user(login) {
    print("removing user " + login);
    db.users.remove({event_id: "kio21", login: login});
}

function update_apps(login, new_apps) {
    print("setting new apps for user: " + login);
    printjson(new_apps);
    db.users.update({event_id: "kio21", login: login}, {"$set": {apps: new_apps}});
}
