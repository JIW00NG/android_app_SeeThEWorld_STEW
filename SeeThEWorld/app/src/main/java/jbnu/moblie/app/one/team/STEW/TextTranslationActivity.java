package jbnu.moblie.app.one.team.STEW;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.NoCopySpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class TextTranslationActivity extends Activity {
    private String sourceText;
    private String targetText;
    private Button mTranslateButton;
    private Button mCopyResourceButton;
    private Button mCopytargetButton;

    private TextView resourceTextView;
    private TextView targetTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_translation);
        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            sourceText = intent.getExtras().getString("text");
        }

        else {
            sourceText = "";
        }



        resourceTextView = findViewById(R.id.textView_resource);
        targetTextView = findViewById(R.id.textView_target);
        mTranslateButton = findViewById(R.id.button_translate);
        mCopyResourceButton = findViewById(R.id.button_copy_resource);
        mCopytargetButton = findViewById(R.id.button_copy_target);

        resourceTextView.setText(sourceText);
        mCopyResourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("resource text",resourceTextView.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(TextTranslationActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        mCopytargetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("target text",targetTextView.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(TextTranslationActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        // setup translation
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.KO)
                        .build();
        final FirebaseTranslator englishKoreanTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);


        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        englishKoreanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

        // do translation
        mTranslateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceText = resourceTextView.getText().toString();
                englishKoreanTranslator.translate(sourceText)
                        .addOnSuccessListener(
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(@NonNull String translatedText) {
                                        targetTextView.setText(translatedText);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });


            }
        });
    }
}
