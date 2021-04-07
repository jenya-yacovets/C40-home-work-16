package by.tms.students.db;

import by.tms.students.Exceptions.CreateUserExceptions;
import by.tms.students.entity.Student;

import java.sql.*;

public class MysqlUtil {

    // Перегруженый метод можно использовать что бы не открывать и закрывать соеденение с базой при добавлении нового студента
    public static void addStudent(Student student) throws CreateUserExceptions {
        try(Connection connection = MysqlConnection.getConnection()) {
            addStudent(student, connection);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CreateUserExceptions(student.getName());
        }
    }

    public static void addStudent(Student student, Connection connection) throws CreateUserExceptions {
        try {
            connection.setAutoCommit(false);

            // Добавляем студента в базу
            PreparedStatement ps = connection.prepareStatement("INSERT INTO students (name, age, group_id) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, student.getName());
            ps.setInt(2, student.getAge());
            ps.setInt(3, findOrCreateGroup(student.getGroup()));
            ps.execute();

            ResultSet resultCreate = ps.getGeneratedKeys();
            if(!resultCreate.next()) throw new SQLException();
            int studentId = resultCreate.getInt(1);

            // Добавляем оценки студента в базу
            String sql = "";
            for (int i=0; i<student.getMarks().size(); i++){
                sql += " (?, ?)";
                if (i<student.getMarks().size()-1) sql += ",";
            }

            PreparedStatement psMarks = connection.prepareStatement("INSERT INTO marks (student_id, mark) VALUES" + sql);

            int nextIndex = 1;
            for (Integer mark : student.getMarks()){
                psMarks.setInt(nextIndex, studentId);
                nextIndex++;
                psMarks.setInt(nextIndex, mark);
                nextIndex++;
            }
            psMarks.execute();

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CreateUserExceptions(student.getName());
        }
    }

    private static int findOrCreateGroup(String group) {
        try(Connection connection = MysqlConnection.getConnection()) {
            // Пытаемся найти нужную группу
            PreparedStatement psFind = connection.prepareStatement("SELECT id FROM `groups` WHERE name=?;");
            psFind.setString(1, group);
            ResultSet resultFind = psFind.executeQuery();
            if (resultFind.next()) return resultFind.getInt("id");

            // Если нет создаем такую группу
            PreparedStatement psCreate = connection.prepareStatement("INSERT INTO `groups` (name) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            psCreate.setString(1, group);
            psCreate.execute();
            ResultSet resultCreate = psCreate.getGeneratedKeys();
            if(resultCreate.next()) return resultCreate.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
