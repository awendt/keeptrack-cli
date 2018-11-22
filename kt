#!/usr/bin/env ruby

trap("SIGINT") { exit(0) }

require "sqlite3"
require "time"

class KeepTrack

  class Today

    def initialize(db)
      @db = db
    end

    def summarize
      @db.execute(<<-SQL) do |row|
        SELECT time(created_at, 'utc', 'localtime'), category, minutes_spent FROM time_spent
        WHERE created_at >= date('now', 'start of day')
      SQL
        print_line(timestamp: row[0], category: row[1], minutes: row[2])
      end
      print_sum
    end

    private

    def print_line(timestamp:, category:, minutes:)
      puts [
        timestamp,
        category.ljust(max_category_length),
        minutes.to_s.rjust(2)
      ].join(' | ')
    end

    def print_sum
      line_length = (max_category_length + 16)
      puts '-' * line_length
      puts time_spent_today.rjust(line_length)
    end

    def time_spent_today
      @db.get_first_value(<<-SQL)
        SELECT strftime('%H:%M', SUM(minutes_spent)*60, 'unixepoch') FROM time_spent
        WHERE created_at >= date('now', 'start of day')
      SQL
    end

    def max_category_length
      @max_category_length ||= @db.get_first_value(<<-SQL).to_i
        SELECT MAX(LENGTH(category)) FROM time_spent
        WHERE created_at >= date('now', 'start of day')
      SQL
    end

  end

  def initialize
    @db = SQLite3::Database.new "smt.db"

    create_table!
  end

  def record_time_spent
    time_spent = ask("How many minutes would you like to log? ", suggestion).to_i
    activity = ask("For what? ")
    log(time_spent, activity)
    summarize_today
  end

  def summarize_today
    Today.new(@db).summarize
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
      return row[0] ? Time.iso8601(row[0]) : nil
    end
  end

  def last_activity_minutes_ago
    (Time.now.utc - last_created_at) / 60
  end

  def log(time_spent, activity)
    @db.execute "insert into time_spent(created_at, category, minutes_spent) values ( ?, ?, ? )",
      [Time.now.utc.iso8601, activity, time_spent]
  end

  def create_table!
    # Create a table
    rows = @db.execute <<-SQL
      create table if not exists time_spent (
        id INTEGER PRIMARY KEY,
        created_at varchar(30),
        category varchar(255),
        minutes_spent int
      );
    SQL
  end

  def last_activity_was_today?
    last_created_at && last_created_at.day == Time.now.utc.day
  end

end

KeepTrack.new.record_time_spent
