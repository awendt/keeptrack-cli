#!/usr/bin/env ruby

require "sqlite3"

# Open a database
db = SQLite3::Database.new "smt.db"

# Create a table
rows = db.execute <<-SQL
  create table if not exists time_spent (
    created_at varchar(30),
    category varchar(255),
    minutes_spent int
  );
SQL

db.execute "insert into time_spent values ( ?, ?, ? )", [ Time.now.utc.to_s, 'meeting', 40 ]
