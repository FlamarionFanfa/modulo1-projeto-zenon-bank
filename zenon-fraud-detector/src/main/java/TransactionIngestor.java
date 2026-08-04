import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;



public class TransactionIngestor {

    private static final int TRANSACTION_LIMIT = 10_000;
    private final TransactionLineParser parser = new TransactionLineParser();



    public List<Transaction> readNew(String filename) throws IOException {

        Path path = Path.of(filename);

        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .limit(TRANSACTION_LIMIT)
                    .map(parser::parse)
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
                Optional<Transaction> transaction = parser.parse(line);
                transactions.add(transaction);

            }


        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo " + filename, e);
        }

        return transactions;
    }

}
