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

		DBConnection.createConnection();
		//launch(args);
		
		//Downloader.redownloadData();
		
        String sql = "SELECT * FROM room";
        
        try (

             ResultSet rs    = DBConnection.getStatement().executeQuery(sql)){
            
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString("room_number") +  "\t" + 
                                   rs.getInt("capacity") + "\t");
            }
            
            sql = "SELECT * FROM class";
            ResultSet rs2 = DBConnection.getStatement().executeQuery(sql);
            
            while (rs2.next()) {
            	System.out.println("-----------------------");
                System.out.println(rs2.getString("time") +  "\t" + 
                                   rs2.getString("day") + "\t" +
                		rs2.getString("module") + "\t" +
                                   rs2.getInt("week_number") + "\t" + 
                                   rs2.getString("room_number") + "\t" + 
                                   rs2.getString("class_group") + "\t" +
                                   rs2.getInt("length"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
		

	}

	@Override
	public void start(Stage arg0) throws Exception {
		arg0.show();
	}

}
