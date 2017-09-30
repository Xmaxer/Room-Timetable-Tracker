import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {


	public static void redownloadData(Connection connection) {
		
	
		List<Room> rooms = getAllRoomsData();


		
		for(int roomIndex = 0; roomIndex < rooms.size(); roomIndex++)
		{

			String roomNumber = rooms.get(roomIndex).getRoomNumber();
			int roomCapacity = rooms.get(roomIndex).getRoomCapacity();

			String tableName = roomNumber;


			try
			{
				
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30);

				statement.execute("DROP TABLE IF EXISTS " + tableName);
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (ID int IDENTITY(1,1) PRIMARY KEY, time time NOT NULL, day VARCHAR(15) NOT NULL, class_groups VARCHAR(255), module VARCHAR(255), weeks VARCHAR(255), capacity int, UNIQUE(time, day))");

				for(Class currentClass : rooms.get(roomIndex).getClasses())
				{
					String time = currentClass.getTime();
					String day = currentClass.getDay();
					List<String> classGroups = currentClass.getClassGroups();
					String module = currentClass.getModule();
					List<String> weeks = currentClass.getWeeks();

					statement.executeUpdate("INSERT INTO " + tableName + " (time, day, class_groups, module, weeks, capacity) values ('" + time + "','" + day + "','" + ((classGroups != null) ? String.join(",", classGroups) : "") + "','" + module + "','" + ((weeks != null) ? String.join(",", weeks) : "") + "'," + roomCapacity + ")");

				}
			}catch(Exception e)
			{
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
		}

	}

	private static List<Room> getAllRoomsData() {

		List<Room> rooms = new ArrayList<Room>();

		Document doc = downloadPage();

		if(doc != null)
		{

			Elements tables = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(2) > tbody > tr");

			Elements tableInfo = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(1) > tbody > tr > td > table > tbody > tr > td:first-child");

			List<String> days = configureValidDaysFromTable(doc);

			for(int j = 1; j < tables.size(); j+=57)
			{
				String room = tableInfo.get(j/57).text();

				Room currentRoom = new Room(room);

				List<Class> classes = getAllClassesForRoom(j, tables, days);

				currentRoom.setClasses(classes);
				rooms.add(currentRoom);
			}
		}
		return rooms;
	}

	private static List<Class> getAllClassesForRoom(int j, Elements tables, List<String> days) {

		List<Class> classes = new ArrayList<Class>();

		for(int trip = j; trip < (j+56); trip++)
		{
			classes.addAll(getAllClassesForAllTimes(j, tables, days, trip));
		}

		return classes;
	}

	private static List<Class> getAllClassesForAllTimes(int j, Elements tables, List<String> days, int trip) {

		List<Class> classes = new ArrayList<Class>();

		Elements specificTimeData = tables.get(trip).select("td");

		//This is to remove all junk elements which we don't need.
		//In this case all 'align' attribute elements are not needed.
		for(int i = 0; i < specificTimeData.size(); i++)
		{
			if(specificTimeData.get(i).hasAttr("align"))
			{
				specificTimeData.remove(i);
				i = -1;
				continue;
			}
		}

		String time = null;

		for(int i = 0; i < specificTimeData.size(); i++)
		{
			if(i == 0)
			{
				time = specificTimeData.get(i).text();
			}
			else
			{
				String day = days.get(i - 1);
				Class classOn = new Class(time, day, null, null, null);

				if(specificTimeData.get(i).hasAttr("rowspan"))
				{
					Elements classDataTables = specificTimeData.get(i).select("table");

					String[] classGroups = classDataTables.get(0).select("tbody > tr > td").text().split(",");
					String module = classDataTables.get(1).select("tbody > tr > td").text();
					String[] weeks = classDataTables.get(2).select("tbody > tr > td").text().split(",");

					classOn = new Class(time, day, Arrays.asList(classGroups), module, Arrays.asList(weeks));
				}

				//System.out.println(time);
				classes.add(classOn);
			}
		}

		return classes;
	}

	private static Document downloadPage() {

		File input = new File("Testing.html");

		Document doc = null;
		try {
			doc = Jsoup.parse(input, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return doc;
	}

	private static List<String> configureValidDaysFromTable(Document doc) {

		Elements daysTableData = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(2) > tbody > tr:first-child > td");

		List<String> days = new ArrayList<String>();

		for(Element dayTableData : daysTableData)
		{
			if(!dayTableData.text().isEmpty() && !days.contains(dayTableData.text()))
			{
				days.add(dayTableData.text());
			}
		}

		return days;
	}

}
