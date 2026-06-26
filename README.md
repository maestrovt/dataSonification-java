# dataSonification — Java Sonification Engine

dataSonification converts data into computer-generated sound — an auditory
display that supplements (or replaces) visual representation. This repository
contains the **Java engine**: a standalone desktop application and a socket
daemon that streams sonifications in real time. It is most commonly driven by
the companion Excel add-in, which sends spreadsheet data to the daemon over a
local TCP connection.

See <http://www.datasonification.com/> for background on the project and the
field of data sonification.

## Related repositories

- **[dataSonification-excel-addin](https://github.com/maestrovt/dataSonification-excel-addin)**
  — the C# / ExcelDna add-in that feeds data to this engine and ships the
  runtime configuration database.
- The **audio sample set** that the engine plays is distributed as a GitHub
  Release asset on **this** repository
  (`dataSonification-runtime-samples.zip`, under *Releases*).

## Sonification schemes

The engine implements four progressively richer schemes:

1. **Movement** — two notes indicate whether a value rose or fell since the
   last sonification. A configurable "significant move" threshold suppresses
   noise.
2. **Movement + reference** — three notes: a fixed reference pitch (e.g. the
   opening value), the previous value, and the current value, with pitch
   spacing set by the significant move.
3. **Target** — adds an optional target value; the closer the value gets to the
   target, the longer a target warning sounds (governed by a "target distance").
4. **Transaction size** — adds a trill whose length reflects how far a
   transaction's size exceeds a configurable "threshold size".

## Requirements

- **Eclipse Temurin (OpenJDK) 17 LTS** — supported through October 2029.
- **Apache Ant** (1.10.x).
- The third-party libraries listed in [LICENSING.md](LICENSING.md), placed in
  `externals/`. These (JMSL, JScore, JSyn, the SQLite JDBC driver, and an
  optional MIDI soundbank) are **not** redistributed here and must be obtained
  separately.

## Build & run

All commands run from the `build/` directory:

```bash
cd build

ant jar     # builds jar/dataSonification.jar
ant run     # launches the daemon on PORT 2011 (for the Excel add-in)
ant clean   # removes all generated files
```

Run modes directly:

```bash
# Desktop mode
java -cp jar/dataSonification.jar com.dataSonification.v2.Main

# Daemon mode (socket server) — PORT 2011 by default
java -cp jar/dataSonification.jar com.dataSonification.v2.MainDaemon -PORT 2011
```

## Architecture

```text
DataSource → DataComponent → Sonification → Analyzer → Arranger → Trainer → Gatekeeper → Audio
```

- **Core** — singleton central engine managing all subsystems.
- **Analyzers** extract meaning from data; **Arrangers** turn it into musical
  output; **Trainers** reinforce learning with feedback.
- **Gatekeeper** — audio dispatcher/mixer (max 22 simultaneous sonifiables).
- **SocketUI / FacelessUI** — socket and headless front-ends for daemon mode.

Configuration lives in `settings/` (`.txt` files); configuration keys are
defined in `util/Key.java`. Sonification schemes and instrument configuration
are read from a SQLite database shipped with the Excel add-in.

## History

The sonification techniques here were developed beginning in 2002 by a startup
founded by Edward Childs, with the engine developed by Kimo Johnson, Benjamin
Childs, and John Stephens. The two associated patents were abandoned in January
2019. This release modernizes the code (Java 8 → 17) and opens it to the
community.

## License

Project source code: [MIT](LICENSE). Third-party dependencies and the audio
sample sets carry their own licenses — see [LICENSING.md](LICENSING.md). The
**ACB (Acoustic Branding)** sample set is included in the runtime release with
grateful acknowledgment to its creators.
