package com.example.listacompras

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.listacompras.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    //tem que ativar no tools realtime database pra usar
    lateinit var dataBase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tratarLogin()

        binding.fab.setOnClickListener{
            novoItem()
        }
    }

    fun tratarLogin(){
        //só precisa fazer se é necessário autenticar ou não, se for necessário, entra no if :D
        if(FirebaseAuth.getInstance().currentUser == null){
            //primeiro mecanismo de autenticação configurado
            //pra colocar mais é só separar por vírgula :D
            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
            val intent = AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            startActivityForResult(intent, 1)
        }
        else{
            configDataBase()
        }
    }

    //callback de atividade, quando recebe a resposta é executada.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Caminho feliz, usuario tem conta. :D
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Autenticado", Toast.LENGTH_LONG).show()
            configDataBase()
        }
        else {
            finishAffinity()
        }
    }

    fun configDataBase(){
        //? e let verificam se algo é nulo :D, its a null check
        FirebaseAuth.getInstance().currentUser?.let {
            //it é o currentUser, pois ele pega oq vem antes do let
            dataBase = FirebaseDatabase.getInstance().reference.child(it.uid)
        }
    }

    fun novoItem(){
        val editText = EditText(this)
        editText.hint = "Nome do Item"

        //carrega as coisas
        AlertDialog.Builder(this)
            .setTitle("Novo Item")
            .setView(editText)
            .setPositiveButton("Inserir", null)
            .create()
            .show()
    }
}