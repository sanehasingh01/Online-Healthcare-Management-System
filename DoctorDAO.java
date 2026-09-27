import java.sql.*;

public class DoctorDAO {

    // ADD DOCTOR
    public boolean addDoctor(int userId,
                             String specialization,
                             String schedule) {

        String sql = "INSERT INTO doctors(user_id, specialization, schedule) VALUES (?, ?, ?)";

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setString(2, specialization);
            ps.setString(3, schedule);

            ps.executeUpdate();

            System.out.println("✅ Doctor added successfully!");

            con.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // VIEW DOCTORS
    public void viewDoctors() {

        String sql = "SELECT * FROM doctors";

        try {
            Connection con = DBConnection.getConnection();

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                System.out.println(
                    "Doctor ID: " + rs.getInt("doctor_id")
                );

                System.out.println(
                    "Specialization: " +
                    rs.getString("specialization")
                );

                System.out.println(
                    "Schedule: " +
                    rs.getString("schedule")
                );

                System.out.println("-------------------");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}