import java.util.ArrayList;

class Group {
    private ArrayList<User> users;
    Group() {
        this.users = new ArrayList<>();
    }
    void addUser(User user) {
        this.users.add(user);
    }
    ArrayList<User> getUsers() {
        return users;
    }
}
