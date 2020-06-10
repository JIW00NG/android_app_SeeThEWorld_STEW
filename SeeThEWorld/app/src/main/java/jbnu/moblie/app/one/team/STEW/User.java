package jbnu.moblie.app.one.team.STEW;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    private String name,email;

    FirebaseUser user = FirebaseAuth.getInstance(). getCurrentUser();

    public User(){
        if (user != null) {
            // Name, email address, and profile photo Url
            email = user.getEmail();
        }
        name = email.substring(0,email.indexOf("@"));
    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("user");


    public void userRegist(String email,String username){

        databaseReference.child(name).child("easy_best").setValue(0);

        databaseReference.child(name).child("email").setValue(email);
        databaseReference.child(name).child("user_name").setValue(username);
    }

    public String getName(){
        return name;
    }
}
