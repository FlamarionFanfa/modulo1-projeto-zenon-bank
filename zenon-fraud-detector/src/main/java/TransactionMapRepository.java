import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionMapRepository implements TransactionRepository{

    private final Map<String, Transaction> transactionsByOriginName;

    public TransactionMapRepository(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);
        this.transactionsByOriginName = transactions
                .stream()
                .collect(Collectors.toMap(
                        transaction -> transaction.origin().name(),
                        Function.identity(),
                        (existing, ignored) -> existing,
                        LinkedHashMap::new
                ));

    }


    @Override
    public Optional<Transaction> findByOriginName(String name) {
        return Optional.ofNullable(transactionsByOriginName.get(name));

    }
}
