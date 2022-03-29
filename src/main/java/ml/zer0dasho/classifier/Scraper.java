package ml.zer0dasho.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Scraper {
	
	private static final String CLASS_SCHEDULE  = "https://www.buffalo.edu/class-schedule?semester=%s&division=%d";
	
	public static enum Semester {
		SPRING, SUMMER, FALL, WINTER;
	}
	
	public static enum StudentType {
		UNDERGRADUATE, GRADUATE;
	}
	
	public static List<String> getDepartmentPageLinks(Semester[] semesters, StudentType[] types) {
		List<String> result = new ArrayList<>();
		
		for(Semester semester : semesters) {
			for(StudentType type : types) {
				String link = String.format(CLASS_SCHEDULE, semester.toString(), type.ordinal() + 1);
				result.addAll(getDepartmentPageLinks(link));
				System.out.println(result);
			}
		}
		
		return result;
	}
	
	private static List<String> getDepartmentPageLinks(String classScheduleLink) {
		String departmentPage = new String(Requests.Try(() -> Requests.get(classScheduleLink).getEntity().getContent().readAllBytes()));
		Pattern pattern = Pattern.compile("http://www\\.buffalo\\.edu/class-schedule\\?switch=showcourses[^\"]+");
		Matcher matcher = pattern.matcher(departmentPage);

		List<String> result = new ArrayList<String>();
		
		while(matcher.find())
			result.add(matcher.group());
		
		return result;
	}
	
	public static List<Course> getDepartmentCourses(String departmentPageLink) {
		return getDepartmentCoursesRaw(departmentPageLink).stream().map((list) -> new Course(list)).collect(Collectors.toList());
	}

	public static List<List<String>> getDepartmentCoursesRaw(String departmentPageLink) {
		String departmentPage = Requests.Try(() -> Requests.readAllBytes(Requests.get(departmentPageLink).getEntity().getContent()));
		// Gets rid of unnecessary whitespace
		departmentPage = departmentPage.replaceAll("\\n|  ", "");
		
		// Find course information
		Pattern pattern = Pattern.compile("<td class=\"(padding|open|closed|arrows)\"( nowrap=\"nowrap\")?>(.*?)</td>");
		Matcher matcher = pattern.matcher(departmentPage);
		
		List<List<String>> result = new ArrayList<>();
		List<String> course_builder = new ArrayList<>();

		while(matcher.find()) {
			String match = matcher.group(3);
			course_builder.add(match);
			
			if(course_builder.size() == 11) {
				result.add(new ArrayList<>(course_builder));
				course_builder.clear();
			}
		}

		return result;
	}
	
	public static String[] getCourseDetails(String coursePageLink) {
		String coursePage = Requests.Try(() -> Requests.readAllBytes(Requests.get(coursePageLink).getEntity().getContent()));
		// Gets rid of unnecessary whitespace
		coursePage = coursePage.replaceAll("\\n", "").replaceAll("[ ]{2,}", " ");
		
		// Find course information
		Pattern pattern = Pattern.compile("<td class=\"info\"><br/>(.+?(?=<\\/td>))");
		Matcher matcher = pattern.matcher(coursePage);
		
		if(matcher.find()) {
			// Gets rid of unnecessary whitespace
			String result = matcher.group(1)
								   .replaceAll("&nbsp;", "")
								   .replaceAll("[ ]{2,}", " ")
								   .replaceAll("TBA[^,]", "TBA,")
								   .trim();
			
			String[] fields = result.split(",|<br />");
			
			return fields;
		}
		
		return null;
	}
	
	public static List<String> getChainedClasses(String coursePageLink) {
		String page = Requests.Try(() -> new String(Requests.get(coursePageLink).getEntity().getContent().readAllBytes()));
		page = page.replaceAll("\\n", "").replaceAll("[ ]{2,}", " ");
		page = page.replaceAll(".*?<ul(.*?)</ul>.*", "$1");
		
		Pattern pattern = Pattern.compile("regnum=(\\d+)");
		Matcher matcher = pattern.matcher(page);
		
		List<String> result = new ArrayList<>();
		
		while(matcher.find()) {
			result.add(matcher.group(1));
		}
		
		return result;
	}
}