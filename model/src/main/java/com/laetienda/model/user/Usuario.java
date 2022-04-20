package com.laetienda.model.user;

import com.laetienda.lib.annotation.HtmlForm;
import com.laetienda.lib.annotation.HtmlInput;
import com.laetienda.lib.interfaces.Form;
import com.laetienda.lib.options.HtmlInputType;

@HtmlForm(name="usuario", url = "/signup.html")
public class Usuario implements Form {

    @HtmlInput(label = "Username", placeholder = "Place the username", style_size="col-md-12")
    private String username;    //userid, uid

    @HtmlInput(label = "First Name", placeholder = "Insert your first name", style_size="col-md-4")
    private String firstname;

    @HtmlInput(label = "Middle Name", placeholder = "Insert your middle name", required = false, style_size="col-md-4")
    private String middlename;

    @HtmlInput(label = "Last Name", placeholder = "Please insert your last name", style_size="col-md-4")
    private String lastname;

    @HtmlInput(label = "eMail address", placeholder = "Please insert your email address", style_size="col-md-12")
    private String email;

    @HtmlInput(type= HtmlInputType.PASSWORD, label = "Password", placeholder = "Please insert your password", style_size="col-md-6")
    private String password;

    @HtmlInput(type= HtmlInputType.PASSWORD, label = "Re-enter password", placeholder = "Please confirm your password", style_size="col-md-6")
    private String password2;

    public Usuario() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }
}
