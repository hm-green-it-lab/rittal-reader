# Rittal SNMP Reader
A simple Java/Quarkus tool to periodically read power consumption values from a Rittal device via SNMP.

> [!note]
> **SNMP Reliability**: SNMP uses UDP as a transport protocol, which is connectionless and does not guarantee message delivery. This application uses a simple retry mechanism (configured in code) to handle potential packet loss. For most use cases (like polling power values every few seconds), this is sufficient and an industry‑standard approach. Alternatively, if the device supports it, Modbus TCP (connection‑oriented) could be used for higher reliability, at the cost of additional complexity and connection overhead.

## Configuration

Adjust settings in `application.properties`:

```txt
# Quarkus CRON schedule (Quartz format, includes seconds)
# Example: run every 10 seconds
rittal.cron=*/10 * * * * ?

# SNMP Settings
rittal.snmp.community=public
rittal.snmp.address=[IP]/[PORT]

# Comma-separated list of OIDs
rittal.snmp.oids=[OID1],[OID2]
```

### Providing a List of OIDs

There are multiple ways to provide a list of OIDs to the application at startup:

#### 1. Configuration File (application.properties)

The simplest method is to edit the `src/main/resources/application.properties` file before building:

```properties
rittal.snmp.oids=1.3.6.1.4.1.2606.7.4.2.2.1.10.2.1,1.3.6.1.4.1.2606.7.4.2.2.1.10.2.2
```

Multiple OIDs are separated by commas. Whitespace around OIDs is automatically trimmed.

#### 2. Environment Variables

Override the configuration at runtime using environment variables:

**Windows PowerShell:**
```powershell
$env:RITTAL_SNMP_OIDS="1.3.6.1.4.1.2606.7.4.2.2.1.10.2.1,1.3.6.1.4.1.2606.7.4.2.2.1.10.2.2"
java -jar target\rittal-reader-*-runner.jar
```

**Linux/macOS:**
```bash
export RITTAL_SNMP_OIDS="1.3.6.1.4.1.2606.7.4.2.2.1.10.2.1,1.3.6.1.4.1.2606.7.4.2.2.1.10.2.2"
java -jar target/rittal-reader-*-runner.jar
```

> **Note:** Environment variables use underscores instead of dots and are UPPERCASE.

#### 3. Command Line Arguments

Pass configuration directly on the command line:

```bash
java -Drittal.snmp.oids=1.3.6.1.4.1.2606.7.4.2.2.1.10.2.1,1.3.6.1.4.1.2606.7.4.2.2.1.10.2.2 -jar target/rittal-reader-*-runner.jar
```

#### 4. External Configuration File

Create an `application.properties` file in the same directory as the JAR:

```properties
rittal.snmp.oids=1.3.6.1.4.1.2606.7.4.2.2.1.10.2.1,1.3.6.1.4.1.2606.7.4.2.2.1.10.2.2
```

Then run:
```bash
java -jar rittal-reader-*-runner.jar
```

The external configuration file takes precedence over the bundled one.

#### Configuration Priority

Quarkus applies configuration in the following order (highest priority first):
1. System properties (`-D` command line arguments)
2. Environment variables
3. External `application.properties` (in current directory)
4. Bundled `application.properties` (in JAR)

**Example with multiple OIDs:**
```properties
# Power consumption for multiple phases
rittal.snmp.oids=1.3.6.1.4.1.2606.7.4.2.2.1.10.2.1,1.3.6.1.4.1.2606.7.4.2.2.1.10.2.2,1.3.6.1.4.1.2606.7.4.2.2.1.10.2.3
```

## Build

```bash
mvn clean package
```

## Run

```bash
java -jar rittal-reader-[version]-runner.jar
```

Example Output:

```txt
Timestamp,OID,Power (Watts)
1744444081716,[OID1],105
1744444081716,[OID2],81
```






