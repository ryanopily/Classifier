package ml.zer0dasho.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Course {

	private String databaseFormat = null;
	private String coursePageLink = null;
	
	public Course(List<String> fields) {
		cleanFields(fields);
	}
	
	@Override
	public String toString() {
		return databaseFormat;
	}
	
	public String databaseFormat() {
		return databaseFormat;
	}
	
	/** 
	 Fields format: 
	 0 = regnum/class,
	 1 = department  ,
	 2 = course,
	 3 = title,
	 4 = section,
	 5 = type,
	 6 = days,
	 7 = time,
	 8 = building,
	 9 = room,
	 10 = campus,
	 11 = instructor,
	 12 = status
	  **/
	private void cleanFields(List<String> fields) {
		trim(fields);
		this.coursePageLink = fields.get(1).replaceAll(".*\"(.*)\".*", "$1");
		
		// Check if enrollment in this course is "chained"
		if(fields.get(0).length() == 0) {
			String classes = Scraper.getChainedClasses(coursePageLink).toString();
			fields.set(0, classes.substring(1, classes.length()-1));
		}
		
		// Split 'Course' into Department and Course Number
		String[] temp = fields.get(1).replaceAll(".*>(.*)<.*","$1").split(" ");
		fields.set(1, temp[0]); // Department
		fields.add(2, temp[1]); // Course number
		fields.add(9, "?"); // Room number (we will update later)

		// Class meets in different rooms on different days and at different times - requires us to insert multiple records for the same class
		if(fields.get(7).matches("Multiple meeting patterns.") || fields.get(8).matches("See class detail")) {
			multipleMeetingPatterns(fields);
			return;
		}
			
		// Split 'Room' into Building and Room NUmber
		if (fields.get(8).matches(".+ .+")) {
			temp = fields.get(8).split(" ");
			fields.set(8, temp[0]); // Building
			fields.set(9, temp[1]); // Room number
		} else fields.set(9, fields.get(8));
		
		fields.set(7, fields.get(7).replaceAll(" - ", "-"));
		
		quote(fields);
		databaseFormat = "(" + String.join(", ", fields) + ")";
	}
	
	private void trim(List<String> fields) {
		// Trim whitespace and clean up encodings
		for(int i = 0; i < fields.size(); i++) {
			fields.set(i, fields.get(i).trim()
					.replaceAll("\\&amp;", "&")
					.replaceAll("\\&[^;]+;", ""));
		}
	}
	
	private void quote(List<String> fields) {
		for(int i = 0; i < fields.size(); i++) {
			fields.set(i, "\"" + fields.get(i) + "\"");
		}
	}
	
	public void multipleMeetingPatterns(List<String> fields) {
		
		// Details are all the multiple meeting times - (days, time, building, room) fields
		List<String> details = new ArrayList<String>();
		details.addAll(Arrays.asList(Scraper.getCourseDetails(coursePageLink)));
		
		trim(details);
		quote(details);
		quote(fields);
		
		StringBuilder format = new StringBuilder();
		
		while(details.size() > 0) {
			// Details should always come in 4, if not, we should fill in the blanks
			while(details.size() % 4 != 0)
				details.add("?");
			
			fields.set(6, details.get(0));
			fields.set(7, details.get(1));
				
			if(details.get(2).matches(".+ .+")) {
				String temp = details.get(2);
				int index = temp.lastIndexOf(' ');
				
				fields.set(8, temp.substring(0, index) + "\"");
				fields.set(9, "\"" + temp.substring(index+1, temp.length()));
			} else {
				fields.set(8, details.get(2));
				fields.set(9, details.get(2));
			}
			
			fields.set(10, details.get(3));
			
			for(int i = 0; i < 4; i++)
				details.remove(0);

			format.append("(" + String.join(", ", fields) + ")" + ",");
		}
		
		format.deleteCharAt(format.length()-1);
		databaseFormat = format.toString();
	}
}
