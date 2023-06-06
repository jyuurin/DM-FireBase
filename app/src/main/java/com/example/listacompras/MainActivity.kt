package com.example.listacompras

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler.Value
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.listacompras.databinding.ActivityMainBinding
import com.example.listacompras.databinding.ItemBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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
            dataBase = FirebaseDatabase.getInstance().reference.child(it.uid) //reference ~e o no raiz, child é o filho da raiz.
            //no do usuario q pega la no firebase.

            var valueEventListener = object  : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // quando alterar vai rodar essa função. snapshot é tipo uma foto de td que tem no json do firebase.
                    //converte tudo la para uma lista de produtos. e essa função jogara tudo na tela
                    tratarDadosProdutos(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    // caso der erro roda essa. registra erro de que deu problema na conexão com o firebase
                    Log.w("MainActivity", "configDataBase", error.toException())
                    Toast.makeText(this@MainActivity, "Erro de conexão", Toast.LENGTH_LONG).show() //mensagenzinha pequena.
                }
            }
            dataBase.child("produtos").addValueEventListener(valueEventListener)
        }
    }

    fun novoItem(){
        val editText = EditText(this)
        editText.hint = "Nome do Item"

        //carrega as coisas
        AlertDialog.Builder(this)
            .setTitle("Novo Item")
            .setView(editText)
            .setPositiveButton("Inserir") { dialog, button -> // funcao seta
                //classe de modelo para facilitar o trabalho.
                val produto = Produto(nome = editText.text.toString())
                //jogando para dentro do firebase
                val novoNo = dataBase.child("produtos").push() //cria um novo nó
                produto.id = novoNo.key
                novoNo.setValue(produto)
            } //< funcao que vai ser executada ao clicar no botao, é um listener. um parametro que é uma função.
            .create()
            .show()
    }
    //foto do no que esta sendo monitorado. json que esta la no firebase.
    fun tratarDadosProdutos(dataSnapshot: DataSnapshot) {
        val listaProdutos = arrayListOf<Produto>()

        dataSnapshot.children.forEach {
            val produto = it.getValue(Produto::class.java) //< converte todos os produtos no json para produto

            produto?.let {
                listaProdutos.add(it)
            }
        }

        atualizarTela(listaProdutos)
    }

    fun atualizarTela(lista: List<Produto>) {
        //0 - limpar o container para nao duplicar os itens
        binding.container.removeAllViews()

        lista.forEach {
            //1 - infla o elemento que representa um item da lista
            val item = ItemBinding.inflate(layoutInflater)

            //2 - configura os atributos no elemento
            item.nome.text = it.nome
            item.comprado.isChecked = it.comprado

            item.excluir.setOnClickListener { view -> //sobrescreve o it
                it.id?.let {
                    val no = dataBase.child("produtos").child(it)
                    no.removeValue()
                }

            }

           item.comprado.setOnCheckedChangeListener { button, isChecked ->
               it.id?.let {
                   val no = dataBase.child("produtos").child(it)
                   no.child("comprado").setValue(isChecked)
               }
           }

            //3 - coloca o elemento dentro do container
            binding.container.addView(item.root)
        }
    }


}