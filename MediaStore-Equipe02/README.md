# MediaStore — Equipe 02

Projeto acadêmico de Desenvolvimento Mobile — Código de Alta Performance.

**Integrantes:** Matheus Rosas, Brendo Souza, Gustavo Figueiredo e Laércio Varela.

## Objetivo
Desafio 2 do Módulo 6 (página 19 da apostila): consultar o Content Provider nativo MediaStore.Images.Media.EXTERNAL_CONTENT_URI, usando Dispatchers.IO, listar nome e tamanho em KB no Logcat e limitar a consulta a dez resultados.

## Implementação
- Kotlin e interface XML; Android 8 ou superior.
- ContentResolver com projeção de três colunas (_ID, DISPLAY_NAME, SIZE).
- Paginação por ID, ordenação crescente e QUERY_ARG_LIMIT = 10; limite defensivo no Cursor.
- Cursor fechado com use; nenhuma imagem decodificada em bitmap.
- Corrotinas com lifecycleScope e withContext(Dispatchers.IO).
- Permissões de leitura conforme a versão do Android, incluindo acesso parcial no Android 14+.
- Listeners para consultar e navegar; resultados na interface e no Logcat (MEDIA_STORE).
- Conversão de bytes para KB usando 1024, conforme convenção binária.

## Executar
1. Instale Android Studio, SDK 35 e use JDK 17 (ou JDK compatível com AGP 8.7.3).
2. Este pacote não contém o binário do Gradle Wrapper. Gere-o com Gradle 8.9 instalado (`gradle wrapper --gradle-version 8.9`) na raiz. Alternativamente, crie um projeto Empty Views Activity no Android Studio e copie dele gradlew, gradlew.bat e gradle/wrapper/gradle-wrapper.jar para esta raiz, mantendo nosso gradle-wrapper.properties.
3. Abra a pasta MediaStore-Equipe02 no Android Studio (File > Open), sincronize o Gradle e aguarde o download das dependências.
4. Crie ou selecione um emulador Android (API 34 ou 35 recomendado) e pressione Run.
5. Adicione pelo menos 12 imagens fictícias à galeria (arraste arquivos JPG/PNG ao emulador; se necessário, mova-os para Pictures usando Files ou use a câmera do emulador).
6. Toque em Consultar galeria e conceda acesso a todas as fotos para testar a paginação.
7. Abra View > Tool Windows > Logcat e filtre por `tag:MEDIA_STORE`.

## Roteiro de testes (a executar)
| Cenário | Resultado esperado |
|---|---|
| Galeria com 12 imagens | Primeira página com 10, segunda com 2 |
| Botão Anterior | Retorna à primeira página |
| Galeria vazia | Mensagem informativa, sem travamento |
| Permissão negada | Mensagem de orientação |
| Logcat | Nome e tamanho em KB de cada imagem da página |
| Exatamente 10 imagens | Próxima consulta vazia informa fim, preservando página |

A galeria pode mudar durante a navegação; Consultar galeria reinicia a paginação. Com acesso parcial, apenas imagens autorizadas são consultadas.

## Estrutura
- MainActivity.kt: permissões, eventos, interface e Logcat.
- MediaStoreRepository.kt: consulta otimizada e paginação.
- Imagem.kt: modelo de metadados e conversão de tamanho.
- activity_main.xml: interface.
- AndroidManifest.xml: permissões e registro da Activity.

Referência: Apostila Completa — Código de Alta Performance: Mobile, módulos 2, 3 e 6.
