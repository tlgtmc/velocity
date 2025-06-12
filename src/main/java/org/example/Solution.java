package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.time.*;

public class Solution {

    static class Payment {
        /* The payment ID. */
        private final String paymentId;
        /* The timestamp of the payment processing start. */
        private final Instant timestamp;
        /* The hashed card number used for the payment. */
        private final String hashedCardNumber;

        public Payment(String paymentId, Instant timestamp, String hashedCardNumber) {
            this.paymentId = paymentId;
            this.timestamp = timestamp;
            this.hashedCardNumber = hashedCardNumber;
        }

        public String getPaymentId() {
            return paymentId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public String getHashedCardNumber() {
            return hashedCardNumber;
        }
    }

    interface VelocityProvider {

        /**
         * This method is called during the payment risk assessment.
         *
         * It returns how many times the card in the Payment has been seen in the last minutes/seconds/hours as
         * defined in the {@code duration} parameter at the time of the payment processing start.
         *
         * @param payment  The payment being processed
         * @param duration The interval to count
         * @return The number of times the card was used in the interval defined in duration.
         */
        int getCardUsageCount(Payment payment, Duration duration);

        /**
         * After the payment is processed this method is called.
         *
         * @param payment The payment that has been processed.
         */
        void registerPayment(Payment payment);

        /**
         * @return Instance of a Velocity provider
         */
        static VelocityProvider getProvider() {
            return new VelocityProviderImpl();
        }
    }

    static class VelocityProviderImpl implements VelocityProvider {

        private ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>> cardUsageHistory = new ConcurrentHashMap<>();

        @Override
        public int getCardUsageCount(Solution.Payment payment, Duration duration) {
            String cardHash = payment.getHashedCardNumber();
            Instant paymentTime = payment.getTimestamp();
            Instant cutOff = paymentTime.minus(duration);

            ConcurrentLinkedDeque<Instant> usageDeque = cardUsageHistory.get(cardHash);
            if(usageDeque == null) {
                return 0;
            }

            int count = 0;

            for(Instant time: usageDeque) {
                if(!time.isBefore(cutOff) && !time.isAfter(paymentTime)) {
                    count++;
                }
            }

            return count;
        }

        @Override
        public void registerPayment(Solution.Payment payment) {
            String cardHash = payment.getHashedCardNumber();
            Instant timestamp = payment.getTimestamp();

            cardUsageHistory.computeIfAbsent(cardHash, id -> new ConcurrentLinkedDeque<>()).addLast(timestamp);
        }
    }

    public static void main(String args[]) throws Exception {
        final VelocityProvider velocityProvider = VelocityProvider.getProvider();

        try (final Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String assoc = scanner.next();
                final String[] split = assoc.split(":");

                final String operation = split[0];

                if (split.length == 3 && "register".equals(operation)) {
                    final long timestamp = Long.parseLong(split[1]);
                    final String hashedCardNumber = split[2];
                    final Payment payment = new Payment(UUID.randomUUID().toString(), Instant.ofEpochMilli(timestamp), hashedCardNumber);

                    velocityProvider.registerPayment(payment);
                } else if (split.length == 4 &&  "get".equals(operation)) {
                    final long queryTime = Long.parseLong(split[1]);
                    final String hashedCardNumber = split[2];
                    final long durationInSeconds = Long.parseLong(split[3]);
                    System.out.println(velocityProvider.getCardUsageCount(new Payment(UUID.randomUUID().toString(), Instant.ofEpochMilli(queryTime), hashedCardNumber), Duration.ofSeconds(durationInSeconds)));
                } else {
                    throw new RuntimeException("Invalid test input");
                }
            }
        }
    }
}