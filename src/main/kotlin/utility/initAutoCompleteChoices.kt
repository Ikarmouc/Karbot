package utility

import dotenv
import java.io.File


fun initCities(): MutableList<String> {
    val cities : MutableList<String> = mutableListOf()

    val cityFile : File = File(dotenv.get("PATH_TO_CITIES_CSV"))
    cityFile.forEachLine {
        if(!it.contains("city")){
            val city = it.split(",")[0]
            cities.add(city)

        }
    }
    return cities

}