
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Room {

	private String roomHeaderData;
	private List<Class> classes;
	private String roomNumber;
	private int roomCapacity;

	public Room(String roomHeaderData)
	{
		this.roomHeaderData = roomHeaderData;

		parseRoomHeaderData();
	}

	private void parseRoomHeaderData() {

		Matcher roomFinder = Pattern.compile("Room: ([^ (]*)([^)]*)").matcher(roomHeaderData);

		if(roomFinder.find() && roomFinder.groupCount() == 2)
		{
			this.roomNumber = roomFinder.group(1);
			try {
				this.roomCapacity = Integer.parseInt(roomFinder.group(2).replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", ""));
			}catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
			return;
		}
		else
		{
			Matcher roomFindAux = Pattern.compile("Room\\: (.*)NOTE").matcher(roomHeaderData);

			if(roomFindAux.find() && roomFindAux.groupCount() >= 1)
			{
				this.roomNumber = roomFindAux.group(1);
			}
			else
			{
				this.roomNumber = null;
			}
		}

		this.roomCapacity = 0;
	}

	/**
	 * @return the roomHeaderData
	 */
	public String getRoomHeaderData() {
		return roomHeaderData;
	}

	/**
	 * @return the classes
	 */
	public List<Class> getClasses() {
		return classes;
	}

	/**
	 * @return the roomNumber
	 */
	public String getRoomNumber() {
		return roomNumber;
	}

	/**
	 * @return the roomCapacity
	 */
	public int getRoomCapacity() {
		return roomCapacity;
	}

	/**
	 * @param roomNumber the roomNumber to set
	 */
	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	/**
	 * @param roomCapacity the roomCapacity to set
	 */
	public void setRoomCapacity(int roomCapacity) {
		this.roomCapacity = roomCapacity;
	}

	/**
	 * @param roomHeaderData the roomHeaderData to set
	 */
	public void setRoomHeaderData(String roomHeaderData) {
		this.roomHeaderData = roomHeaderData;
	}

	/**
	 * @param classes the classes to set
	 */
	public void setClasses(List<Class> classes) {
		this.classes = classes;
	}
}
