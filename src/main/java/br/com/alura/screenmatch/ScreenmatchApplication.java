package br.com.alura.screenmatch;

import br.com.alura.screenmatch.models.DadosSerie;
import br.com.alura.screenmatch.services.ConsumoAPI;
import br.com.alura.screenmatch.services.ConverteDados;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchApplication  implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	// Se torna o método main já que vai ser chamado na linha 11 como define o contrato da interface CommandLineRunner
	@Override
	public void run(String... args) throws Exception {
		var consumoAPI = new ConsumoAPI();
		var dataRequest = consumoAPI.obterDados("https://www.omdbapi.com/?t=gilmore+girls&Season=1&apiKey=cf87569f");

		ConverteDados conversor = new ConverteDados();

		DadosSerie dataConvertido = conversor.obterDados(dataRequest, DadosSerie.class);

		System.out.println(dataConvertido);
	}
}
