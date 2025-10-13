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






