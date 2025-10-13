package edu.hm.greenit.tools.rittal;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
@QuarkusMain
public class RittalReader implements QuarkusApplication, Closeable {

    @ConfigProperty(name = "rittal.snmp.community")
    String community;

    @ConfigProperty(name = "rittal.snmp.address")
    String targetAddress;

    @ConfigProperty(name = "rittal.snmp.oids")
    List<String> oids;

    private Snmp snmp;
    private CommunityTarget<UdpAddress> target;

    public static void main(String[] args) {
        Quarkus.run(RittalReader.class, args);
    }

    @Override
    public int run(String... args) {
        // Print CSV header
        System.out.println("Timestamp,OID,Power (Watts)");
        Quarkus.waitForExit();
        return 0;
    }

    @PostConstruct
    void init() {
        try {
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            transport.listen();

            target = new CommunityTarget<>();
            target.setCommunity(new OctetString(community));
            target.setAddress(new UdpAddress(targetAddress));
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            snmp = new Snmp(transport);
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Failed to initialize SNMP transport", e);
        }
    }

    @RunOnVirtualThread
    @Scheduled(cron = "${rittal.cron}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void readSnmpData() {
        long timestamp = System.currentTimeMillis();
        try {
            // Build a single PDU with all OIDs
            PDU pdu = new PDU();
            for (String oid : oids) {
                pdu.add(new VariableBinding(new OID(oid)));
            }
            pdu.setType(PDU.GET);

            ResponseEvent event = snmp.get(pdu, target);

            if (event != null && event.getResponse() != null) {
                for (VariableBinding vb : event.getResponse().toArray()) {
                    // CSV output with OID
                    System.out.println(timestamp + "," + vb.getOid() + "," + vb.getVariable());
                }
            } else {
                System.err.println("[WARN] No SNMP response received.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] SNMP read failed: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        if (snmp != null) {
            snmp.close();
        }
    }
}
