package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.models.DadosEpisodio;
import br.com.alura.screenmatch.models.DadosSerie;
import br.com.alura.screenmatch.models.DadosTemporada;
import br.com.alura.screenmatch.models.Episodio;
import br.com.alura.screenmatch.services.ConsumoAPI;
import br.com.alura.screenmatch.services.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private final String URI = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apiKey=cf87569f";
    private  ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();


    public void exibeMenu () {

        System.out.println("Digite o nome da Série: ");

        var nomeSerie = this.leitura.nextLine();
        var dataRequest = this.consumoAPI.obterDados(URI + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dadosSerie = conversor.obterDados(dataRequest, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();

		for(int i = 1; i <= dadosSerie.totalTemporadas(); i++){
			dataRequest = consumoAPI.obterDados(URI + nomeSerie.replace(" ", "+") + "&Season="+ i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(dataRequest, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));


        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        System.out.println("Top 5 Episódios: ");
        dadosEpisodios.stream()
                .filter(e -> !e.avalicao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avalicao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("A partir de que ano você deseja ver os episódios? ");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1,1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e ->e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        " Temporada: " + e.getTemporada() +
                        " Episódio: " + e.getTitulo() +
                        " Data de Lançamento: " + e.getDataLancamento().format(formatter)
                ));
    }
}
