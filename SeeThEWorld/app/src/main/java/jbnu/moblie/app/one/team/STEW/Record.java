package jbnu.moblie.app.one.team.STEW;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Record extends Activity {

    int num=0;
    String text;
    String name;

    private Button copyButton;
    private Button backButton;
    private EditText editText;
    private ImageView imageView;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record);
        User user = new User();

        Intent intent = getIntent();
        num=intent.getIntExtra("num",0);
        name=user.getName();
        System.out.println(name);

        copyButton=findViewById(R.id.button_copy);
        backButton=findViewById(R.id.button_back);
        editText=findViewById(R.id.edit_text_view);
        imageView=findViewById(R.id.image_view);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("User").child(name);

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("recognised text",editText.getText());
                clipboard.setPrimaryClip(clip);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                finish();
            }
        });

        storageRef.child(name+"/"+num+".png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Glide.with(Record.this)
                            .load(task.getResult())
                            .fitCenter()
                            .into(imageView);

                } else {
                    // URL을 가져오지 못하면 토스트 메세지
                    Toast.makeText(Record.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        databaseReference.child("text_recognise").child(String.valueOf(num)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text=dataSnapshot.getValue().toString();
                editText.setText(text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });

    }
}
