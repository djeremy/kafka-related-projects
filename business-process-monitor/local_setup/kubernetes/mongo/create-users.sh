#!/usr/bin/env bash

mongo -u admin -p admin --eval "db.getSiblingDB('bpm_db').createUser({user:
              'admin', pwd: 'admin', roles: [{role: 'dbAdmin', db: 'bpm_db'}, {role:
              'readWrite', db: 'bpm_db'}] })"
