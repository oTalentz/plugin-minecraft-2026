# ✨ Espírito de Luz (Plugin Minecraft 2026)

Um plugin criativo e altamente otimizado para servidores Minecraft 1.21+ que introduz o efeito de **Luz Dinâmica** através de espíritos mágicos.

## 🌟 O que o plugin faz?
Ele adiciona um item especial que, ao ser ativado, invoca um espírito mágico invisível (um ArmorStand com partículas) que segue o jogador. Durante 3 minutos, o jogador recebe o efeito de Visão Noturna (*Night Vision*), e o espírito orbita ao seu redor, criando uma atmosfera imersiva e funcional.

## 💡 Por que Visão Noturna e não blocos de luz?
O Minecraft Vanilla não suporta nativamente "Luz Dinâmica" emitida por entidades. Colocar e quebrar blocos de luz reais a cada movimento do jogador causa **alto consumo de recursos no servidor** e problemas de renderização no cliente (atualizações constantes de chunks). 
Utilizar o efeito de `Night Vision` contínuo, combinado com partículas orbitais, entrega a experiência de iluminação total em cavernas de forma leve, limpa e completamente livre de *lag*.

## 🎮 Como Usar
1. Possua a permissão de administração (`luzdinamica.admin` ou seja OP).
2. Execute o comando `/luz` (ou `/espirito`) para receber o item "Espírito de Luz".
3. Segure o item e clique com o botão direito no ar.
4. O espírito começará a orbitar ao seu redor, emitindo partículas e garantindo visão no escuro!

## 🛠️ Como Compilar
Se você tiver o Maven instalado, abra o terminal na raiz do projeto e execute:
```bash
mvn clean package
```
O arquivo `.jar` do plugin será gerado na pasta `target/`. Basta movê-lo para a pasta `plugins/` do seu servidor Paper/Spigot.

## ⚙️ Permissões
- `luzdinamica.admin`: Permite usar o comando para receber o item de teste.
- `luzdinamica.use`: (Opcional) Permite que jogadores comuns utilizem o item, caso seja distribuído por outros meios (lojas, eventos, recompensas de mineração).

---
*Desenvolvido para oferecer a melhor experiência de iluminação sem comprometer a performance do servidor.*