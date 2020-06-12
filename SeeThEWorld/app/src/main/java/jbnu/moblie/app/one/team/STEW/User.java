package jbnu.moblie.app.one.team.STEW;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    private  String name;
    private String email;

    FirebaseUser user = FirebaseAuth.getInstance(). getCurrentUser();

    public User(){
        if (user != null) {
            // Name, email address, and profile photo Url
            email = user.getEmail();
        }
        name = email.substring(0,email.indexOf("@"));
    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("User");


    public void userRegist(String username){

        databaseReference.child(name).child("user_name").setValue(name);
        databaseReference.child(name).child("count").setValue(0);
    }

    public String getName(){
        return name;
    }
}
