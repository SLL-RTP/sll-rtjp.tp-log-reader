# tp-log-reader

Används för att läsa av tjänsteplattformens loggar (ActiveMQ) och konsolidera dessa till en logfil i JSON format.

Logfilen roteras varje natt. Se konfiguration nedan för hur man ställer in var filerna ska loggas till och basnamnet för dessa.

Programmet startas enligt:

```sh
$ java [-Dlog-reader-config=<config-file> ] -jar log-reader<-version>.jar
```

## Default konfiguration
```sh
# Default properties for log-reader
# Also see logback.xml for actual logging configuration and output formats

# List of AMQ instances to read events from
log.amqInstances=failover:(tcp://localhost:61616)

# Directory for log files
log.directory = /tmp

# Basename of log file
log.basename = tp-log-reader

# List of queues to read events from (full AMQ names)
soitoolkit.logQueueNames=SOITOOLKIT.LOG.STORE,SOITOOLKIT.LOG.ERROR,SOITOOLKIT.LOG.PING,SOITOOLKIT.LOG.INFO

# Logging level for application output to the console (not event logging)
console.logLevel = INFO
```

## License

Apache License, Version 2.0
