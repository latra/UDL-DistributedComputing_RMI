/* ---------------------------------------------------------------
Práctica 1.
Código fuente: Group.java
Grau Informàtica
47129784J - Paula Gallucci Zurita
--------------------------------------------------------------- */
import java.util.ArrayList;

class Group {
    private String name;
    private ArrayList<User> users;

    Group(String name) {
        this.name = name;
        this.users = new ArrayList<>();
    }

    void addUser(User user) {
        this.users.add(user);
    }

    void removeUser(User user) {
        this.users.remove(user);
    }

    boolean containsUser(User user) {
        return this.users.contains(user);
    }

    ArrayList<User> getUsers() {
        return users;
    }
}
