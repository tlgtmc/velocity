package org.example;

import java.time.Instant;
import java.util.concurrent.*;
class Geeks {
    
    public static void main(String[] args)
    {
        ConcurrentHashMap<String, ConcurrentLinkedDeque<Integer>> cardUsageHistory = new ConcurrentHashMap<>();

        cardUsageHistory.computeIfAbsent("123", k -> new ConcurrentLinkedDeque<>()).addLast(1);
        cardUsageHistory.computeIfAbsent("123", k -> new ConcurrentLinkedDeque<>()).addLast(2);
        cardUsageHistory.computeIfAbsent("123", k -> new ConcurrentLinkedDeque<>()).addLast(3);
        cardUsageHistory.computeIfAbsent("123", k -> new ConcurrentLinkedDeque<>()).addLast(4);
        cardUsageHistory.computeIfAbsent("456", k -> new ConcurrentLinkedDeque<>()).addLast(4);
        cardUsageHistory.computeIfAbsent("456", k -> new ConcurrentLinkedDeque<>()).addLast(5);
        cardUsageHistory.computeIfAbsent("456", k -> new ConcurrentLinkedDeque<>()).addLast(6);

        System.out.println("Card 123 usage history: " + cardUsageHistory.get("123"));
        System.out.println("Card 456 usage history: " + cardUsageHistory.get("456"));

    }
}