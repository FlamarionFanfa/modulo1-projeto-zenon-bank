import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository{
    @Override
    public Optional<Transaction> findByOriginName(String originName) throws SQLException {
        String sql = """
                SELECT step, `type`, amount, nome_origin,
           old_balance_origin, new_balance_origin,
           name_recipient, old_balance_recipient,
           new_balance_recipient, is_fraud, is_flagged_fraud
                FROM transactions
    WHERE nome_origin = ?
    ORDER BY step
    LIMIT 1
    """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1,originName);

            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    TransactionCustomer origin = new TransactionCustomer(
                            rs.getString("nome_origin"),
                            rs.getBigDecimal("old_balance_origin"),
                            rs.getBigDecimal("new_balance_origin")
                    );

                    TransactionCustomer recipient = new TransactionCustomer(
                            rs.getString("name_recipient"),
                            rs.getBigDecimal("old_balance_recipient"),
                            rs.getBigDecimal("new_balance_recipient")
                    );

                    Transaction transaction = new Transaction(
                            rs.getInt("step"),
                            TransactionType.valueOf(rs.getString("type")),
                            rs.getBigDecimal("amount"),
                            origin,
                            recipient,
                            rs.getBoolean("is_fraud"),
                            rs.getBoolean("is_flagged_fraud")
                    );

                    return Optional.of(transaction);
                } else {
                    IO.println("Transaction not found to origin name: " + originName);
                }


            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }
}
