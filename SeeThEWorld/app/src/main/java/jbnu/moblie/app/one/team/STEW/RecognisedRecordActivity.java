package jbnu.moblie.app.one.team.STEW;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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


public class RecognisedRecordActivity extends Activity {

    private Button backButton;
    private ImageButton imageButtonTest;
    private TextView textView;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognised_record);

        Intent intent = getIntent();
        String name=intent.getStringExtra("user");

        textView=findViewById(R.id.text_view);
        backButton=findViewById(R.id.button_goto_record);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("User").child(name);
        databaseReference.child("text_recognise").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count=(int)dataSnapshot.getChildrenCount();
                textView.setText(count+" Record");
                for(int i=1;i<=count;i++){
                    final LinearLayout linear = (LinearLayout) findViewById(R.id.linear_root);

                    // linearLayout params 정의

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 350);

                    // LinearLayout 생성

                    // 버튼 생성
                    final ImageButton imageButton = new ImageButton(RecognisedRecordActivity.this);
                    // setId 버튼에 대한 키값
                    imageButton.setId(i);
                    imageButton.setLayoutParams(params);

                    final int finalI = i;
                    imageButton.setOnClickListener(new View.OnClickListener() {
                        final int num= finalI;
                        public void onClick(View v) {
                            System.out.println(num);
                            Intent intent = new Intent(RecognisedRecordActivity.this,Record.class);
                            intent.putExtra("num", finalI);
                            startActivity(intent);
                        }
                    });

                    //버튼 add
                    linear.addView(imageButton);
                    //LinearLayout 정의된거 add

                    storageRef.child("images/"+i+".png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Glide.with(RecognisedRecordActivity.this)
                                        .load(task.getResult())
                                        .fitCenter()
                                        .centerCrop()
                                        .into(imageButton);

                            } else {
                                // URL을 가져오지 못하면 토스트 메세지
                                Toast.makeText(RecognisedRecordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
