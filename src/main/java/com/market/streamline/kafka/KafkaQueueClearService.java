package com.market.streamline.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class KafkaQueueClearService {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    private static final List<String> TOPICS_TO_CLEAR = Arrays.asList(
            "candle-added",
            "bos-event-topic",
            "swing-point-event-topic"
    );

    private static final int OPERATION_TIMEOUT_SECONDS = 10;

    /**
     * Optimized method to clear all messages from specified Kafka topics
     * Uses parallel operations and only deletes/recreates existing topics
     */
    public void clearAllQueues() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {

            System.out.println("Starting optimized Kafka queue cleanup...");
            long startTime = System.currentTimeMillis();

            // First, check which topics actually exist to avoid unnecessary operations
            Set<String> existingTopics = getExistingTopics(adminClient);
            List<String> topicsToDelete = TOPICS_TO_CLEAR.stream()
                    .filter(existingTopics::contains)
                    .collect(Collectors.toList());

            if (topicsToDelete.isEmpty()) {
                System.out.println("No existing topics to delete. Creating all topics...");
                createTopicsOptimized(adminClient, TOPICS_TO_CLEAR);
            } else {
                System.out.println("Found existing topics to delete: " + topicsToDelete);

                // Delete only existing topics
                deleteTopicsOptimized(adminClient, topicsToDelete);

                // Short wait for deletion to propagate
                Thread.sleep(500); // Reduced from 2000ms

                // Recreate all topics (both deleted and potentially missing ones)
                createTopicsOptimized(adminClient, TOPICS_TO_CLEAR);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Kafka queue cleanup completed in " + (endTime - startTime) + "ms");

        } catch (Exception e) {
            System.err.println("Error clearing Kafka queues: " + e.getMessage());
            throw new RuntimeException("Failed to clear Kafka queues", e);
        }
    }

    /**
     * Get list of existing topics to avoid unnecessary delete operations
     */
    private Set<String> getExistingTopics(AdminClient adminClient) throws ExecutionException, InterruptedException, TimeoutException {
        ListTopicsResult listResult = adminClient.listTopics();
        return listResult.names().get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Optimized topic deletion with timeout and parallel processing
     */
    private void deleteTopicsOptimized(AdminClient adminClient, List<String> topicsToDelete)
            throws ExecutionException, InterruptedException {

        if (topicsToDelete.isEmpty()) {
            return;
        }

        System.out.println("Deleting topics: " + topicsToDelete);

        DeleteTopicsResult deleteResult = adminClient.deleteTopics(topicsToDelete);

        // Wait for all deletions to complete with timeout
        try {
            deleteResult.all().get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("Successfully deleted all topics: " + topicsToDelete);
        } catch (Exception e) {
            System.err.println("Some topics failed to delete: " + e.getMessage());
            // Continue with recreation anyway
        }
    }

    /**
     * Optimized topic creation with better error handling and parallel processing
     */
    private void createTopicsOptimized(AdminClient adminClient, List<String> topicsToCreate)
            throws ExecutionException, InterruptedException {

        System.out.println("Creating topics: " + topicsToCreate);

        List<NewTopic> newTopics = topicsToCreate.stream()
                .map(topicName -> new NewTopic(topicName, 1, (short) 1))
                .collect(Collectors.toList());

        CreateTopicsResult createResult = adminClient.createTopics(newTopics);

        // Wait for all creations to complete with timeout
        try {
            createResult.all().get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("Successfully created all topics: " + topicsToCreate);
        } catch (Exception e) {
            // Check individual topic creation results
            for (String topicName : topicsToCreate) {
                try {
                    createResult.values().get(topicName).get(1, TimeUnit.SECONDS);
                    System.out.println("Created topic: " + topicName);
                } catch (Exception topicError) {
                    if (topicError.getCause() != null &&
                            topicError.getCause().getMessage().contains("TopicExistsException")) {
                        System.out.println("Topic " + topicName + " already exists, skipping.");
                    } else {
                        System.err.println("Failed to create topic " + topicName + ": " + topicError.getMessage());
                    }
                }
            }
        }
    }
}
