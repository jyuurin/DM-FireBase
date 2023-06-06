package com.example.listacompras

//classe de negocio, classe de modelo
//no room é entidade
//retrofit é o que converte o json
data class Produto(
    var id: String? = null,
    var nome: String = "",
    var comprado: Boolean = false
)
