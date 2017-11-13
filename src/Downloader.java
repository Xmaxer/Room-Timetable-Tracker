import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

	private static Document roomsPage = downloadPage("room_timetables.html");
	private static Document timetablesPage = downloadPage("Testing.html");

	public static void redownloadData() {

		createRoomDB();

		List<Room> rooms = getAllRoomsData();

		try
		{


			String classTable = "class";
			String roomTable = "room";

			DBConnection.getStatement().execute("DROP TABLE IF EXISTS " + classTable);

			DBConnection.getStatement().executeUpdate("CREATE TABLE IF NOT EXISTS class" + 
					"(time time NOT NULL," + 
					"day VARCHAR(10) NOT NULL," + 
					"module VARCHAR(20) NOT NULL," + 
					"week_number int NOT NULL," + 
					"room_number VARCHAR(20) NOT NULL," + 
					"class_group VARCHAR(255) NOT NULL," + 
					"length int," + 
					"PRIMARY KEY (time, day, week_number, class_group, module, room_number)," + 
					"FOREIGN KEY (room_number) REFERENCES room (room_number) ON DELETE RESTRICT ON UPDATE CASCADE" + 
					");");

			String finalInsert = "INSERT INTO " + classTable + " (time, day, module, week_number, room_number, class_group, length) values ";
			for(int roomIndex = 0; roomIndex < rooms.size(); roomIndex++)
			{

				String roomNumber = rooms.get(roomIndex).getRoomNumber();
				int roomCapacity = rooms.get(roomIndex).getRoomCapacity();

				//Adding the capacity of the room
				PreparedStatement ps = DBConnection.getConnection().prepareStatement("UPDATE " + roomTable + 
						" SET capacity='" + roomCapacity + "'" + 
						" WHERE room_number='" + roomNumber + "'");

				ps.executeUpdate();

				for(Class currentClass : rooms.get(roomIndex).getClasses())
				{
					String time = currentClass.getTime();
					String day = currentClass.getDay();
					List<String> classGroups = currentClass.getClassGroups();
					String module = currentClass.getModule();
					List<String> weeks = currentClass.getWeeks();
					int length = currentClass.getLength();

					if(weeks != null && classGroups != null)
					{
						for(String week : weeks)
						{
							for(String classGroup : classGroups)
							{
								finalInsert += "(" + 
										"'" + time + "'," + 
										"'" + day + "'," + 
										"'" + module + "'," + 
										"'" + week + "'," + 
										"'" + roomNumber + "'," + 
										"'" + classGroup + "'," + 
										"'" + length + "'),";
							}
						}
					}
				}
			}

			System.out.println("Completed building string...");
			char[] chars = finalInsert.toCharArray();

			chars[finalInsert.length() - 1] = Character.MIN_VALUE;

			finalInsert = new String(chars);

			DBConnection.getStatement().executeUpdate(finalInsert);

		}catch(Exception e)
		{
			DBConnection.closeConnection();
			e.printStackTrace();
		}

		System.out.println("Finished downloading");
	}

	private static List<Room> getAllRoomsData() {


		List<Room> rooms = new ArrayList<Room>();

		Document doc = downloadPage("Testing.html");

		if(doc != null)
		{

			Elements tables = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(2) > tbody > tr");

			Elements tableInfo = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(1) > tbody > tr > td > table > tbody > tr > td:first-child");

			for(int j = 1; j < tables.size(); j+=57)
			{
				String room = tableInfo.get(j/57).text();

				Room currentRoom = new Room(room);

				List<Class> classes = getAllClassesForRoom(j, tables);

				currentRoom.setClasses(classes);
				rooms.add(currentRoom);
			}
		}

		return rooms;
	}

	private static List<Class> getAllClassesForRoom(int j, Elements tables) {


		List<Class> classes = new ArrayList<Class>();

		for(int trip = j; trip < (j+56); trip++)
		{
			classes.addAll(getAllClassesForAllTimes(j, tables, trip));
		}

		return classes;
	}

	private static List<Class> getAllClassesForAllTimes(int j, Elements tables, int trip) {

		List<String> days = configureValidDaysFromTable();

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
				String[] classGroups = null;
				String module = null;
				List<String> weeks = new ArrayList<String>();
				int length = 0;
				Class classOn = new Class(time, day, null, null, null, 0);

				if(specificTimeData.get(i).hasAttr("rowspan"))
				{
					Elements classDataTables = specificTimeData.get(i).select("table");
					try
					{
						length = Integer.valueOf(specificTimeData.get(i).attr("rowspan"))/4;
					} catch (NumberFormatException f)
					{
					}
					classGroups = classDataTables.get(0).select("tbody > tr > td").text().split(",");
					module = classDataTables.get(1).select("tbody > tr > td").text();
					weeks = new LinkedList<String>(Arrays.asList(classDataTables.get(2).select("tbody > tr > td").text().split(",")));

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
				}

				classOn = new Class(time, day, (classGroups == null) ? null :Arrays.asList(classGroups), module, weeks, length);
				classes.add(classOn);
			}
		}

		return classes;
	}

	private static Document downloadPage(String link) {

		Document doc = null;
		try {
			File input = new File(link);
			doc = Jsoup.parse(input, "UTF-8");

		} catch (IOException e) {
			e.printStackTrace();
		}

		return doc;
	}
	private static List<String> configureValidDaysFromTable() {

		Elements daysTableData = timetablesPage.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(2) > tbody > tr:first-child > td");

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

	private static void createRoomDB()
	{

		String roomTable = "room";
		try {
			DBConnection.getStatement().execute("DROP TABLE IF EXISTS " + roomTable);
			DBConnection.getStatement().executeUpdate("CREATE TABLE IF NOT EXISTS room" + 
					"(room_number VARCHAR(255) PRIMARY KEY," + 
					"capacity int" + 
					");");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Elements options = roomsPage.select("select[size=\"6\"] > option");
		Long current = System.currentTimeMillis();
		String finalInsert = "INSERT INTO " + roomTable + " (room_number) VALUES ";
		for(Element option : options)
		{
			if(!option.text().contains("#"))
			{

				finalInsert += "(" +
						"'" + option.text() + "'),";

			}
		}
		char[] chars = finalInsert.toCharArray();

		chars[finalInsert.length() - 1] = Character.MIN_VALUE;

		finalInsert = new String(chars);
		try {
			DBConnection.getStatement().executeUpdate(finalInsert);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Finished making room table." + ((System.currentTimeMillis() - current)/1000) + " seconds");
	}
}
