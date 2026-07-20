# 🎮 Servidor Minecraft 1.21.8 — Multi-Versão

Servidor Minecraft baseado em **Paper 1.21.8** com suporte a múltiplas versões de cliente (1.8 até 1.21.8+) usando **ViaVersion**, **ViaBackwards** e **ViaRewind**.

## 📋 Requisitos

- **Java 21** (JRE ou JDK)
- **4 GB+ de RAM** recomendados
- Conexão com internet (para download inicial)

## 🚀 Início Rápido

### Linux / macOS

```bash
# 1. Dar permissão de execução
chmod +x scripts/setup.sh scripts/start.sh

# 2. Baixar e configurar tudo
./scripts/setup.sh

# 3. Iniciar o servidor
./scripts/start.sh
```

### Windows

```bat
:: 1. Baixar e configurar tudo
scripts\setup.bat

:: 2. Iniciar o servidor
scripts\start.bat
```

### Docker

```bash
# Build e start
docker compose up -d

# Ver logs
docker compose logs -f minecraft

# Parar
docker compose down
```

## 🌐 Versões Suportadas

| Versão do Cliente | Suporte |
|---|---|
| 1.21.8 (nativa) | ✅ Completo |
| 1.21.x | ✅ ViaBackwards |
| 1.20.x | ✅ ViaBackwards |
| 1.19.x | ✅ ViaBackwards |
| 1.18.x | ✅ ViaBackwards |
| 1.17.x | ✅ ViaBackwards |
| 1.16.x | ✅ ViaBackwards |
| 1.15.x | ✅ ViaBackwards |
| 1.14.x | ✅ ViaBackwards |
| 1.13.x | ✅ ViaBackwards |
| 1.12.x | ✅ ViaBackwards |
| 1.11.x | ✅ ViaBackwards |
| 1.10.x | ✅ ViaBackwards |
| 1.9.x | ✅ ViaBackwards |
| 1.8.x | ✅ ViaRewind |
| 1.7.x | ✅ ViaRewind |

## 📁 Estrutura do Projeto

```
.
├── README.md
├── server.properties
├── eula.txt
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── scripts/
│   ├── setup.sh
│   ├── start.sh
│   ├── setup.bat
│   └── start.bat
└── plugins/
    ├── ViaVersion/
    │   └── config.yml
    └── ViaBackwards/
        └── config.yml
```

## ⚙️ Configuração

### server.properties
Edite o arquivo `server.properties` na raiz para ajustar:
- `server-port` — Porta do servidor (padrão: 25565)
- `max-players` — Máximo de jogadores
- `motd` — Mensagem do dia
- `online-mode` — Autenticação Mojang (true/false)
- `view-distance` — Distância de renderização

### Plugins
- **ViaVersion**: `plugins/ViaVersion/config.yml`
- **ViaBackwards**: `plugins/ViaBackwards/config.yml`

## 🔧 Comandos Úteis

| Comando | Descrição |
|---|---|
| `/viaversion` | Informações do ViaVersion |
| `/viaversion reload` | Recarregar config do ViaVersion |
| `/viabackwards` | Informações do ViaBackwards |
| `/viarewind` | Informações do ViaRewind |

## 📝 Notas

- O arquivo `paper-1.21.8.jar` é baixado automaticamente pelo script de setup e **não** é versionado no Git.
- Mundos, logs e caches também são ignorados pelo `.gitignore`.
- Para atualizar o Paper, basta re-executar o script de setup.

## 📜 Licença

Este projeto é fornecido como está. O Minecraft é propriedade da Mojang Studios / Microsoft.
