# Third-Party Components & Licensing

The source code in this repository is released under the [MIT License](LICENSE).
However, the dataSonification Java engine depends at build/run time on several
third-party libraries and resource files that are **not** redistributed here
because of their licenses or uncertain provenance. You must obtain them
yourself and place them in the `externals/` directory before building.

This document lists each one and where to get it.

## Required to build and run

| File (place in `externals/`) | Component | Where to obtain | License notes |
| --- | --- | --- | --- |
| `jmsl.jar` | Java Music Specification Language | <https://www.algomusic.com/jmsl/> | Commercial product by Nick Didkovsky. A JMSL license is required; recent JMSL releases also require a runtime license file. |
| `jscore.jar` | JScore (common music notation) | <https://www.algomusic.com/jmsl/> | Ships with JMSL; same licensing as JMSL. |
| `jsyn-20171016.jar` | JSyn audio synthesis | <https://www.softsynth.com/jsyn/> | By Phil Burk / Mobileer. Freely redistributable, but excluded here to keep the acquisition story in one place. |
| `jsyn-old-api-20161206.jar` | JSyn legacy API shim | <https://www.softsynth.com/jsyn/> | Same as above. Required for the older JSyn API calls used by JMSL. |
| `sqlite-jdbc-3.53.1.0.jar` | Xerial SQLite JDBC driver | <https://github.com/xerial/sqlite-jdbc> | Apache-2.0. Freely redistributable; excluded here only for repo leanness. |

Once these are in `externals/`, build and run with Ant (Eclipse Temurin 17 LTS):

```bash
cd build
ant clean && ant jar && ant run    # daemon on PORT 2011 for the Excel add-in
```

## Optional (MIDI-instrument path only)

| File | Component | Notes |
| --- | --- | --- |
| `soundbank-deluxe.gm` | General MIDI "deluxe" soundbank | Used only when sonifying through the built-in MIDI instruments (not the sample-based instruments). Provenance is uncertain (originates from the Beatnik Audio Engine / legacy Java Sound "deluxe" soundbank), so it is **not** redistributed. If you need the MIDI path, supply your own General MIDI `.gm`/`.sf2`-equivalent soundbank and place it in `externals/`. |

## Audio sample sets

The sample-based instruments load audio from a separate sample directory, not
from the tracked source. The cleaned sample set is distributed as a GitHub
Release asset on this repository:

- **Releases → `dataSonification-runtime-samples.zip`**

Sample provenance and acknowledgments are documented in that release (see the
`READHERE.txt` inside the archive). In particular, the **ACB (Acoustic
Branding)** sample set is redistributed with grateful acknowledgment to its
creators.

## Removed components

The legacy MySQL JDBC driver (`mysql.jar`) was removed in the public release;
the live database driver is `org.sqlite.JDBC` (xerial sqlite-jdbc), loaded via
reflection. Configuration and sonification schemes are read from a SQLite
database shipped with the Excel add-in.
