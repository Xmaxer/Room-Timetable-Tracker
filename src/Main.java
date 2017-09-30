import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException {

		Connection connection = DBConnection.establishConnectionToDB();;
		
		Downloader.redownloadData(connection);
		
		getAllRoomNames(connection);

	}

	private static List<String> getAllRoomNames(Connection connection) {
		
		List<String> roomNames = new ArrayList<String>();
		
		try {
			ResultSet rs = connection.getMetaData().getTables(null, null, "%", null);

			while(rs.next())
			{
				roomNames.add(rs.getString("TABLE_NAME"));
			}
		} catch (SQLException e) {
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException ef)
			{
				System.out.println("Error closing connection to database");
			}
			e.printStackTrace();
		}
		
		return roomNames;
	}

}
