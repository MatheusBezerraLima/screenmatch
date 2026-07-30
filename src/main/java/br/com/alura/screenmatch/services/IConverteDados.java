package br.com.alura.screenmatch.services;

public interface IConverteDados {
    //  Como varias classes podem ser usadas de modelo eu uso o generics
    <T> T obterDados(String json, Class<T> classe);
}
