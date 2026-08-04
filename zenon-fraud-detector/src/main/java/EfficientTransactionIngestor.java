import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorCompletionService;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EfficientTransactionIngestor implements AutoCloseable {

    public static final int STREAM_LIMIT = 10_000;
    public static final int BATCH_SIZE = 10_000;
    private static final int MAX_PENDING_MULTIPLIER = 4;

    private final TransactionLineParser parser = new TransactionLineParser();
    private final ExecutorService executorService;

    public EfficientTransactionIngestor() {
        this(Math.max(1, Runtime.getRuntime().availableProcessors()));
    }

    public EfficientTransactionIngestor(int threadCount) {
        if (threadCount < 1) {
            throw new IllegalArgumentException("Thread count must be at least 1");
        }
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public EfficientTransactionIngestor(ExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService);
    }

    public long readAsStream(String filename, Consumer<Transaction> consumer) throws IOException {
        return readAsStream(filename, STREAM_LIMIT, consumer);
    }

    public long readAsStream(String filename, int maxTransactions, Consumer<Transaction> consumer) throws IOException {
        Objects.requireNonNull(consumer);
        if (maxTransactions < 1) {
            throw new IllegalArgumentException("Max transactions must be at least 1");
        }

        long processed = 0;
        Path path = Path.of(filename);

        try (Stream<String> lines = Files.lines(path)) {
            Iterator<String> iterator = lines.skip(1).iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                Optional<Transaction> transaction = parser.parse(line);
                if (transaction.isEmpty()) {
                    continue;
                }

                consumer.accept(transaction.get());
                processed++;

                if (processed >= maxTransactions) {
                    break;
                }
            }
        }

        return processed;
    }

    public long readBatch(String filename, Consumer<List<Transaction>> consumer) throws IOException {
        return readBatch(filename, BATCH_SIZE, Integer.MAX_VALUE, consumer);
    }

    public long readBatch(String filename, int batchSize, Consumer<List<Transaction>> consumer) throws IOException {
        return readBatch(filename, batchSize, Integer.MAX_VALUE, consumer);
    }

    public long readBatch(
            String filename,
            int batchSize,
            int maxTransactions,
            Consumer<List<Transaction>> consumer
    ) throws IOException {
        Objects.requireNonNull(consumer);
        if (batchSize < 1) {
            throw new IllegalArgumentException("Batch size must be at least 1");
        }
        if (maxTransactions < 1) {
            throw new IllegalArgumentException("Max transactions must be at least 1");
        }

        Path path = Path.of(filename);
        List<Transaction> currentBatch = new ArrayList<>(batchSize);
        long processed = 0;
        int submittedTasks = 0;
        int completedTasks = 0;
        int maxPendingTasks = Math.max(1, Runtime.getRuntime().availableProcessors() * MAX_PENDING_MULTIPLIER);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        try (Stream<String> lines = Files.lines(path)) {
            Iterator<String> iterator = lines.skip(1).iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                Optional<Transaction> transaction = parser.parse(line);
                if (transaction.isEmpty()) {
                    continue;
                }

                currentBatch.add(transaction.get());
                processed++;

                if (processed >= maxTransactions) {
                    break;
                }

                if (currentBatch.size() == batchSize) {
                    submitBatch(currentBatch, consumer, completionService);
                    submittedTasks++;
                    currentBatch = new ArrayList<>(batchSize);
                    completedTasks += drainCompletedTasks(completionService);

                    if ((submittedTasks - completedTasks) >= maxPendingTasks) {
                        waitForNextBatch(completionService);
                        completedTasks++;
                    }
                }
            }
        }

        if (!currentBatch.isEmpty()) {
            submitBatch(currentBatch, consumer, completionService);
            submittedTasks++;
        }

        while (completedTasks < submittedTasks) {
            waitForNextBatch(completionService);
            completedTasks++;
        }
        return processed;
    }

    private void submitBatch(
            List<Transaction> batch,
            Consumer<List<Transaction>> consumer,
            CompletionService<Void> completionService
    ) {
        List<Transaction> immutableBatch = List.copyOf(batch);
        completionService.submit(() -> {
            consumer.accept(immutableBatch);
            return null;
        });
    }

    private int drainCompletedTasks(CompletionService<Void> completionService) {
        int drained = 0;
        while (true) {
            var future = completionService.poll();
            if (future == null) {
                return drained;
            }
            awaitFuture(future);
            drained++;
        }
    }

    private void waitForNextBatch(CompletionService<Void> completionService) {
        try {
            awaitFuture(completionService.take());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrompida durante o processamento dos lotes", e);
        }
    }

    private void awaitFuture(java.util.concurrent.Future<Void> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrompida durante o processamento dos lotes", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao processar lote em paralelo", e.getCause());
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
