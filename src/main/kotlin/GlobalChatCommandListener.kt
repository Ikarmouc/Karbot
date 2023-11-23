import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.toList

suspend fun globalChatCommandlistener(kord: Kord){

    kord.on<ChatInputCommandInteractionCreateEvent> {

        val response = interaction.deferPublicResponse()
        val command = interaction.command
        when(command.data.name.value){
            "ping" -> {
                val resultat = "```Ping vers l'api Discord : ${response.kord.gateway.averagePing?.inWholeMilliseconds} ms```"
                response.respond {
                    content = resultat
                }
            }
            "sum" -> {
                val firstNum = command.integers["first_num"]!!
                val secondNum = command.integers["second_num"]!!
                val result: String = sum(firstNum,secondNum)
                response.respond {
                    content = result
                }
            }
            "liste_des_membres" -> {
                println(kord.getGuild(kord.guilds.toList().get(0).id).members.toList())
                val users = kord.guilds.toList().get(0).members.toList()
                var resultat = "```Liste des utilisateurs : \n"
                for (user in users){
                    resultat += user.data.username + " : " + user.globalName + "\n"
                }
                resultat += "```"
                response.respond {
                    content = resultat
                }
            }
            "about" -> {
                val embed = EmbedBuilder()
                embed.color = Color(0, 0, 0)
                var customUsername = command.users["user"]?.globalName
                if (customUsername == null){
                   customUsername = command.users["user"]?.username
                }
                embed.title = "Informations sur l'utilisateur : ${command.users["user"]?.username}"
                embed.description = "```" +
                    "Nom : ${customUsername}\n" +
                    "Identifiant : ${command.users["user"]?.username}\n ```"

                if(command.users["user"] != null){
                    val thumbnail = EmbedBuilder.Thumbnail()
                    thumbnail.url = command.users["user"]?.avatar?.cdnUrl?.toUrl().toString()
                    embed.thumbnail = thumbnail

                    response.respond {
                        embeds = mutableListOf(embed)
                    }
                }else{
                    val pingDiscord : String = response.kord.gateway.averagePing?.inWholeMilliseconds.toString()
                    embed.title = "Informations sur le bot : "
                    embed.description = "```" + "Version Kord : 0.11.1 \n" + "Version bot : 0.0.3-alpha \n" + "```"
                    embed.field("Ping vers l'api Discord", false){
                        "⬆⬇ $pingDiscord ms"
                    }
                    val thumdnail = EmbedBuilder.Thumbnail()
                    thumdnail.url = "https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif"
                    embed.thumbnail = thumdnail
                response.respond {
                    embeds = mutableListOf(embed)
                }
                }// https://cdn3.emoji.gg/emojis/2217-salesforce-load.gif
            }
            else -> {
                    response.respond {
                        content = "```Erreur de commande, veuillez reessayer```"
                    }
                    error("Erreur de commande, veuillez reessayer")

            }
        }
    }
}