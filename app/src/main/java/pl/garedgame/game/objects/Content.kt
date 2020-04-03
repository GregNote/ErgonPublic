package pl.garedgame.game.objects

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import pl.garedgame.game.R
import pl.garedgame.game.objects.subOjects.*

data class Content(
        @Expose val firstNamesFemale: List<String>,
        @Expose val firstNamesMale: List<String>,
        @Expose val lastNames: List<String>,
        @Expose val baseNames: List<String>,
        @Expose val skinTones: List<String>,
        @Expose val hairColors: List<String>,
        @Expose val irisColors: List<String>,
        @Expose val shirtColors: List<String>,

        @Expose val equipment: List<Equipment>,
        @Expose val researches: List<Research>,
        @Expose val buildings: List<Building>
) {
    companion object {
        private val gson = GsonBuilder()
                .registerTypeAdapterFactory(
                        RuntimeTypeAdapterFactory
                                .of(Equipment::class.java, "itemType")
                                .registerSubtype(WeaponRange::class.java, "weaponRanged")
                                .registerSubtype(WeaponMelee::class.java, "weaponMelee")
                                .registerSubtype(Explosive::class.java, "explosive")
                                .registerSubtype(Armor::class.java, "armor")
                                .registerSubtype(Usable::class.java, "usable")
                                .registerSubtype(Other::class.java, "other")
                ).excludeFieldsWithoutExposeAnnotation().create()
        fun loadContent(ctx: Context): Content {
            val json = ctx.resources.openRawResource(R.raw.content).bufferedReader().use { it.readText() }
            return gson.fromJson(json, Content::class.java)
        }
    }
    fun templateEquipment(key: String): Equipment = equipment.find { it.key == key }!!
    fun newEquipment(key: String): Equipment = templateEquipment(key).let {
        gson.fromJson(gson.toJson(it), it::class.java)
    }
    fun getResearch(key: String): Research = researches.find { it.key == key }!!
    fun getBuilding(key: String): Building = buildings.find { it.key == key }!!
}
