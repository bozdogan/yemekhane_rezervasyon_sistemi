package org.bozdgn.model;

public class User {
    String pid;
    String firstname;
    String lastname;
    boolean isAdmin;

    public User(
            String pid,
            String firstname,
            String lastname,
            boolean isAdmin
    ) {
        this.isAdmin = isAdmin;
        this.pid = pid;
        this.firstname = firstname;
        this.lastname = lastname;
    }


    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
