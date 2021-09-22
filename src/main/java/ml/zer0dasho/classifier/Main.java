package ml.zer0dasho.classifier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class Main {
	
	public static void main(String...args) throws SQLException, ClassNotFoundException {
		
		String url = "jdbc:mysql://localhost:3306/db_name";
		String username = "db_user";
		String password = "db_password";

		// System.out.println("Connecting database...");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {		
			
			List<String> links = Scraper.getDepartmentCourseLinks();
						 links.addAll(Scraper.getDepartmentCourseLinks2());

			for(String link : links) {
				for(Course course : Scraper.getCourses(link)) {
					String stmt = DB.insertCourse(course);
					connection.prepareStatement(stmt).execute();
				}
			}
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		
	}
}