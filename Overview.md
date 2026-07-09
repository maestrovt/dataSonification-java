# Overview of dataSonification Software

## Abstract

This repository contains the Java audio engine for the dataSonification project. The companion Excel add-in lives in the *[dataSonification-excel-addin respository](https://github.com/maestrovt/dataSonification-excel-addin)*.

The code in this repository was originally built an deployed in 2002 as a pilot project for financial traders. The project failed as a commercial venture. In 2026, the codebase was update to build in Java 17.

It is hoped that releasing this code on an open source platform will be useful to the sonification research community.

The platform as currently implemented is designed to sonify any kind of real time data that can be imported into an Excel spreadsheet.

The existing sonification strategies were designed with financial data in mind, no doubt they could be adapted to other uses.

What follows is some background and explanation of the software.

## History

Data sonification is a multi-disciplinary field which was first widely recognized at the [International Conference on Auditory Display in 1992](https://www.icad.org/websiteV2.0/Conferences/ICAD92/about92.html). The basic idea is to represent data in computer-generated sound as a supplement to visual representations. An intriguing possibility is that some kinds of data may lend themselves more readily to sonic rather than visual representation. Additionally, workers who depend heavily on screens could use data sonification to augment their attention bandwidth.

My first project was to sonify a computational fluid dynamics solver. It was presented at [ICAD 2001](http://legacy.spa.aalto.fi/icad2001/proceedings/papers/childs.pdf) and in a modified version at [ICAD 2008](https://blog.edwardchilds.com/wp-content/uploads/blog.edwardchilds.com/2011/01/D_02.pdf). The [project](http://www.datasonification.com/wp-content/uploads/2014/02/thesis.pdf) was undertaken while pursuing a Master of Arts in Electro-Acoustic Music at Dartmouth College.

While at Dartmouth I worked with the Dartmouth Entrepreneurial Network to develop a potentially commercializable application: sonifying financial data. Some of this work was presented at [ICAD 2004](https://www.icad.org/websiteV2.0/Conferences/ICAD2004/papers/janata_childs.pdf).

The work was eventually generalized to allow the sonification of any data that could be imported into an Excel spreadsheet.

## Sonification Schemes

1. The simplest scheme is to detect whether or not a given security or index has gone up or down. If the item moves up, two notes are sounded, the second higher than the first. If the item moves down, the second note is lower than the first. A "significant move" threshold can be specified such that sonification only occurs if the movement exceeds the threshold.
2. If it is desired to additionally provide a reference (for example, the value of the item at the opening), then three notes are sounded. The pitch of the first note is always the same, and represents the reference. The second note represents the value of the item the last time a sonification occurred. Its pitch is determined by whether the item is higher or lower than the opening price (with spacing determined by the significant move). The third note represents the current value of the item. For the most part, the significant move corresponds to a half step (chromatic) pitch difference. For example, suppose an index whose opening value is 100, and has been configured with a significant move of 10, first moves from 100 to 110. Then if the reference pitch is C, the first sonification to be heard will be the three notes C C C#. If the index moves to 120 the next three notes will be C C# D.
3. Adding to scheme 2, an optional target value can be added. If for example, a trader wishes to sell a stock if it reaches a target price. As with schemes 1 and 2, a "significant move" is configurable, in addition to a "target distance", which determines how close to the target the current value must be in order for a target warning to be sounded.
4. If additionally the size of the transaction is to be monitored, a "threshold size" is specified. If the number of units in a given transaction does not exceed the "threshold size", then no sonification will be heard. If a given transaction does meet or exceed the threshold size, the movement sonification (two, three or four notes) will be augmented by a trill sound. The number of trills is determined by how much the number of units in the transaction exceeds the "threshold size". This is configurable via the "threshold change" parameter. For example, suppose the threshold size is 700 units. If there is a trade which meets or exceeds 700 units, at least 1 trill will be heard. For every additional 100 units (in this case "threshold change" = 100), an additional trill is added to the sonification.

## Java Repository Layout

The Java source is at `src/com/dataSonification/v2/` with the following sub-packages:

- `sound/` — Analyzers, Arrangers, Trainers, Instruments, and the Gatekeeper
- `data/` — DataSource, DataComponent, SAX-based XML message parsers, V2 socket data source
- `ui/` — SocketUI (TCP server-side handler), FacelessUI (headless daemon UI)
- `util/` — Logging, configuration keys, return codes, type converters

External `.jar` dependencies live in `externals/`:

- `jmsl.jar`, `jscore.jar` — Java Music Specification Language ([algomusic.com](https://www.algomusic.com/jmsl/))
- `jsyn-20171016.jar`, `jsyn-old-api-20161206.jar` — JSyn audio synthesis ([softsynth.com](https://softsynth.com/jsyn/))
- `mysql.jar`, `sqlitejdbc-v054.jar` — database drivers
- `soundbank-deluxe.gm` — MIDI soundbank
