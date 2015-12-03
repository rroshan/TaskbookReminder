import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map.Entry;
import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.*;

public class ReminderSQL {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/taskbook";

	//  Database credentials
	static final String USER = "roshan";
	static final String PASS = "Sql@1234";


	public HashMap<String, Task> getRecentDueTasks() {
		//map key email,tasklist_id
		//value task object
		HashMap<String, Task> map = new HashMap<String, Task>();
		Task task;

		// getting database connection from connection pool
		// connection handled by tomcat
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet set;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			String sql = "select tasks.* from tasks, reminders where tasks.task_id = reminders.task_id AND tasks.status = \"N\" AND reminders.active = \"Y\" AND extract(hour from timediff(reminders.date_time, now())) < 1";
			pstmt = conn.prepareStatement(sql);
			set = pstmt.executeQuery();

			while (set.next()) {
				// Retrieve by column name
				int taskId = set.getInt("task_id");
				Timestamp createdDate = set.getTimestamp("created_date");
				Timestamp lastModifiedDate = set.getTimestamp("last_modified_date");
				String assignedUser = set.getString("assigned_user");
				String status = set.getString("status");
				String scope = set.getString("scope");
				String title = set.getString("title");
				Timestamp dueDate = set.getTimestamp("due_date");

				task = new Task();
				task.setTaskId(taskId);
				task.setCreatedDate(createdDate);
				task.setLastModifiedDate(lastModifiedDate);
				task.setDueDate(dueDate);
				task.setAssignedUser(assignedUser);
				task.setScope(scope);
				task.setStatus(status);
				task.setTitle(title);

				map.put(task.getAssignedUser()+","+set.getInt("tasklist_id"), task);
				
				//update reminders active flag to N
				sql = "update reminders set active=\"N\" where task_id=? and active=\"Y\"";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, task.getTaskId());
				pstmt.executeUpdate();
			}
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

		}

		return map;
	}

	public static void main(String[] args) {
		ReminderSQL reminderSQL = new ReminderSQL();
		HashMap<String, Task> map = reminderSQL.getRecentDueTasks();
		int tasklistId;
		String emailId;
		String csvKey;
		String[] spiltCsvKey;
		Task task;
		
		for(Entry<String, Task> entry : map.entrySet()) {
			csvKey = entry.getKey();
			spiltCsvKey = csvKey.split(",");
			task = entry.getValue();
			
			emailId = spiltCsvKey[0];
			tasklistId = Integer.parseInt(spiltCsvKey[1]);

			//sending request mail
			String link = "http://localhost:8080/Taskbook/subtask";
			String subject = "Reminder for task "+task.getTitle();

			StringTemplateGroup group =  new StringTemplateGroup("sendReminder", "/Users/roshan/Documents/UTD/Fall 2015/OOAD/Project/Template", DefaultTemplateLexer.class);
			StringTemplate sendRequestTemplate = group.getInstanceOf("send_reminder");

			link = link + "?tasklistId="+tasklistId+"&taskId="+task.getTaskId();

			sendRequestTemplate.setAttribute("title", "Reminder");
			sendRequestTemplate.setAttribute("task", task.getTitle());
			sendRequestTemplate.setAttribute("due_date", task.getDueDate());
			sendRequestTemplate.setAttribute("link", link);

			SendMail sendMail = new SendMail(subject, sendRequestTemplate.toString(), "jakx24@gmail.com", emailId);
			sendMail.send();

		}
	}

}
