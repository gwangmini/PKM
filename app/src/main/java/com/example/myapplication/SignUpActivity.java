package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG="SignUpActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_signup).setOnClickListener(onClickListener);
        findViewById(R.id.btn_gotologin).setOnClickListener(onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.btn_signup:
                    singUp();
                    break;
                case R.id.btn_gotologin:
                    mystartActivity(LoginActivity.class);
                    break;
            }

        }
    };

    private void singUp(){

        String email=((EditText)findViewById(R.id.reg_email)).getText().toString();
        String password=((EditText)findViewById(R.id.reg_pswd)).getText().toString();
        String passwordcheck=((EditText)findViewById(R.id.reg_pswd_check)).getText().toString();


        if(email.length()>0&&password.length()>0&&passwordcheck.length()>0){
            if(password.equals(passwordcheck)){
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    startToast( "회원가입에 성공하였습니다.");
                                    mystartActivity(LoginActivity.class);

                                } else {
                                    if(task.getException()!=null) {
                                        // If sign in fails, display a message to the user.
                                        startToast(task.getException().toString());
                                    }


                                }
                            }
                        });
            } else {
                startToast("비밀번호가 일치하지 않습니다.");

            }

        }else{
            startToast("이메일 또는 비밀번호를 입력해 주세요.");
        }



    }
    private void startToast(String msg){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
    }

    private void startLoginActivity(){
        Intent intent= new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    private void mystartActivity(Class c){
        Intent intent= new Intent(this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

