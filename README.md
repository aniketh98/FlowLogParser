# Flow Log Parser

## Overview

The **Flow Log Parser** is a Java-based program designed to parse flow log data and map each row to a tag based on a lookup table. The lookup table, provided as a CSV file, contains three columns: `dstport`, `protocol`, and `tag`. The combination of `dstport` and `protocol` from the flow logs is used to determine the corresponding tag from the lookup table.

The program generates two types of output:
1. **Tag Counts**: The number of matches for each tag.
2. **Port/Protocol Combination Counts**: The number of occurrences for each unique `dstport` and `protocol` combination.

### Key Features:
- Handles flow logs of up to **10 MB** in size.
- Supports a lookup table with up to **10,000 mappings**.
- **Case-insensitive** matching of protocol and tag data.
- Generates reports for:
  - **Tag Counts** (including "Untagged" for unmatched rows).
  - **Port/Protocol Combination Counts**.
- Handles invalid input rows (e.g., missing columns).
- 
## Prerequisites

- **Java Development Kit (JDK) 8 or higher**.
- **VS Code** (with Java extensions installed).

### To run the code:

```bash
javac FlowLogParser.java
java FlowLogParser <flowlogfile> <lookupfile>
```

### View the Output

After running the program, two reports will be generated in the `output/` directory:

- `tag_counts.csv` - Count of matches for each tag.
- `port_protocol_counts.csv` - Count of matches for each unique port/protocol combination.
