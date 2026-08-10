package br.com.alura.screenmatch.service;

public interface IConverteDados {
    //  Como varias classes podem ser usadas de modelo eu uso o generics
    <T> T obterDados(String json, Class<T> classe);
}
