
import java.util.List;

public class Room {
	
	private String roomHeaderData;
	private List<Class> classes;
	
	public Room(String roomHeaderData)
	{
		this.roomHeaderData = roomHeaderData;
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
