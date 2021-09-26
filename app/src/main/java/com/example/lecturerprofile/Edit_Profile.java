package com.example.lecturerprofile;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class Edit_Profile extends AppCompatActivity {

   EditText etfName,etlName,etSubject,etMobile,etEmail;
   Button button;
   ImageView imageView;
   ProgressBar progressBar;
   Uri imageUri;
   UploadTask uploadTask;
   StorageReference storageReference;
   FirebaseDatabase database = FirebaseDatabase.getInstance();
   DatabaseReference databaseReference;
   FirebaseFirestore db = FirebaseFirestore.getInstance();
   DocumentReference documentReference;
   private static final int PICK_IMAGE = -1;
   All_Lecturers lecturers;
   String currentUserId;

   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.edit_profile);

      lecturers = new All_Lecturers();
      imageView = findViewById(R.id.iv_cp);
      etfName = findViewById(R.id.et_firstname_cp);
      etlName = findViewById(R.id.et_lastname_cp);
      etSubject = findViewById(R.id.et_subject_cp);
      etMobile = findViewById(R.id.et_mobile_cp);
      etEmail = findViewById(R.id.et_email_cp);

      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      currentUserId = user.getUid();

      documentReference = db.collection("user").document(currentUserId);
      storageReference = FirebaseStorage.getInstance().getReference("Profile images");
      databaseReference =database.getReference("All Users");

      button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            uploadData();
         }

         private void uploadData() {
         }
      });

      imageView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivity(intent);
         }
      });


   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      try{

      }catch (Exception e){
         Toast.makeText(this, "Error"+e, Toast.LENGTH_SHORT).show();
      }

   }
   private String getFileExt(Uri uri){
      ContentResolver contentResolver = getContentResolver();
      MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
      return  mimeTypeMap.getExtensionFromMimeType((contentResolver.getType(uri)));

   }

   private void uploadData(){

      String First_Name = etfName.getText().toString();
      String Last_Name = etlName.getText().toString();
      String Subject = etSubject.getText().toString();
      String Mobile = etMobile.getText().toString();
      String Email = etEmail.getText().toString();

      if(!TextUtils.isEmpty(First_Name) || !TextUtils.isEmpty(Last_Name) || !TextUtils.isEmpty(Subject)
              || !TextUtils.isEmpty(Mobile) || !TextUtils.isEmpty(Email) || imageUri != null){

         progressBar.setVisibility(View.VISIBLE);
         final StorageReference reference = storageReference.child(System.currentTimeMillis()+ "." +getFileExt(imageUri));
         uploadTask = reference.putFile(imageUri);

         Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
               if (!task.isSuccessful()) {
                  throw task.getException();

               }

               return reference.getDownloadUrl();
            }
         }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

               if(task.isSuccessful()){
                  Uri downloadUri = task.getResult();

                  Map<String,String> profile = new HashMap<>();
                  profile.put("First Name" , First_Name);
                  profile.put("Last Name" , Last_Name);
                  profile.put("url",downloadUri.toString());
                  profile.put("Subject",Subject);
                  profile.put("Mobile",Mobile);
                  profile.put("E-mail", Email);
                  profile.put("privacy","Public");

                  lecturers.setEtfName(First_Name);
                  lecturers.setEtSubject(Subject);
                  lecturers.setEtEmail(Email);

                  databaseReference.child(currentUserId).setValue(lecturers);
                  documentReference.set(profile)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                                 progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(Edit_Profile.this, "Profile Updated", Toast.LENGTH_SHORT).show();

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                   @Override
                                   public void run() {
                                        Intent intent = new Intent (Edit_Profile.this,LecturerProfile.class);
                                        startActivity(intent);
                                   }
                                },2000);
                             }
                          });
               }
            }
         });
      }else{
         Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
      }

   }

}
