# Servidor Paper 1.21.8

Configuracao pronta para rodar um servidor **Paper 1.21.8** com suporte a multiplas versoes (ViaVersion/ViaBackwards) e os plugins customizados **Coins** e **Tags**.

## Requisitos

- Docker + Docker Compose
- Java 21 (para compilar os plugins)

## Iniciar

```bash
./start.sh
```

O script compila os plugins (`Coins` e `Tags`), baixa os plugins externos (ViaVersion, ViaBackwards, LuckPerms, Vault, playit-gg) e sobe o container.

## Comandos uteis

```bash
# Ver logs
docker compose -f server/docker-compose.yml logs -f minecraft

# Parar
docker compose -f server/docker-compose.yml down

# Acessar console do servidor (precisa do RCON ativado ou attach)
docker attach minecraft-paper-1.21.8
```

## Plugins incluidos

- **Coins**: economia com a moeda `Coins`. Comandos: `/coins`, `/pay`, `/coins <set|add|remove> <jogador> <quantia>`.
- **Tags**: define tags para jogadores. Tags: `Recruta` (padrao), `Moderador`, `Admin`, `Dono`. Comando: `/tag <jogador> <tag>`.

## Integracoes

- **Vault**: a economia `Coins` registra um provider `Economy` do Vault para outros plugins.
- **LuckPerms**: o plugin `Tags` cria os grupos e define permissoes automaticamente para cada tag.
- **ViaVersion + ViaBackwards**: permite que jogadores em versoes diferentes da 1.21.8 entrem no servidor.
- **playit-gg**: tenta gerar um tunel publico para o servidor. Apos iniciar, o console exibe um link `https://playit.gg/claim/...`; acesse-o para obter o endereco publico. Em algumas redes o playit nao consegue conectar no servidor de controle (UDP), entao o `start.sh` tambem inicia um tunel `bore` como fallback.
- **bore**: tunel TCP publico via `bore.digital` (sem conta). Endereco publico padrao: `bore.digital:55001`.
- **Console web**: painel em Flask (porta 5000) para ver logs, executar comandos, dar/remover OP, kick e reiniciar/parar o servidor. O `start.sh` ja sobe a console localmente.

## Estrutura

- `server/docker-compose.yml` - orquestracao do container
- `server/data/` - dados do servidor (mundo, configs, plugins baixados)
- `server/plugins/` - plugins customizados gerados pelo `build.sh`
