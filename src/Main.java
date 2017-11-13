import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main { //extends Application{

	public static void main(String[] args) throws IOException {

		DBConnection.createConnection();
		//launch(args);

		Downloader.redownloadData();

		LocalDateTime now = LocalDateTime.now();

		/*		Scanner input = new Scanner(System.in);

		System.out.print("Input date: ");
		String day = input.nextLine();
		System.out.print("Input time: ");
		String time = input.next();*/

		String day = "14";
		String time = "13:00";
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.DAY_OF_MONTH) <= Integer.valueOf(day))
		{
			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
			day = getDayFromNumber(cal.get(Calendar.DAY_OF_WEEK));
			getRoomUsage(day, time, cal.get(Calendar.WEEK_OF_YEAR));
		}


		//System.out.println(now);

		//printAll();
		DBConnection.closeConnection();

	}

	private static String getDayFromNumber(int number) {

		switch(number)
		{
		case 2:
			return "Monday";
		case 3:
			return "Tuesday";
		case 4:
			return "Wednesday";
		case 5:
			return "Thursday";
		case 6:
			return "Friday";
		case 0:
			return "Saturday";
		case 1:
			return "Sunday";
		default:
			return null;
		}
	}

	private static void getRoomUsage(String day, String time, int week) {
		//TO DO implement smart system that detects things like 16:30
		try {

			List<String> rooms = new ArrayList<String>();
			String roomQuery = "Select room_number FROM class GROUP BY room_number";

			ResultSet roomResult = DBConnection.getStatement().executeQuery(roomQuery);

			while(roomResult.next())
				rooms.add(roomResult.getString("room_number"));

			String sql = "SELECT * FROM class WHERE day='" + day + "' AND time='" + time + "' AND week_number='" + week + "'COLLATE NOCASE";
			ResultSet rs = DBConnection.getStatement().executeQuery(sql);

			while(rs.next()) {
				String rn = rs.getString("room_number");
				if(rooms.contains(rn))
					rooms.remove(rn);
			}
			
			for(String room : rooms)
			{
				System.out.println(room + " is available on " + day + " at " + time);
			}

		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void printAll() {
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

	/*	@Override
	public void start(Stage arg0) throws Exception {
		arg0.show();
	}*/

}
