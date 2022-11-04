package com.kony.adminconsole.commons.utils;

public final class CUID {

    private static final long NODE_ID = 1;
    private static short nextId = 1;

    public static synchronized Long nextId() {
        // Push the Node_ID to max bits
        long maxBitNodeId = NODE_ID << (Long.numberOfLeadingZeros(NODE_ID) - 1);

        int nodeIdZeroBitSize = Long.numberOfTrailingZeros(maxBitNodeId);

        // Create timestamp and shift the bits next to Node ID
        long currentTime = System.currentTimeMillis();
        long maxBitTimestamp = currentTime << (Long.numberOfLeadingZeros(currentTime) - (64 - nodeIdZeroBitSize));

        // Because timestamp can give only millisecond precision add a incremental Id.
        long uniqueClusterId;
        if (nextId < Short.MAX_VALUE) {
            uniqueClusterId = maxBitNodeId | maxBitTimestamp | nextId++;
        } else {
            // Counter Reset after reaching the SHORT max value.
            nextId = 1;
            uniqueClusterId = maxBitNodeId | maxBitTimestamp | nextId++;
        }
        return uniqueClusterId;
    }

}