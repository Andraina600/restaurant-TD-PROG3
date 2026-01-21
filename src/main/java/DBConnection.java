import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(System.getenv("JDBC_URL"), System.getenv("USERNAME"), System.getenv("PASSWORD"));
    }

    public void closeConnection(Connection connection) throws SQLException {
        if(connection != null){
            try {
                connection.close();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        }
    }
}
