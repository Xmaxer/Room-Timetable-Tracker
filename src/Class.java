
import java.util.List;

public class Class {
	
	private String time;
	private String day;
	private List<String> classGroups;
	private String module;
	private List<String> weeks;
	
	public Class(String time, String day, List<String> classGroups, String module, List<String> weeks)
	{
		this.time = time;
		this.day = day;
		this.classGroups = classGroups;
		this.module = module;
		this.weeks = weeks;
	}

	public String toString()
	{
		return "'" + module + "' is on for '" + classGroups + "' at " + time + " on " + day;
	}
	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @return the day
	 */
	public String getDay() {
		return day;
	}

	/**
	 * @return the classGroups
	 */
	public List<String> getClassGroups() {
		return classGroups;
	}

	/**
	 * @return the module
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @return the weeks
	 */
	public List<String> getWeeks() {
		return weeks;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(String day) {
		this.day = day;
	}

	/**
	 * @param classGroups the classGroups to set
	 */
	public void setClassGroups(List<String> classGroups) {
		this.classGroups = classGroups;
	}

	/**
	 * @param module the module to set
	 */
	public void setModule(String module) {
		this.module = module;
	}

	/**
	 * @param weeks the weeks to set
	 */
	public void setWeeks(List<String> weeks) {
		this.weeks = weeks;
	}
}
