#!/usr/bin/env ruby

trap("SIGINT") { exit(0) }

require "sqlite3"
require "time"

class SpendingMyTime

  def initialize
    @db = SQLite3::Database.new "smt.db"

    create_table!
  end

  def record_time_spent
    time_spent = ask("How many minutes would you like to log? ", suggestion).to_i
    activity = ask("For what? ")
    log(time_spent, activity)
  end

  def ask(prompt, suggestion=nil)
    print(suggestion ? prompt + "(#{suggestion})" : prompt)
    input = gets.chomp

    return suggestion if input == "" && suggestion

    input
  end

  def suggestion
    return last_activity_minutes_ago.round if last_activity_was_today?

    nil
  end

  def last_created_at
    @last_created_at ||= @db.execute( "select MAX(created_at) from time_spent" ) do |row|
      return Time.iso8601(row[0])
    end
  end

  def last_activity_minutes_ago
    (Time.now.utc - last_created_at) / 60
  end

  def log(time_spent, activity)
    @db.execute "insert into time_spent values ( ?, ?, ? )", [Time.now.utc.iso8601, activity, time_spent]
  end

  def create_table!
    # Create a table
    rows = @db.execute <<-SQL
      create table if not exists time_spent (
        created_at varchar(30),
        category varchar(255),
        minutes_spent int
      );
    SQL
  end

  def last_activity_was_today?
    last_created_at.day == Time.now.utc.day
  end

end

SpendingMyTime.new.record_time_spent
