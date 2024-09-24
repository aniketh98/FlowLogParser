import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowLogParser {

    private static final Map<String, String> tagLookup = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> tagCount = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> portProtocolCount = new ConcurrentHashMap<>();
    private static final String UNTAGGED = "Untagged";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FlowLogParser <flowlogfile> <lookupfile>");
            return;
        }

        String flowLogFile = args[0];
        String lookupFile = args[1];

        try {
            loadLookupTable(lookupFile);
            processFlowLogs(flowLogFile);
            generateOutputFiles();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Load the lookup table into a map
    private static void loadLookupTable(String lookupFile) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(lookupFile));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String portProtocolKey = parts[0] + "," + parts[1].toLowerCase();
                tagLookup.put(portProtocolKey, parts[2]);
            }
        }
        System.out.println(tagLookup);
    }

    // Process flow logs using a thread pool
    private static void processFlowLogs(String flowLogFile) throws IOException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<String> flowLogs = Files.readAllLines(Paths.get(flowLogFile));

        for (String log : flowLogs) {
            executor.submit(() -> processFlowLog(log));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }

    // Process each flow log entry
    private static void processFlowLog(String log) {
        try {
            String[] parts = log.split(" ");
            if (parts.length < 13) {
                // If any column is missing, skip and log the exception
                System.err.println("Invalid log entry: " + log);
                return;
            }

            String dstPort = parts[5];
            String protocol = parts[7].equals("6") ? "tcp" : parts[7].equals("17") ? "udp" : "other";  // Simple protocol parsing
            String portProtocolKey = dstPort + "," + protocol;

            String tag = tagLookup.getOrDefault(portProtocolKey, UNTAGGED);

            tagCount.computeIfAbsent(tag, k -> new AtomicInteger(0)).incrementAndGet();

            portProtocolCount.computeIfAbsent(portProtocolKey, k -> new AtomicInteger(0)).incrementAndGet();

        } catch (Exception e) {
            System.err.println("Error processing log entry: " + log);
        }
    }

    // Generate output files
    private static void generateOutputFiles() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("tag_counts.csv"))) {
            writer.write("Tag,Count\n");
            for (Map.Entry<String, AtomicInteger> entry : tagCount.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue().get() + "\n");
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("port_protocol_counts.csv"))) {
            writer.write("Port,Protocol,Count\n");
            for (Map.Entry<String, AtomicInteger> entry : portProtocolCount.entrySet()) {
                String[] portProtocol = entry.getKey().split(",");
                writer.write(portProtocol[0] + "," + portProtocol[1] + "," + entry.getValue().get() + "\n");
            }
        }
    }
}
