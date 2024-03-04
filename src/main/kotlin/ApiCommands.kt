import com.google.gson.Gson
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.toLocalDate
import utility.embedMaker
import weather.WeatherData
import weather.WeatherIcons
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

val dotenv: Dotenv = Dotenv.load()

suspend fun getWeather(city: String,response: DeferredPublicMessageInteractionResponseBehavior){
    val client = HttpClient()
    val formatter = DateTimeFormatter.ofPattern("yyyy-M-d['T'H:m:s][.SSS][X]")
    val currentDate = LocalDateTime.now()
    val futureDate = LocalDateTime.now().plusDays(6).format(formatter)
    val url =
        "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" + city + "/${currentDate.format(formatter)}/${futureDate}?unitGroup=metric&key=${dotenv.get("WEATHER_API_KEY")}"
    val responseRequest: HttpResponse = client.request {
        url(Url(url))
        method = HttpMethod.Get
    }
    val body = responseRequest.bodyAsChannel().readRemaining().readText()
    val gson = Gson()
    val weather = gson.fromJson(body, WeatherData::class.java)
    var icon = weather.days?.get(0)?.icon
    // replace - with _ to match the enum
    icon = icon?.replace("-","_")
    client.close()
    val fields = mutableListOf<EmbedBuilder.Field>()

    for(i in 1..<weather?.days?.size!!){
        val field = EmbedBuilder.Field()
        // format the date to ex : 2 june 2021
        val formattedDate = weather.days[i]?.datetime!!.toLocalDate().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " " + weather.days[i]?.datetime!!.toLocalDate().dayOfMonth + " " + weather.days[i]?.datetime!!.toLocalDate().month.getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " "
        field.name = formattedDate
        field.value = "${weather.days[i]!!.conditions}\n"
                "\uD83C\uDF21 Temperature : ${weather.days[i]?.tempmax}°C\n"
        field.inline = true
        fields.add(field)
    }

    response.respond {
        embeds = mutableListOf(
            embedMaker(
                title = "Weather in $city",
                thumbnailUrl = dotenv.get("ASSETS_SERVER_URL")+"/weather/"+ WeatherIcons.valueOf(icon!!.uppercase(Locale.getDefault())).icon,
                footer = "Data from Visual Crossing Weather API",
                description = "${weather.days[0]?.conditions}\n"+
                        "\uD83C\uDF21 Temperature : ${weather.days[0]?.tempmax}°C\n" +
                        "\uD83D\uDCA6 Precipitation : ${weather.days[0]?.precip}mm\n" +
                        "\uD83C\uDF43 Wind : ${weather.days[0]?.windspeed}km/h\n" +
                        "\uD83D\uDCA7 Humidity : ${weather.days[0]?.humidity}%\n" +
                        "☀\uFE0F UV Index : ${weather.days[0]?.uvindex}\n",
                fields = fields
            )
        )
    }
}