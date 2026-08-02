import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;



public class TransactionIngestor {



    public List<Transaction> readNew(String filename) throws IOException {

        Path path = Path.of(filename);
        final int FRAUD_LIMITED = 100_000;

        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .limit(FRAUD_LIMITED)
                    .map(this::parseTransaction)
                    .flatMap(Optional::stream)
                    .toList();
        }

    }


    public List<Optional<Transaction>> readOld(String filename) {

        List<Optional<Transaction>> transactions = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filename);
             Scanner scanner = new Scanner(fis)) {

            int lineCount = 0;
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineCount++;

                if (lineCount == 1001) {
                    break;
                }
                Optional<Transaction> transaction = parseTransaction(line);
                transactions.add(transaction);

            }


        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo " + filename, e);
        }

        return transactions;
    }

    private Optional<Transaction> parseTransaction(String line) {
        try {

        String[] chunks = line.split(",");
        if (chunks.length < 11) {
            throw new IllegalArgumentException("Line with invalid format: " + line);
        }
        int step = Integer.parseInt(chunks[0]);
        TransactionType type = TransactionType.valueOf(chunks[1]);

        if (chunks[2] == null || chunks[2].isEmpty()) throw new IllegalArgumentException("Amount is null or empty: " + chunks[2]);

        BigDecimal amount = new BigDecimal(chunks[2]);
        TransactionCustomer origin = new TransactionCustomer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
        TransactionCustomer recipient = new TransactionCustomer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));
        boolean isFraud = "1".equals(chunks[9]);
        boolean isFlaggedFraud = "1".equals(chunks[10]);

        return Optional.of(new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud));

        }
        catch (Exception e) {
            System.err.println("Erro ao ao fazer o parse: " + line + " - " + e.getMessage());
            //e.printStackTrace();

            return Optional.empty();


        }

    }
}
