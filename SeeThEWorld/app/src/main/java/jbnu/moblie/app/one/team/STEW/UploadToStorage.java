package jbnu.moblie.app.one.team.STEW;

import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class UploadToStorage extends IntentService {

    long count;
    Bitmap bitmapImage;
    Uri filePath;
    Uri imageUri;
    User user = new User();

    public UploadToStorage() {
        super("UploadToStorage");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        filePath=Uri.parse(intent.getStringExtra("filePath"));
        imageUri= Uri.parse(intent.getStringExtra("image"));

        try {
            bitmapImage= MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        count=intent.getLongExtra("count",0);

        //업로드할 파일이 있으면 수행
        if (bitmapImage != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            String filename = Long.toString(count+1) + ".png";

            //storage 주소와 폴더 파일명을 지정
            StorageReference storageRef = storage.getReferenceFromUrl("gs://moblie-app-one-team-stew.appspot.com").child(user.getName()+"/" + filename);

            storageRef.putFile(filePath)
                    //성공
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "업로드 성공!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //실패
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }
}
