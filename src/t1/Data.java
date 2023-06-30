package t1;
import java.sql.Connection;
import java.sql.DriverManager;

public class Data {

	public static void main(String[] args) {
		Connection con = null;
		try {
			con = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/biblioteca", "root", "admin");
			if (con!=null) {
				System.out.println("database is connected");
			}
		} catch (Exception e) {
			System.out.println("not connected.");
		}
	}

}
