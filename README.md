# swmetrics [![Build Status](https://travis-ci.com/BT-OpenSource/bt-sw-metrics.svg?branch=master)](https://travis-ci.com/BT-OpenSource/bt-sw-metrics)

The main purpose of **swmetrics** tool is to generate a number of metrics
about (principally) source code. The metrics can be generated either
from checked out local files or from output from version control system
commands. There is support for both Git and Subversion.

The tool also provides a means to visualise those metrics using
[tree-maps](https://en.wikipedia.org/wiki/Treemapping).

The **swmetrics** tool is inspired by ideas from Adam Tornhill's
excellent book ["Your Code as a Crime Scene"](https://pragprog.com/book/atcrime/your-code-as-a-crime-scene)
and, particularly, some of the ideas in the paper
["Reading Beside the Lines: Indentation as a Proxy for Complexity Metrics"](http://ieeexplore.ieee.org/document/4556125/)
that is referred to in that book.

## Building

**swmetrics** is implemented in [Groovy](http://groovy-lang.org) and
built using [Gradle](http://gradle.org). A Gradle "wrapper" is used so
you should be able to build the tool just by checking out the code
and invoking the supplied wrapper script as follows:

    gradlew build

By default the build produces a standalone "fat jar" file in `build/libs`
containing all of the necessary dependencies, including the Groovy run-time.

### Installation

As part of the build process the application is packaged in RPM and
DEB format for Linux and as a Zip file for Windows.

## Documentation

Full documentation on how to use the tool is contained in the
AsciiDoc format [user manual](src/docs/asciidoc/MANUAL.adoc).

Running `gradlew asciidoc` will generate an HTML version of the manual
in `build/asciidoc/html5`.

## Licence

This program is licensed under the terms of the open source "MIT license", as
contained in the file [LICENCE.md](LICENCE.md).

Note that the D3, D3lplus and JIT JavaScript libraries included with this
program are licensed under their own terms. See the relevant files in the
`src/main/resources` directory.
