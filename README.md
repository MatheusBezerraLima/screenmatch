# ScreenMatch API
 
ScreenMatch é uma API REST desenvolvida em Spring Boot que centraliza dados de séries de TV consumidos da OMDb API, traduz automaticamente as sinopses para português e disponibiliza tudo através de endpoints REST, prontos para serem consumidos por um front-end ou qualquer outro cliente HTTP.
 
O projeto nasceu como uma aplicação de linha de comando (usando `CommandLineRunner`) durante a formação Avançando com Java, da Alura, e evoluiu para expor os mesmos dados através de uma API REST persistida em PostgreSQL, mantendo o menu original de terminal disponível como forma alternativa de popular e consultar o banco.
 
## 🔻Funcionalidades
 
- Busca de séries e episódios na OMDb API e persistência dos dados no banco
- Tradução automática das sinopses (inglês para português) via MyMemory Translation API
- Endpoints REST para listagem geral, lançamentos recentes, top séries, detalhes por ID, temporadas/episódios e filtro por categoria
- Configuração de CORS para consumo por um front-end local
## Endpoints
 
| Método | Rota | Descrição |
|---|---|---|
| GET | `/series` | Lista todas as séries cadastradas |
| GET | `/series/top5` | Lista as séries com melhor avaliação, ordenadas por nota |
| GET | `/series/lancamentos` | Lista as séries com episódios lançados mais recentemente |
| GET | `/series/{id}` | Retorna os dados detalhados de uma série |
| GET | `/series/{id}/temporadas/todas` | Lista todos os episódios de uma série, agrupados por temporada |
| GET | `/series/{id}/temporadas/{numero}` | Lista os episódios de uma temporada específica |
| GET | `/series/categoria/{nomeGenero}` | Filtra séries por categoria (ação, romance, comédia, drama, crime) |
 
## 🔻Arquitetura
 
O projeto segue uma separação clara de camadas:
 
- `models`: entidades JPA (`Serie`, `Episodio`) e o enum `Categoria`, além dos records que mapeiam a resposta da OMDb API (`DadosSerie`, `DadosTemporada`, `DadosEpisodio`)
- `dto`: DTOs (`SerieDTO`, `EpisodioDTO`) que isolam o modelo de persistência do que é exposto pela API
- `repositories`: `SerieRepository`, com consultas derivadas e JPQL para os diferentes filtros — top séries, lançamentos mais recentes, busca por categoria, por temporada, por trecho de episódio, entre outros
- `service`: `SerieService` concentra a regra de negócio da API REST; `ConsumoAPI` é o cliente HTTP nativo usado para consumir a OMDb; `ConverteDados` cuida do parsing do JSON com Jackson; e o pacote `service.traducao` integra a MyMemory Translation API
- `controller`: `SerieController`, expondo os endpoints REST descritos acima
- `principal`: `Principal`, o menu original de linha de comando, mantido como forma alternativa de popular e consultar o banco diretamente pelo terminal
- `config`: configuração de CORS liberando o consumo pelo front-end local
## 🔻 Tecnologias
 
- Java 17
- Spring Boot, Spring Web e Spring Data JPA
- PostgreSQL
- Jackson, para o parsing de JSON
- Java HttpClient nativo, para o consumo da OMDb API
- OMDb API, como fonte dos dados de séries e episódios
- MyMemory Translation API, para a tradução automática das sinopses
## 🔻 Como executar
 
A aplicação espera as seguintes variáveis de ambiente para a conexão com o banco:
 
```
DB_HOST=localhost:5432
DB_NAME=screenmatch
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
```
 
Com o banco disponível, basta rodar a classe `ScreenmatchApplication` (ou `./mvnw spring-boot:run`). A API sobe em `http://localhost:8080`.
 
Para popular a base, é possível usar o menu de linha de comando (classe `Principal`, disponível comentada em `ScreenmatchApplicationSemWeb`) ou inserir os dados diretamente no banco.
 
## 🔻Aprendizados
 
- Transformar uma aplicação de terminal em uma API REST, mantendo a lógica de negócio na camada de serviço e reaproveitando o mesmo modelo de dados
- Separar DTOs de entidades JPA para não expor a estrutura de persistência diretamente na API
- Modelar o relacionamento `@OneToMany`/`@ManyToOne` entre `Serie` e `Episodio`, com cascata e fetch eager
- Escrever consultas JPQL customizadas no repositório, incluindo ordenação, agregações com `MAX` e filtros por data e por trecho de texto
- Integrar duas APIs externas dentro do mesmo fluxo de persistência — a OMDb para os dados e a MyMemory para a tradução das sinopses
- Configurar CORS no Spring para liberar o consumo da API por um front-end rodando em outra origem
