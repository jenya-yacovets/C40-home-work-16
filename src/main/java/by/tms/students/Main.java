package by.tms.students;

import by.tms.students.Exceptions.CreateUserExceptions;
import by.tms.students.db.MysqlConnection;
import by.tms.students.db.MysqlUtil;
import by.tms.students.entity.Student;
import by.tms.students.entity.Students;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        Students students = null;

        try {
            JAXBContext context = JAXBContext.newInstance(Students.class, Student.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            students = (Students) unmarshaller.unmarshal(new File("students.xml"));
            System.out.println("+ Список пользователь успешно прочтен из файла");
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        if(students != null) {

            try {
                Connection connection = MysqlConnection.getConnection();

                // Добавляем студентов в БД
                for(Student student : students.getStudents()) {
                    try {
                        MysqlUtil.addStudent(student, connection);
                    } catch (CreateUserExceptions e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("+ Пользователи успешно добавлены в БД");

                // Делаем запросы
                Statement statement = connection.createStatement();

                // Список групп и сколько в них студентов
                ResultSet result1 = statement.executeQuery("SELECT g.name AS `group`, COUNT(s.id) AS `count` FROM `groups` g JOIN `students` s ON g.id = s.group_id GROUP BY g.id;");
                System.out.println("\nСписок групп и сколько в них студентов:");
                while (result1.next()){
                    String group = result1.getString("group");
                    int count = result1.getInt("count");
                    System.out.format("В группе %s учится %d студентов\n", group, count);
                }

                // Студент с возрастом до 21 года
                ResultSet result2 = statement.executeQuery("SELECT name, age FROM `students` WHERE age < 21;");
                System.out.println("\nСтудент с возрастом до 21 года:");
                while (result2.next()){
                    String name = result2.getString("name");
                    int age = result2.getInt("age");
                    System.out.format("%s - %d\n", name, age);
                }

                // Студенты с группой отсортированые по возрасту
                // DESC что бы отсортровать по убыванию
                // ASC что бы отсортровать по возростанию - по умолчанию
                ResultSet result3 = statement.executeQuery("SELECT s.name, s.age, g.name AS `group` FROM `students` s JOIN `groups` g ON s.group_id = g.id ORDER BY s.age ASC;");
                System.out.println("\nСтуденты с группой отсортированые по возрасту");
                while (result3.next()){
                    System.out.format("%s (%d лет) - %s\n", result3.getString("name"), result3.getInt("age"), result3.getString("group"));
                }

                // Список студентов с номером группы, средним балом и отсортироваными по среднему балу
                ResultSet result4 = statement.executeQuery("SELECT s.name, g.name AS `group`, AVG(m.mark) AS marks FROM `students` s JOIN `groups` g ON s.group_id = g.id JOIN `marks` m ON s.id = m.student_id GROUP BY s.name, g.name ORDER BY marks;");
                System.out.println("\nСписок студентов с номером группы, средним балом и отсортироваными по среднему балу:");
                while (result4.next()){
                    System.out.format("%s (%s) - средний бал = %f\n", result4.getString("name"), result4.getString("group"), result4.getFloat("marks"));
                }

                // Перевод студента Вася из группы С42 в С40
                int countUpdate = statement.executeUpdate("UPDATE `students` SET group_id = ( SELECT id FROM `groups` WHERE name='С40' LIMIT 1) WHERE name='Вася';");
                System.out.println("\nПеревод студента Вася из группы С42 в С40:");
                System.out.format("Успешно обновлена %d запись\n", countUpdate);

                // Группы и средние балы студентов из этих групп
                ResultSet result5 = statement.executeQuery("SELECT g.name, AVG(m.mark) AS mark FROM `groups` g JOIN `students` s ON g.id = s.group_id JOIN `marks` m ON s.id = m.student_id GROUP BY g.name;");
                System.out.println("\nГруппы и средние балы студентов из этих групп:");
                while (result5.next()){
                    System.out.format("%s - %f\n", result5.getString("name"), result5.getFloat("mark"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
