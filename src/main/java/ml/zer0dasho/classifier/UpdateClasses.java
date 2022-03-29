package ml.zer0dasho.classifier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import ml.zer0dasho.classifier.Scraper.Semester;
import ml.zer0dasho.classifier.Scraper.StudentType;

public class UpdateClasses {

	public static void main(String...args) throws InterruptedException {
		ThreadPoolExecutor netExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
		List<String> links = (Scraper.getDepartmentPageLinks(new Semester[] {Semester.SPRING}, new StudentType[] {StudentType.UNDERGRADUATE, StudentType.GRADUATE}));
		
		int numLinks = links.size();
		AtomicInteger downloads = new AtomicInteger(1);
		
		List<Future<?>> tasks = new ArrayList<>();
		Queue<List<String>> rawCoursesQ = new ConcurrentLinkedQueue<>();

		System.out.println("DOWNLOADING!");
		for(int i = 0; i < links.size(); i++) {
			String link = links.get(i);
				
			tasks.add(netExecutor.submit(() -> {
				List<List<String>> rawCourses = Scraper.getDepartmentCoursesRaw(link);
				rawCoursesQ.addAll(rawCourses);
				System.out.println("Downloaded link #" + downloads.getAndIncrement() + "/" + numLinks);
			}));
		}
			
		while(tasks.size() > 0) {
			Iterator<Future<?>> it = tasks.iterator();
			
			while(it.hasNext()) {
				Future<?> future = it.next();
				
				if(future.isDone())
					it.remove();
			}
		}
			
		int numCourses = rawCoursesQ.size();
		AtomicInteger processed = new AtomicInteger(1);
		
		Queue<Course> coursesQ = new ConcurrentLinkedQueue<>();
		
		System.out.println("PROCESSING!");
		while(rawCoursesQ.peek() != null) {
			List<String> rawCourses = rawCoursesQ.poll();
			
			tasks.add(netExecutor.submit(() -> {
				coursesQ.add(new Course(rawCourses));
				processed.getAndIncrement();
				
				int num = processed.get();
				if(num % 500 == 0)
					System.out.println("Processed course #" + num + "/" + numCourses);
			}));
		}
		
		while(tasks.size() > 0) {
			Iterator<Future<?>> it = tasks.iterator();
			
			while(it.hasNext()) {
				Future<?> future = it.next();
				
				if(future.isDone())
					it.remove();
			}
		}
		
		String url = "jdbc:mysql://localhost:3306/Classifier";
		String username = "classifier";
		String password = "password";
		
		System.out.println("INSERTING!");
		try(Connection conn = DriverManager.getConnection(url, username, password)) {
			
			Statement statement = conn.createStatement();
			conn.setAutoCommit(false);
			
			int size = coursesQ.size();
			for(int i = 0; coursesQ.peek() != null; i++) {
				
				Course course = coursesQ.poll();
				statement.addBatch(DB.insertCourse(course));
				
				if(i > 0 && i % 999 == 0) {
					System.out.println("Inserted entry #" + i+1 + "/" + size);
					statement.executeBatch();
					conn.commit();
				}
			}
			
			statement.executeBatch();
			conn.commit();
			statement.close();
			conn.close();
			System.out.println("FINISHED!");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
			

	}
	
}
