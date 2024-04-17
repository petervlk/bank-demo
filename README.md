# bank-demo/bank-demo

FIXME: my new application.

## Installation

Download from https://github.com/petervlk/bank-demo

## Usage

### Run App

    $ clojure -M:run-m

### Run Tests

    $ clojure -M:test

# Notes

## Design decisions

Initially considered use of message broker but went with cache instead. In production something like Redis would be used. 
Again due to lack of time and to avoid potential issues with configuration decided to store cached data in atom. 
This solution is far from optimal, but communicates the idea well enough.

Data is stored in a relational DB. Current configuration uses H2 but swapping it for postgres is just a matter of config change.
This should also be true for table initialization scripts.

## Tech debt 

Due to lack of time test coverage is not great. Tried to cover at least some non trivial functionality.

Request handlers could use some refactoring to reduce code duplication.

Cache namespace could be broken down further into multiple namespaces for commands, queries and potentially validators.
