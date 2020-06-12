// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package jbnu.moblie.app.one.team.STEW;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TextRecogniserActivity extends AppCompatActivity {
    private ImageView mImageView;

    private Button mTextButton;
    private Button logoutButton;
    private Button mCopyButton;
    private Button galleryButton;
    private Button recordButton;
    private Button translationViewButton;
    private Bitmap mSelectedImage;
    private EditText mEditText;

    private MyReceiver BR;
    private Uri filePath;

    private final int GET_GALLERY_IMAGE = 200;

    long childrenCount =0;
    private String text="";
    User user = new User();
    Uri imageUri;
    String imageString;
    String filePathString;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("User").child(user.getName());


    /**
     * Number of results to show in the UI.
     */
    private static final int RESULTS_TO_SHOW = 3;
    /**
     * Dimensions of inputs.
     */

    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float>
                                o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognise);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("autologin",MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        setReceiver();       // BroadReceiver on

        mImageView = findViewById(R.id.image_view);
        mEditText = findViewById(R.id.edit_text);
        mTextButton = findViewById(R.id.button_text);
        logoutButton = findViewById(R.id.button_logout);
        mCopyButton = findViewById(R.id.button_copy);
        recordButton = findViewById(R.id.button_goto_record);
        galleryButton = findViewById(R.id.button_gallery);
        translationViewButton = findViewById(R.id.button_translationView);
        translationViewButton.setEnabled(false);
        mSelectedImage = ((BitmapDrawable)getResources().getDrawable(R.drawable.photo)).getBitmap();

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TextRecogniserActivity.this,RecognisedRecordActivity.class);
                intent.putExtra("user",user.getName());
                startActivity(intent);
            }
        });

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("recognised text",mEditText.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(TextRecogniserActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        mTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                Intent intent = new Intent(TextRecogniserActivity.this,Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                editor.putBoolean("autologin",false);
                editor.commit();
                Toast.makeText(TextRecogniserActivity.this, "Log Out", Toast.LENGTH_SHORT).show();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mEditText.setText("");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageView = findViewById(R.id.image_view);

        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            imageUri=data.getData();
            imageString = imageUri.toString();

            mImageView.setImageURI(selectedImageUri);
            filePath = selectedImageUri;
            filePathString = filePath.toString();
            try {
                mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        mTextButton.setEnabled(false);
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(Text texts) {
                                mTextButton.setEnabled(true);
                                final String recognisedText = processTextRecognitionResult(texts);
                                mEditText.setText(recognisedText);
                                translationViewButton.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(TextRecogniserActivity.this, TextTranslationActivity.class);
                                        intent.putExtra("text", mEditText.getText().toString());
                                        startActivity(intent);
                                    }
                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mTextButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String processTextRecognitionResult(Text texts) {

        //String text="";
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return null;
        }
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            //<----------------------------------------------------------------텍스트를 text변수로 저장
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    if(text==""){
                        text=text.concat(elements.get(k).getText()).concat(" ");
                    }else{
                        text=text.concat(elements.get(k).getText()).concat(" ");
                    }
                    System.out.println(text.substring(text.length()));
                }
                text=text.substring(0,text.length()-1).concat("\n");
            }
        }

        databaseReference.child("text_recognise").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                childrenCount = (int)dataSnapshot.getChildrenCount();
                databaseReference.child("text_recognise").child(String.valueOf(childrenCount+1)).setValue(text);
                databaseReference.child("count").setValue(childrenCount+1);
                Intent serviceIntent = new Intent(TextRecogniserActivity.this,UploadToStorage.class);
                serviceIntent.putExtra("image",imageString);
                serviceIntent.putExtra("filePath",filePathString);
                serviceIntent.putExtra("count",childrenCount);
                startService(serviceIntent);
                createNotification();

                text="";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                text="";
            }

        });
        translationViewButton.setEnabled(true);
        return text;
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void setReceiver() {

        BR = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_CAMERA_BUTTON);
        filter.addAction(Intent.ACTION_TIME_TICK);

        registerReceiver(BR, filter);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BR);
    }

    private void createNotification() {
        Intent intent = new Intent(this, Record.class);
        intent.putExtra("num", (int)childrenCount+1);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("OCR 파일 업로드완료");
        builder.setContentText(String.valueOf(childrenCount+1) + "th file 바로가기");
        builder.setAutoCancel(true);

        NotificationCompat.BigPictureStyle pictureStyle = new NotificationCompat.BigPictureStyle(builder);
        pictureStyle.bigPicture(mSelectedImage);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        notificationManager.notify(1, builder.build());
    }
    private void removeNotification() {

        // Notification 제거
        NotificationManagerCompat.from(this).cancel(1);
    }

}