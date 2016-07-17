package com.example.hp.thetacab;

/**
 * Created by gul on 6/23/16.
 */
public class Facebook {
    public String Email;
    public String Birthday;
    public String Name;
    public String Gender;
    public String type;
    public Facebook(){

    }
    public Facebook(String Email,String Birthday,String Name,String Gender){
        this.Email=Email;
        this.Birthday=Birthday;
        this.Name=Name;
        this.Gender=Gender;
        type="facebook";
    }
}
