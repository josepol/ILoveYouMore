package app.josepol.com.iloveyoumore.ViewModels;

/**
 * Created by polsa on 05/09/2017.
 */

public class UserViewModel {

    private String userName;
    private String userLastName;

    public UserViewModel() {

    }

    public UserViewModel(String user, String userLastName) {
        this.userName = userName;
        this.userLastName = userLastName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }
}
