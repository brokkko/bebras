#!/usr/bin/env bash

event=$1
host=$2

mongoexport --host $host -d dces2 -c users -q '{"want_ad": true, "_role": "SCHOOL_ORG", "event_id": "'$event'"}' --type=csv -f name,patronymic,surname,email -o ${event}_emails.csv