# bandmon

Simple script for running and recording the results of various network
connection tests. 

## Usage

    $ java -jar bandmon-1.0.0-SNAPSHOT-standalone.jar    

## Installation

Create a database to store results (or use bandmon-postgres.sql).

Review project.clj and settings in core.clj.  Modify them as needed for your setup then run:

    $ lein uberjar

## License

Copyright (C) 2010 David Jack

Distributed under the Eclipse Public License, the same as Clojure.
