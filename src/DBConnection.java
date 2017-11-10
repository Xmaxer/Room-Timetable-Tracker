import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	
	private final static String SQLFile = "rooms.db";
	
	public static Connection establishConnectionToDB() {
		
		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + SQLFile);
			
			System.out.println("Established connection");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return connection;
	}

}
