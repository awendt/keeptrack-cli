# keeptrack-cli

`kt` is a script that helps you track what you're working on and how many times you're context-switching.

It's a time tracker quite different from [TimeTrap](https://github.com/samg/timetrap).

There's **no support for projects**, **no configuration**, and it **requires sqlite3**.

## Usage

When invoking `kt`, it asks you:

1. How many minutes you'd like to track
2. Which activity did you spent the time on

Whenever you make a new entry, it prints a summary for today. (It keeps the entries indefinitely.)

If you already tracked something on the same day, it will auto-suggest the time spent
based on when you made the last entry.

This makes it faster to work with.

## TODO

- statistics about how many things you've been switching between over the week
- break-down what you spend your time on
