import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{

	public static void main(String[] args) throws IOException {

		//launch(args);
		Connection connection = DBConnection.establishConnectionToDB();;
		
		Downloader.redownloadData(connection);
		
		System.out.println(getAllRoomNames(connection));
		
        String sql = "SELECT * FROM room";
        
        try (
             Statement stmt  = connection.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString("room_number") +  "\t" + 
                                   rs.getInt("capacity") + "\t");
            }
            
            sql = "SELECT * FROM class";
            ResultSet rs2 = stmt.executeQuery(sql);
            
            while (rs2.next()) {
            	System.out.println("-----------------------");
                System.out.println(rs2.getString("time") +  "\t" + 
                                   rs2.getString("day") + "\t" +
                		rs2.getString("module") + "\t" +
                                   rs2.getInt("week_number") + "\t" + 
                                   rs2.getString("room_number") + "\t" + 
                                   rs2.getString("class_group"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
		
		try
		{
			if(connection != null)
				connection.close();
		}
		catch(SQLException ef)
		{
			System.out.println("Error closing connection to database");
		}

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

	@Override
	public void start(Stage arg0) throws Exception {
		arg0.show();
	}

}
