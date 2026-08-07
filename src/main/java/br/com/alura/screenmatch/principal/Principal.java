package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.models.*;
import br.com.alura.screenmatch.repositories.SerieRepository;
import br.com.alura.screenmatch.services.ConsumoAPI;
import br.com.alura.screenmatch.services.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private final String BASE_URL = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apiKey=cf87569f";
    private  ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repositoio) {
        this.repositorio = repositoio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar série por ator
                    6 - Top 5 séries
                    7 - Buscar série por categoria
                    8 - Buscar série por quantidade de temporadas
                    9 - Buscar episodio por trecho
                    10 - Buscar top episodios por série
                    11 - Buscar episodios a partir de uma data

                    
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    buscarSeriePorQuantiadeDeTemporadas();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTop5EpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }


    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumoAPI.obterDados(BASE_URL + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serie.isPresent()) {
            var serieEcontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEcontrada.getTotalTemporadas(); i++) {
                var json = consumoAPI.obterDados(BASE_URL + serieEcontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEcontrada.setEpisodios(episodios);

            repositorio.save(serieEcontrada);
        } else {
            System.out.println("Serie não encontrada!");
        }
    }

    private void listarSeriesBuscadas(){
        this.series = repositorio.findAll();

        this.series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBuscada.isPresent()){
            System.out.println("Dados da série: " + serieBuscada.get());
        }else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Escolha um ator para consultar suas séries: ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de que valor? ");
        var avaliacao = leitura.nextDouble();

        List<Serie> serieBuscada = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);

        if(!serieBuscada.isEmpty()){
            System.out.println("Séries em que " + nomeAtor + " trabalhou: ");
            serieBuscada.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
        }else {
            System.out.println("Nenhuma serie registrada com esse autor!");
        }

    }

    private void buscarTop5Series(){
        List<Serie> topSeries = repositorio.findTop5ByOrderByAvaliacaoDesc();
        topSeries.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriePorCategoria(){
        System.out.println("Escolha uma categoria para consultar séries: ");
        var categoriaEscolhida = leitura.nextLine();

        Categoria categoria = Categoria.fromPortugues(categoriaEscolhida);

        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Séries da categoria " + categoriaEscolhida + ": ");
        seriesPorCategoria.forEach(s -> System.out.println(s.getTitulo() + " categoria: " + s.getGenero()));

    }

    private void buscarSeriePorQuantiadeDeTemporadas() {
        System.out.println("Qual o máximo de temporadas? ");
        var maxTemporadas = leitura.nextInt();

        List<Serie> seriesEncontradas = repositorio.seriesPorTemporadaEAvaliacao(maxTemporadas);
        System.out.println("Séries com até " + maxTemporadas + " temporadas: ");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " Quantidade de Temporadas: " + s.getTotalTemporadas()));

    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio para busca?  ");
        var trechoEpisodio = leitura.nextLine();

        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach( e ->
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo())
        );
    }

    private void buscarTop5EpisodiosPorSerie(){
        buscarSeriePorTitulo();
        if(serieBuscada.isPresent()){
           Serie serie = serieBuscada.get();
           List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
           topEpisodios.forEach( e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s - avaliação: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao())
           );
        }
    }

    private void buscarEpisodiosDepoisDeUmaData() {
        buscarSeriePorTitulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            System.out.println("Digite a partir de que ano foi o lançamento: ");
            var anoLancamento = leitura.nextInt();

            List<Episodio> episodiosEncontrados = repositorio.episodioPorSerieEAno(serie, anoLancamento);
            episodiosEncontrados.forEach( e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s - Lançamento: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getDataLancamento())
            );
        }
    }
}
