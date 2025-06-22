package utility

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.on

fun registerAutoCompleteCommands(kord: Kord) {


    // Check autoCompletion event for cities
    val cities = initCities()


    kord.on<AutoCompleteInteractionCreateEvent> {
        when (interaction.data.data.name.value) {
            "weather_info" -> {
                interaction.suggestString {
                    // for each city in the list, we suggest it if matches the focused option value
                    val suggestedCities: MutableList<String> =  cities.filter { it.contains(interaction.focusedOption.value) }.toMutableList().distinct()
                            .toMutableList()
                    for (index in 0 until 10) {
                        if (index < suggestedCities.size) {
                            println(suggestedCities[index])
                            choice(suggestedCities[index].replace("\"", ""), suggestedCities[index].replace("\"", ""))
                        }
                    }
                }
            }
            else -> {
                println("Unknown autoCompletion event")
                println(interaction.data.data.name.value)
            }
        }
    }


}