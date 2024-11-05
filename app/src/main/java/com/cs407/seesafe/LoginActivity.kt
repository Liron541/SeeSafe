package com.cs407.seesafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 在这里添加任何登录页面的初始化代码
        // 获取 signupButton 并设置点击事件跳转到 SignUpActivity
        val signupButton: Button = findViewById(R.id.signUpButton)
        signupButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
