# MiningWisps ✦ (Plugin Minecraft 2026)

Um plugin criativo para servidores Minecraft 1.21+ que adiciona os **Espíritos de Luz**!
Este é o repositório para o plugin Minecraft 2026.

## O que o plugin faz?
Ele adiciona um item especial que, ao ser consumido, invoca um espírito mágico invisível (ArmorStand com partículas) que segue o jogador pelas cavernas. Durante 3 minutos, o jogador ganha visão no escuro e o espírito orbita em volta dele.

### Por que Night Vision e não blocos de luz?
O Minecraft Vanilla não suporta "Luz Dinâmica" vinda de entidades de forma natural. Colocar e quebrar blocos de luz reais a cada movimento do jogador causa **muito lag no servidor** e problemas de renderização no cliente (Chunk Updates).
Usar o efeito de `Night Vision` contínuo + as partículas em volta do jogador entrega a experiência de 100% de visão em cavernas de forma leve, limpa e completamente livre de bugs.

## Como Usar
1. Tenha permissão de admin (`miningwisps.admin` ou OP).
2. Digite `/wisp` para receber o item "Espírito de Luz" (Pó de Pedra Luminosa mágico).
3. Segure o item e clique com o botão direito no ar.
4. O espírito começará a orbitar ao seu redor emitindo partículas mágicas!

## Como Compilar
Se você tiver o Maven instalado, abra o terminal na pasta do projeto e rode:
`mvn clean package`

O arquivo `.jar` do plugin será gerado na pasta `target/`. Coloque-o na pasta `plugins/` do seu servidor!