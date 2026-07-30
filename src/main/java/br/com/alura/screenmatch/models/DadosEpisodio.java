package br.com.alura.screenmatch.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosEpisodio(
        @JsonAlias("Title") String titulo,
        @JsonAlias("Episode") Integer episodio,
        @JsonAlias("imdbRating") String avalicao,
        @JsonAlias("Released") String dataLancamento
) {
}
