//drop collection with

db.events.find({_id: /^bebras\d{2}$/}).forEach(event => {
    for (let contest of event.contests) {
        //test contest.rights is ["anon"]
        let is_anon = contest.rights.length === 1 && contest.rights[0] === "anon";
        if (!is_anon)
            continue;
        if (event._id === "bebras20" || event._id === "bebras21")
            continue;

        print(event._id + " -> " + contest.id);
        let collection_name = `contest-${event._id}_${contest.id}`
        // print(db[collection_name].count());
        db[collection_name].drop();
    }
});
