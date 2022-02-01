db.html_blocks.find({event_id:"kio21"}).forEach(function(d) {
    db.html_blocks.insert({"event_id": "kio22", "name": d.name, "html": d.html});
});
