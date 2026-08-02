import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/zenon_frauds","root", "senha123");
        } catch (SQLException e) {
            throw new SQLException("Erro ao conectar ao banco de dados", e);
        }

    }
}

