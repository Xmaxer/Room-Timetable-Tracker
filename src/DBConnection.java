import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	
	private final static String SQLFile = "rooms.db";
	private static Connection connection;
	private static Statement statement;
	
	public static void createConnection()
	{
		connection = null;
		statement = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + SQLFile);
			statement =  connection.createStatement();
			statement.setQueryTimeout(30);
			
			System.out.println("Established connection");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the statement
	 */
	public static Statement getStatement() {
		return statement;
	}

	/**
	 * @return the connection
	 */
	public static Connection getConnection() {
		return connection;
	}
	
	public static void closeConnection()
	{
		try
		{
			if(connection != null)
				connection.close();
		}
		catch(SQLException ef)
		{
			System.out.println("Error closing connection to database");
			ef.printStackTrace();
		}
	}

}
