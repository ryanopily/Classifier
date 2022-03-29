package ml.zer0dasho.classifier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateStatistics {

	public static void main(String[] args) {
		String url = "jdbc:mysql://localhost:3306/Classifier";
		String username = "classifier";
		String password = "password";

		try(Connection conn = DriverManager.getConnection(url, username, password)) {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			ResultSet r = s.executeQuery("SELECT DISTINCT REPLACE(time, ' - ', '-') AS slot, COUNT(REPLACE(time, ' - ', '-')) AS count FROM classes GROUP BY slot ORDER BY slot");
			
			SimpleDateFormat displayFormat = new SimpleDateFormat("HHmm");
		    SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");

		    Statement insert = conn.createStatement();
		    
			while(r.next()) {
				String time = r.getString("slot");
				int count = r.getInt("count");
				

				try {
					String[] range = time.split("-");
					Date start = parseFormat.parse(range[0]);
					Date end = parseFormat.parse(range[1]);
					
					String json = String.format("{\"time\": \"%s\", \"start\": \"%s\", \"end\": \"%s\", \"count\": %d}", time, displayFormat.format(start), displayFormat.format(end), count);
					insert.addBatch(String.format("INSERT INTO statistics (type, value) VALUES (%d, '%s')", 0, json));
				} catch(Exception ex) {
					continue;
				}
			}
			
			insert.executeBatch();
			conn.commit();
			
		} catch(SQLException ex) {

			ex.printStackTrace();
		}
	}

}
