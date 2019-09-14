db.html_blocks.find({event_id:"bebras18"}).forEach(function(d) {
    db.html_blocks.insert({"event_id": "bebras19", "name": d.name, "html": d.html});
});