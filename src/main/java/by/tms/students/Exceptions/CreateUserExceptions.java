package by.tms.students.Exceptions;

public class CreateUserExceptions extends Exception{
    private String name;

    public CreateUserExceptions(String name) {
        this.name = name;
    }

    public CreateUserExceptions() {
    }

    @Override
    public String getMessage() {
        return "Ошибка при создании пользователя " + name;
    }
}
