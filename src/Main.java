import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	public static void main(String[] args) throws IOException {
		File input = new File("Testing.html");
		Document doc = Jsoup.parse(input, "UTF-8");

		Elements tables = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(2) > tbody > tr");

		Elements daysTableData = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(2) > tbody > tr:first-child > td");

		Elements tableInfo = doc.select("table > tbody:nth-child(1) > tr:nth-child(6) > td:first-child > table:nth-child(1) > tbody > tr > td > table > tbody > tr > td:first-child");
		List<String> days = new ArrayList<String>();

		for(Element dayTableData : daysTableData)
		{
			if(!dayTableData.text().isEmpty() && !days.contains(dayTableData.text()))
			{
				days.add(dayTableData.text());
			}
		}

		List<Room> rooms = new ArrayList<Room>();

		for(int j = 1; j < tables.size(); j+=57)
		{
			String room = tableInfo.get(j/57).text();

			Room currentRoom = new Room(room);

			List<Class> classes = new ArrayList<Class>();

			for(int trip = j; trip < (j+56); trip++)
			{
				Elements specificTimeData = tables.get(trip).select("td");

				String time = null;

				List<String> classOnDay = new ArrayList<String>();
				List<String> classGroupOnDay = new ArrayList<String>();
				List<String> classScheduleOnDay = new ArrayList<String>();
				List<String> associatedDay = new ArrayList<String>();

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
	

							associatedDay.add(days.get(i - 1));
							classGroupOnDay.add(classDataTables.get(0).select("tbody > tr > td").text());
							classOnDay.add(classDataTables.get(1).select("tbody > tr > td").text());
							classScheduleOnDay.add(classDataTables.get(2).select("tbody > tr > td").text());

							classOn = new Class(time, day, Arrays.asList(classGroups), module, Arrays.asList(weeks));
						}

						classes.add(classOn);
					}
				}
			}

			currentRoom.setClasses(classes);
			rooms.add(currentRoom);
		}

		for(Class currentClass : rooms.get(0).getClasses())
		{
			String time = currentClass.getTime();
			String day = currentClass.getDay();
			List<String> classGroups = currentClass.getClassGroups();
			String module = currentClass.getModule();
			List<String> weeks = currentClass.getWeeks();
			
			if(classGroups == null && module == null && weeks == null)
			{
				continue;
			}
			
			System.out.println("Groups " + classGroups + " have " + module + " for the weeks " + weeks + " starting at " + time + " on " + day);
		}
		
		

	}

}
