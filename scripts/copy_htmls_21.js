db.html_blocks.find({event_id:"bebras20"}).forEach(function(d) {
    db.html_blocks.insert({"event_id": "bebras21", "name": d.name, "html": d.html});
});
