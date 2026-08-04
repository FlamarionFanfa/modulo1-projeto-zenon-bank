import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class IngestionMain {

    private static final String DEFAULT_DATA_FILE = "PS_20174392719_1491204439457_log.csv";

    public static void main(String[] args) throws Exception {
        String dataFile = resolveDataFile(args.length > 0 ? args[0] : DEFAULT_DATA_FILE);
        TransactionSQLRepository repository = new TransactionSQLRepository();
        String mode = args.length > 1 ? args[1] : "all";

        System.out.println("Banco de dados conectado!");
        System.out.println("Arquivo usado: " + dataFile);
        System.out.println();

        switch (mode) {
            case "single" -> runSingleInsertScenario(repository, dataFile);
            case "limited-batch" -> runLimitedBatchScenario(repository, dataFile);
            case "full" -> runFullBatchScenarios(repository, dataFile);
            default -> {
                runSingleInsertScenario(repository, dataFile);
                runLimitedBatchScenario(repository, dataFile);
                runFullBatchScenarios(repository, dataFile);
            }
        }
    }

    private static void runSingleInsertScenario(TransactionSQLRepository repository, String dataFile)
            throws IOException, SQLException {
        System.out.println("Iniciando cenario: Stream + insert unitario (10 mil)");
        repository.deleteAll();
        ScenarioAccumulator expected = new ScenarioAccumulator();
        long[] progress = {0};

        long start = System.nanoTime();
        long processed;
        try (EfficientTransactionIngestor ingestor = new EfficientTransactionIngestor(1)) {
            processed = ingestor.readAsStream(dataFile, EfficientTransactionIngestor.STREAM_LIMIT, transaction -> {
                try {
                    repository.save(transaction);
                    expected.add(transaction);
                    progress[0]++;
                    if (progress[0] % 1_000 == 0) {
                        System.out.println("Insercoes unitarias concluidas: " + progress[0]);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Erro ao salvar transacao individualmente", e);
                }
            });
        }
        long elapsed = System.nanoTime() - start;

        printScenario(
                "Stream + insert unitario (10 mil)",
                processed,
                elapsed,
                expected.toStatistics(),
                repository.calculateStatistics()
        );
    }

    private static void runLimitedBatchScenario(TransactionSQLRepository repository, String dataFile)
            throws IOException, SQLException {
        System.out.println("Iniciando cenario: Batch JDBC (10 mil)");
        repository.deleteAll();
        ScenarioAccumulator expected = new ScenarioAccumulator();
        long[] batches = {0};

        long start = System.nanoTime();
        long processed;
        try (EfficientTransactionIngestor ingestor = new EfficientTransactionIngestor(1)) {
            processed = ingestor.readBatch(
                    dataFile,
                    EfficientTransactionIngestor.BATCH_SIZE,
                    EfficientTransactionIngestor.STREAM_LIMIT,
                    batch -> {
                        try {
                            repository.saveBatch(batch);
                            expected.addBatch(batch);
                            batches[0]++;
                            System.out.println("Lotes limitados concluidos: " + batches[0]);
                        } catch (SQLException e) {
                            throw new RuntimeException("Erro ao salvar lote limitado", e);
                        }
                    }
            );
        }
        long elapsed = System.nanoTime() - start;

        printScenario(
                "Batch JDBC (10 mil)",
                processed,
                elapsed,
                expected.toStatistics(),
                repository.calculateStatistics()
        );
    }

    private static void runFullBatchScenarios(TransactionSQLRepository repository, String dataFile)
            throws IOException, SQLException {
        System.out.println("Calculando estatisticas esperadas do arquivo completo...");
        TransactionReport.Statistics expected = new TransactionReport().generateReport(dataFile);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        Set<String> scenarios = new LinkedHashSet<>();
        scenarios.add("fixed:1");
        scenarios.add("fixed:" + Math.max(2, availableProcessors));
        scenarios.add("virtual");

        for (String scenario : scenarios) {
            System.out.println("Iniciando cenario: Batch JDBC arquivo completo [" + scenario + "]");
            repository.deleteAll();

            long start = System.nanoTime();
            long processed;

            if (scenario.startsWith("fixed:")) {
                int threadCount = Integer.parseInt(scenario.substring("fixed:".length()));
                ProgressTracker tracker = new ProgressTracker("Lotes concluidos [" + scenario + "]");
                try (EfficientTransactionIngestor ingestor = new EfficientTransactionIngestor(threadCount)) {
                    processed = ingestor.readBatch(dataFile, batch -> saveBatchWithProgress(repository, batch, tracker));
                }
            } else {
                ProgressTracker tracker = new ProgressTracker("Lotes concluidos [" + scenario + "]");
                try (EfficientTransactionIngestor ingestor =
                             new EfficientTransactionIngestor(Executors.newVirtualThreadPerTaskExecutor())) {
                    processed = ingestor.readBatch(dataFile, batch -> saveBatchWithProgress(repository, batch, tracker));
                }
            }

            long elapsed = System.nanoTime() - start;
            printScenario(
                    "Batch JDBC arquivo completo [" + scenario + "]",
                    processed,
                    elapsed,
                    expected,
                    repository.calculateStatistics()
            );
        }
    }

    private static void saveBatch(TransactionSQLRepository repository, List<Transaction> batch) {
        try {
            repository.saveBatch(batch);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar lote no banco", e);
        }
    }

    private static void saveBatchWithProgress(
            TransactionSQLRepository repository,
            List<Transaction> batch,
            ProgressTracker tracker
    ) {
        saveBatch(repository, batch);
        tracker.increment();
    }

    private static void printScenario(
            String scenarioName,
            long processed,
            long elapsedNanos,
            TransactionReport.Statistics expected,
            TransactionSQLRepository.DatabaseStatistics actual
    ) {
        boolean countMatches = expected.totalTransactions() == actual.totalTransactions();
        boolean fraudMatches = expected.totalFrauds() == actual.totalFrauds();
        boolean amountMatches = expected.totalAmount().compareTo(actual.totalAmount()) == 0;

        System.out.println("=== " + scenarioName + " ===");
        System.out.println("Transacoes processadas: " + processed);
        System.out.println("Tempo total: " + formatDuration(elapsedNanos));
        System.out.println("Esperado -> total=" + expected.totalTransactions()
                + ", fraudes=" + expected.totalFrauds()
                + ", valor=" + expected.totalAmount());
        System.out.println("Banco    -> total=" + actual.totalTransactions()
                + ", fraudes=" + actual.totalFrauds()
                + ", valor=" + actual.totalAmount());
        System.out.println("Corretude -> total=" + countMatches
                + ", fraudes=" + fraudMatches
                + ", valor=" + amountMatches);
        System.out.println();
    }

    private static String formatDuration(long elapsedNanos) {
        return String.format("%.3f s", elapsedNanos / 1_000_000_000.0);
    }

    private static String resolveDataFile(String fileName) {
        Path moduleRelative = Path.of("data", fileName);
        if (Files.exists(moduleRelative)) {
            return moduleRelative.toString();
        }

        Path repoRelative = Path.of("..", "data", fileName);
        if (Files.exists(repoRelative)) {
            return repoRelative.toString();
        }

        throw new IllegalArgumentException("Data file not found: " + fileName);
    }

    private static final class ScenarioAccumulator {
        private long totalTransactions;
        private long totalFrauds;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        private void add(Transaction transaction) {
            totalTransactions++;
            if (transaction.isFraud()) {
                totalFrauds++;
            }
            totalAmount = totalAmount.add(transaction.amount());
        }

        private void addBatch(List<Transaction> transactions) {
            for (Transaction transaction : transactions) {
                add(transaction);
            }
        }

        private TransactionReport.Statistics toStatistics() {
            return new TransactionReport.Statistics(totalTransactions, totalFrauds, totalAmount);
        }
    }

    private static final class ProgressTracker {
        private final String label;
        private long value;

        private ProgressTracker(String label) {
            this.label = label;
        }

        private synchronized void increment() {
            value++;
            System.out.println(label + ": " + value);
        }
    }
}
