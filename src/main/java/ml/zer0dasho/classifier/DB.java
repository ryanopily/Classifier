package ml.zer0dasho.classifier;

public class DB {

	
	public static String insertCourse(Course course) {
		return 
			"INSERT INTO classes (class, department, course, title, section, type, days, time, building, room, campus, instructor, status) " 
		  + "VALUES " + course.databaseFormat();
	}
}
