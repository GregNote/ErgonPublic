package pl.garedgame.game.objects.subOjects

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Building(
        @Expose val key: String,
        @Expose val name: String,
        @Expose val type: BuildingType,
        @Expose val energyBalance: Int,
        @Expose val personnelCapacity: Int,
        @Expose val image: String = "",
        @Expose val cost: Cost
) {
    enum class BuildingType {
        @SerializedName("Research") Research,
        @SerializedName("Production") Production,
        @SerializedName("Other") Other
    }
}
