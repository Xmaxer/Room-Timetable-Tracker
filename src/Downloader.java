import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {

	//private static Connection connection;

	public static void redownloadData(Connection connection) {


		List<Room> rooms = getAllRoomsData();

		try
		{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);

			String classTable = "class";
			String roomTable = "room";

			statement.execute("DROP TABLE IF EXISTS " + roomTable);
			statement.execute("DROP TABLE IF EXISTS " + classTable);
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS room" + 
					"(room_number VARCHAR(20) PRIMARY KEY," + 
					"capacity int" + 
					");");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS class" + 
					"(time time NOT NULL," + 
					"day VARCHAR(10) NOT NULL," + 
					"module VARCHAR(20) NOT NULL," + 
					"week_number int NOT NULL," + 
					"room_number VARCHAR(20) NOT NULL," + 
					"class_group VARCHAR(255) NOT NULL," + 
					"PRIMARY KEY (time, day, week_number, class_group, module)," + 
					"FOREIGN KEY (room_number) REFERENCES room (room_number) ON DELETE RESTRICT ON UPDATE CASCADE" + 
					");");

			for(int roomIndex = 0; roomIndex < rooms.size(); roomIndex++)
			{

				String roomNumber = rooms.get(roomIndex).getRoomNumber();
				int roomCapacity = rooms.get(roomIndex).getRoomCapacity();

				statement.executeUpdate("INSERT INTO " + roomTable + " (room_number, capacity) VALUES (" +
						"'" + roomNumber + "','" + roomCapacity + "')");

				for(Class currentClass : rooms.get(roomIndex).getClasses())
				{
					String time = currentClass.getTime();
					String day = currentClass.getDay();
					List<String> classGroups = currentClass.getClassGroups();
					String module = currentClass.getModule();
					List<String> weeks = currentClass.getWeeks();

					if(weeks != null)
					{
						for(String week : weeks)
						{
							for(String classGroup : classGroups)
							{
								statement.executeUpdate("INSERT INTO " + classTable + " (time, day, module, week_number, room_number, class_group) values (" + 
										"'" + time + "'," + 
										"'" + day + "'," + 
										"'" + module + "'," + 
										"'" + week + "'," + 
										"'" + roomNumber + "'," + 
										"'" + classGroup + "')");
							}
						}
					}
				}


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
					List<String> weeks = new LinkedList<String>(Arrays.asList(classDataTables.get(2).select("tbody > tr > td").text().split(",")));

					String weekFormat = "MMMd";

					for(int x = 0; x < weeks.size(); x++)
					{
						weeks.set(x, weeks.get(x).replaceAll("wk", "").replaceAll(" ", ""));
						
						SimpleDateFormat df = new SimpleDateFormat(weekFormat);
						try {
							String[] possibleWeeks = weeks.get(x).split("-");
							if(possibleWeeks.length == 2)
							{
								Date date1 = df.parse(possibleWeeks[0]);
								Date date2 = df.parse(possibleWeeks[1]);
								Calendar cal = Calendar.getInstance();
								
								cal.setTime(date1);
								int week1 = cal.get(Calendar.WEEK_OF_YEAR);	
								cal.setTime(date2);
								int week2 = cal.get(Calendar.WEEK_OF_YEAR);
								
								weeks.remove(x);
								while(week1 <= week2)
								{
									weeks.add(weeks.size(), String.valueOf(week1));
									week1++;
								}
								break;
							}
							else
							{
								Date date = df.parse(weeks.get(x));
								Calendar cal = Calendar.getInstance();
								cal.setTime(date);
								weeks.set(x, String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)));
							}

						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					classOn = new Class(time, day, Arrays.asList(classGroups), module, weeks);
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
